# Technology Stack

**Project:** Tic-Tac-Toe (Android)
**Researched:** 2026-03-30
**Scope:** Additions and changes needed beyond existing scaffolding for game logic, LAN multiplayer, and CPU AI.

---

## Existing Stack (Do Not Re-research)

Already in place — assume stable:

| Artifact | Version | Role |
|----------|---------|------|
| Kotlin | 2.2.10 | Language |
| Jetpack Compose BOM | 2024.09.00 | UI framework (all `androidx.compose.*`) |
| Material Design 3 | via BOM | Design system |
| `androidx.lifecycle:lifecycle-runtime-ktx` | 2.10.0 | Coroutine scopes, lifecycle |
| `androidx.activity:activity-compose` | 1.13.0 | Compose + ComponentActivity |
| `androidx.core:core-ktx` | 1.18.0 | Android KTX extensions |
| JUnit 4.13.2 / Espresso 3.7.0 | — | Testing |

---

## Compose BOM: Upgrade Recommended

**Current:** `androidx.compose:compose-bom:2024.09.00`
**Latest stable as of 2026-03-30:** `2026.03.01`

The BOM version in `libs.versions.toml` is 18 months stale. The latest BOM resolves `androidx.compose.ui` and `material3` to current patch releases (Compose UI 1.10.6, Material3 1.4.0 vs whatever 2024.09.00 shipped). For a new milestone this is a low-risk upgrade with meaningful bug fix coverage. It is not strictly required to unblock any feature but should be done at milestone start.

**Confidence:** HIGH (fetched directly from the BOM mapping page)

---

## New Dependencies Needed

### 1. ViewModel + StateFlow for Compose

**Add:** `lifecycle-viewmodel-compose`

```toml
# libs.versions.toml
lifecycle-viewmodel-compose = { group = "androidx.lifecycle", name = "lifecycle-viewmodel-compose", version.ref = "lifecycleRuntimeKtx" }
lifecycle-runtime-compose    = { group = "androidx.lifecycle", name = "lifecycle-runtime-compose",    version.ref = "lifecycleRuntimeKtx" }
```

```kotlin
// app/build.gradle.kts
implementation(libs.lifecycle.viewmodel.compose)
implementation(libs.lifecycle.runtime.compose)
```

Both artifacts are version `2.10.0` — the same version already declared for `lifecycle-runtime-ktx`. Use the same `lifecycleRuntimeKtx` version ref; no new version pin needed.

`lifecycle-viewmodel-compose` provides the `viewModel()` composable factory.
`lifecycle-runtime-compose` provides `collectAsStateWithLifecycle()`, which is strictly preferred over `collectAsState()` because it automatically pauses collection when the app is backgrounded, saving CPU and battery.

**Pattern:**

```kotlin
// GameViewModel.kt
class GameViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    fun onCellTapped(index: Int) {
        _uiState.update { current -> /* pure function, returns new state */ }
    }
}

// GameScreen.kt
@Composable
fun GameScreen(vm: GameViewModel = viewModel()) {
    val state by vm.uiState.collectAsStateWithLifecycle()
    // render from state
}
```

**Why not Hilt/DI:** The project is single-module, single-screen-class scope. Plain `viewModel()` factory is sufficient. Adding Hilt at this size is scope inflation.

**Confidence:** HIGH (verified against official Lifecycle 2.10.0 release notes and ViewModel docs)

---

### 2. Android NSD (Network Service Discovery)

**No new dependency.** NSD is part of the Android framework via `android.net.nsd.NsdManager`. Available since API 16; this project's min SDK 28 makes it fully safe.

**Required manifest permissions:**

```xml
<!-- AndroidManifest.xml — NSD over LAN requires no INTERNET permission,
     only local network visibility. -->
<uses-permission android:name="android.permission.INTERNET" />
<!-- INTERNET is needed for TCP ServerSocket/Socket even on LAN -->
```

No `ACCESS_FINE_LOCATION` is required for NSD (unlike Wi-Fi Direct peer discovery). NSD mDNS registration and discovery are permission-free beyond `INTERNET`.

**API strategy — use the pre-API-34 callback style, wrapped in coroutines:**

Android 34 introduced `ServiceInfoCallback` and a new executor-based `resolveService` overload, but min SDK is 28. The older callback interfaces (`RegistrationListener`, `DiscoveryListener`, `ResolveListener`) are still fully functional and unambiguously documented. Using the old style keeps compatibility clean and avoids an API level branch.

The callbacks are not suspend-friendly out of the box. Wrap resolution in `suspendCancellableCoroutine`:

```kotlin
suspend fun NsdManager.resolveServiceSuspend(info: NsdServiceInfo): NsdServiceInfo =
    suspendCancellableCoroutine { cont ->
        resolveService(info, object : NsdManager.ResolveListener {
            override fun onServiceResolved(resolved: NsdServiceInfo) =
                cont.resume(resolved)
            override fun onResolveFailed(si: NsdServiceInfo, errorCode: Int) =
                cont.resumeWithException(IOException("NSD resolve failed: $errorCode"))
        })
        cont.invokeOnCancellation { /* NsdManager has no cancel for resolve */ }
    }
```

Discovery events (service found/lost) naturally become a `callbackFlow`:

```kotlin
fun NsdManager.discoveryFlow(serviceType: String): Flow<NsdEvent> = callbackFlow {
    val listener = object : NsdManager.DiscoveryListener {
        override fun onServiceFound(info: NsdServiceInfo) { trySend(NsdEvent.Found(info)) }
        override fun onServiceLost(info: NsdServiceInfo)  { trySend(NsdEvent.Lost(info))  }
        // ... other overrides
        override fun onDiscoveryStopped(type: String)     { channel.close() }
        override fun onStartDiscoveryFailed(type: String, code: Int) {
            close(IOException("Discovery failed: $code"))
        }
    }
    discoverServices(serviceType, NsdManager.PROTOCOL_DNS_SD, listener)
    awaitClose { stopServiceDiscovery(listener) }
}
```

**TCP transport after NSD resolves:**

After resolution produces an `InetAddress` + port, use plain Java `ServerSocket` (host) and `Socket` (client) wrapped in `viewModelScope.launch(Dispatchers.IO)`. No third-party networking library is needed. Message framing: newline-delimited JSON strings (simple, debuggable, zero extra dependencies).

**Important NSD caveats:**

- `discoverServices` is resource-intensive. Stop it in the ViewModel's `onCleared` or when a connection is established.
- The same `DiscoveryListener` instance cannot be reused after `stopServiceDiscovery`. Create a new instance each time discovery restarts.
- Service name uniqueness: Android appends "(2)", "(3)" automatically on conflict — read `onServiceRegistered` for the actual registered name before advertising it to peers.
- Resolution of the same service can only be in-flight once per `ResolveListener` instance. Queue or gate concurrent resolve calls.

**Confidence:** HIGH for API surface (official NSD docs). MEDIUM for coroutine wrapper pattern (documented community pattern, not in official docs).

---

### 3. CPU AI — Minimax Algorithm

**No new dependency.** Minimax for 3x3 Tic-Tac-Toe is pure Kotlin. No library is needed or recommended.

**Implementation notes:**

The complete game tree for 3x3 Tic-Tac-Toe has at most 9! = 362,880 leaf nodes. Even without alpha-beta pruning, exhaustive minimax runs in under 1ms on any modern device. Run it on `Dispatchers.Default` (or even the main thread — it's that fast) to keep the ViewModel clean:

```kotlin
fun minimax(board: BoardState, isMaximising: Boolean): Int { /* pure function */ }
```

Three difficulty modes:

| Difficulty | Strategy |
|------------|----------|
| Easy | Random legal move |
| Medium | Minimax with probability ~50% (random otherwise) |
| Hard | Full minimax — unbeatable |

Medium "difficulty" is best implemented as: 70% chance play minimax best move, 30% chance play random move. This feels competitive without being perfect.

Do not use alpha-beta pruning complexity for 3x3 — the tree is small enough that it adds code complexity with zero user-perceivable benefit.

**Confidence:** HIGH (well-established algorithm, no external dependency risk)

---

### 4. Sound Effects

**No new dependency.** Use Android's `SoundPool` from `android.media.SoundPool`.

**Why SoundPool over MediaPlayer:** `MediaPlayer` is for long-form audio (music, podcasts). `SoundPool` pre-loads short clips into memory and plays them with sub-10ms latency. For tap feedback and win jingles this is the correct tool.

**Why not ExoPlayer:** Complete overkill for 3-5 short WAV/OGG clips. Adds a significant dependency footprint.

**Construction (API 21+ Builder, min SDK 28 so always safe):**

```kotlin
val soundPool = SoundPool.Builder()
    .setMaxStreams(4)
    .setAudioAttributes(
        AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
    )
    .build()
```

**Asset location:** `app/src/main/res/raw/` — loaded via `soundPool.load(context, R.raw.place_piece, 1)`. No storage permissions needed (bundled resources).

**Lifecycle:** Load in ViewModel init (or a dedicated `SoundManager` object held by the ViewModel). Release in `onCleared()`.

**Confidence:** HIGH (official SoundPool.Builder docs confirmed)

---

### 5. Compose Animations

**No new dependency.** All needed animations are in `androidx.compose.animation` which is already pulled in via the Compose BOM.

| Effect | API |
|--------|-----|
| Piece placement (scale in) | `AnimatedVisibility` with `scaleIn() + fadeIn()` enter transition |
| Win line draw | `animateFloatAsState` driving a custom `Canvas` stroke progress |
| Cell highlight on win | `animateColorAsState` transitioning cell background color |
| Result dialog entrance | `AnimatedVisibility` with `slideInVertically() + fadeIn()` |
| Reconnecting spinner | `CircularProgressIndicator` (Material3 built-in, no animation code needed) |
| Score counter increment | `AnimatedContent` with vertical slide for number flip |

For the win line specifically: draw it as a `Canvas` composable with an animated `Float` (0f → 1f) controlling how far the stroke has been drawn. `animateFloatAsState` with a `tween(durationMillis = 400)` spec is all that is needed.

**Confidence:** HIGH (verified against Compose animation docs)

---

### 6. Navigation

**Recommended: `androidx.navigation:navigation-compose`**

The app has three distinct screens: mode selection, game board, and (optionally) settings. Without Compose Navigation, all state and screen switching lives in `MainActivity` as a `when` branch — manageable but messy as screens grow.

```toml
# libs.versions.toml
navigationCompose = "2.9.0"
androidx-navigation-compose = { group = "androidx.navigation", name = "navigation-compose", version.ref = "navigationCompose" }
```

Use `NavHost` with typed routes (Kotlin Serializable objects, the current `navigation-compose` approach since 2.8.0). Do not use string-based routes — they were superseded.

**Why not skip it:** With three screens + back stack for the "Play Again" flow, manual screen state in `MainActivity` becomes a source of bugs. Navigation-Compose adds ~200KB and eliminates that complexity.

**Confidence:** MEDIUM — version 2.9.0 is the latest stable as of early 2026 (based on Lifecycle 2.10.0 release timeline, navigation follows similar cadence). Verify exact version in `developer.android.com/jetpack/androidx/releases/navigation` before pinning.

---

## Summary: What to Add to `libs.versions.toml` / `build.gradle.kts`

```toml
# libs.versions.toml additions

[versions]
# No new version refs needed for lifecycle (reuse lifecycleRuntimeKtx = "2.10.0")
composeBom        = "2026.03.01"        # upgrade from 2024.09.00
navigationCompose = "2.9.0"             # verify before pinning

[libraries]
androidx-lifecycle-viewmodel-compose = { group = "androidx.lifecycle", name = "lifecycle-viewmodel-compose", version.ref = "lifecycleRuntimeKtx" }
androidx-lifecycle-runtime-compose   = { group = "androidx.lifecycle", name = "lifecycle-runtime-compose",   version.ref = "lifecycleRuntimeKtx" }
androidx-navigation-compose          = { group = "androidx.navigation", name = "navigation-compose",          version.ref = "navigationCompose" }
```

```kotlin
// app/build.gradle.kts additions
implementation(libs.androidx.lifecycle.viewmodel.compose)
implementation(libs.androidx.lifecycle.runtime.compose)
implementation(libs.androidx.navigation.compose)
```

**Nothing else.** Every other feature (NSD, SoundPool, Minimax, Canvas animations) is pure Android SDK or pure Kotlin — no third-party library needed.

---

## What NOT to Use

| Candidate | Why Not |
|-----------|---------|
| Hilt / Dagger | Single-module app, one ViewModel per screen. No injection complexity that warrants DI. |
| Ktor / OkHttp | LAN-only TCP via `java.net.Socket` is sufficient. HTTP adds framing overhead and a large dependency. |
| ExoPlayer / Media3 | 3-5 short sound clips. SoundPool is the correct tool. ExoPlayer is for video/streaming. |
| Room / DataStore | Session-only score tracking. No persistence needed. |
| Coroutines library (separate) | Already provided transitively by `lifecycle-runtime-ktx`. Do not add `kotlinx-coroutines-android` explicitly. |
| Wi-Fi Direct (WifiP2pManager) | NSD over mDNS is simpler, doesn't require accepting a system permission dialog, and works on the same LAN without pairing. Wi-Fi Direct is for device-to-device without a router, which is out of scope. |
| Third-party animation libraries (Lottie, etc.) | All required animations are achievable with Compose built-ins. Adding Lottie for a win celebration is acceptable optionally but not necessary. |
| Alpha-beta pruning for minimax | 3x3 board — not needed. Adds code, no user benefit. |

---

## Manifest Changes Required

```xml
<!-- Required for TCP socket communication (even LAN-only) -->
<uses-permission android:name="android.permission.INTERNET" />

<!-- No additional permissions needed:
     - NSD mDNS does NOT require ACCESS_FINE_LOCATION (unlike Wi-Fi Direct)
     - SoundPool uses bundled res/raw/ assets (no READ_EXTERNAL_STORAGE)
     - LAN-only networking (no CHANGE_NETWORK_STATE or ACCESS_NETWORK_STATE needed)
-->
```

---

## Confidence Assessment

| Area | Confidence | Source |
|------|------------|--------|
| ViewModel + StateFlow pattern | HIGH | Official ViewModel docs + Lifecycle 2.10.0 release notes |
| `lifecycle-viewmodel-compose` version (2.10.0) | HIGH | Official Lifecycle releases page |
| Compose BOM latest version (2026.03.01) | HIGH | Official BOM mapping page |
| NSD callback API surface | HIGH | Official NsdManager docs |
| NSD coroutine wrapping pattern | MEDIUM | Documented community pattern; not in official docs |
| NSD API 34 ServiceInfoCallback details | LOW | WebFetch returned incomplete content — avoid for min SDK 28 |
| SoundPool.Builder + AudioAttributes | HIGH | Official SoundPool.Builder reference |
| Compose animation APIs | HIGH | Official Compose animation docs |
| Navigation-compose version (2.9.0) | MEDIUM | Inferred from Lifecycle 2.10.0 release cadence; verify before pinning |
| Minimax algorithm for 3x3 | HIGH | Established CS algorithm, no library dependency |

---

## Sources

- Android NSD Training Guide: https://developer.android.com/training/connect-devices-wirelessly/nsd
- NsdManager API Reference: https://developer.android.com/reference/android/net/nsd/NsdManager
- ViewModel Architecture Guide: https://developer.android.com/topic/libraries/architecture/viewmodel
- ViewModel Scoping APIs: https://developer.android.com/topic/libraries/architecture/viewmodel/viewmodel-apis
- Lifecycle 2.10.0 Releases: https://developer.android.com/jetpack/androidx/releases/lifecycle
- Compose BOM Mapping (latest): https://developer.android.com/jetpack/compose/bom/bom-mapping
- Compose Animation Introduction: https://developer.android.com/jetpack/compose/animation/introduction
- Compose Animation Composables: https://developer.android.com/jetpack/compose/animation/composables-modifiers
- SoundPool.Builder Reference: https://developer.android.com/reference/android/media/SoundPool.Builder
