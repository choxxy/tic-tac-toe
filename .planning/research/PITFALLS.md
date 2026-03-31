# Domain Pitfalls

**Domain:** Android game app with LAN multiplayer (NSD + sockets), minimax AI, Jetpack Compose UI
**Project:** Zenith Grid (Tic-Tac-Toe)
**Researched:** 2026-03-30
**Confidence:** HIGH (all findings sourced from official Android documentation)

---

## Critical Pitfalls

These mistakes cause rewrites, persistent bugs, or crashes that are hard to recover from.

---

### Pitfall 1: NSD Listener Reuse Across Lifecycle Cycles

**What goes wrong:**
`NsdManager` listeners (`RegistrationListener`, `DiscoveryListener`, `ResolveListener`) are stateful objects tracked internally by the NSD subsystem. If you call `discoverServices()` or `registerService()` with the same listener instance across multiple `onResume`/`onPause` cycles without fully tearing down first, you receive `FAILURE_ALREADY_ACTIVE` errors and the discovery silently fails to restart.

**Why it happens:**
The common pattern is to initialize listeners once as class-level properties and call them in `onResume`/`onPause`. However, if `onPause` teardown throws or is not fully awaited before `onResume` calls `discoverServices()` again, the NSD subsystem still considers the listener active.

**Consequences:**
- Discovery stops working after the first pause/resume cycle
- No visible error in the UI — services are simply never found
- Hard to diagnose because the listener object appears valid

**Prevention:**
- Create fresh listener instances on each `onResume` (or each discovery session start) rather than reusing stale instances.
- Track a boolean `isDiscovering` flag. Only call `stopServiceDiscovery()` if discovery was actually started; calling it with a listener that was never started causes its own errors.
- Wrap all NSD teardown in a try/catch — `stopServiceDiscovery()` and `unregisterService()` can both throw `IllegalArgumentException` if the listener is not registered.

**Detection warning signs:**
- Device finds peers on first launch but never again after backgrounding the app
- `onStartDiscoveryFailed` callback fires with error code immediately on the second discovery attempt

**Phase to address:** Phase implementing LAN matchmaking lobby. Establish the lifecycle pattern before wiring up the UI.

---

### Pitfall 2: NSD Teardown Order Inversion — Stop Discovery Before Unregistering

**What goes wrong:**
Calling `unregisterService()` before `stopServiceDiscovery()` leaves the discovery subsystem attempting to resolve a service that is being unregistered simultaneously. This can cause a transient window where the peer device sees the service but the connection attempt fails because the server socket has already been closed.

**Why it happens:**
`tearDown()` functions are often written in the order "undo what I registered first" — which puts `unregisterService` first. The correct order is the reverse: stop consumers (discovery) before stopping producers (registration).

**Consequences:**
- Race condition: peer resolves and connects exactly as the server tears down
- Silent disconnect on the peer side with no error surfaced in the lobby
- Harder to reproduce — only triggers during concurrent discovery+teardown

**Prevention:**
Always tear down in this exact order:
1. `stopServiceDiscovery(discoveryListener)`
2. `unregisterService(registrationListener)`
3. Close `ServerSocket`
4. Close all client `Socket` connections

**Detection warning signs:**
- Intermittent "failed to connect" errors that only appear when the host navigates away
- Peer device gets to the game screen but immediately sees a disconnect

**Phase to address:** LAN phase. Codify this order as a single `tearDown()` function with inline comments explaining the sequence.

---

### Pitfall 3: NSD resolveService Called Multiple Times Concurrently

**What goes wrong:**
`onServiceFound()` can fire multiple times for the same service (mDNS re-announcement). If you call `nsdManager.resolveService(service, resolveListener)` inside `onServiceFound()` without guarding against duplicate calls, the second call fails with `FAILURE_ALREADY_ACTIVE` because the first resolution is still in flight. Worse, if the first resolution succeeds and the second also fires, you attempt two socket connections to the same host.

**Why it happens:**
mDNS re-announces services periodically. The NSD API does not deduplicate these announcements before passing them to `onServiceFound()`.

**Consequences:**
- `FAILURE_ALREADY_ACTIVE` error on the resolve listener — silently swallowed if not logged
- Double connection attempt resulting in one socket being orphaned
- App-level "already in lobby" state corruption

**Prevention:**
- Track a `resolving: AtomicBoolean` flag. Set it to `true` before calling `resolveService()`, reset it in both `onServiceResolved()` and `onResolveFailed()`.
- Only accept one resolved peer at a time — once connected, stop discovery immediately.
- In `onServiceFound()`, also filter: `if (service.serviceName == mServiceName) return` to skip self-discovery.

**Detection warning signs:**
- Lobby shows duplicate entries for the same host device
- `onResolveFailed` fires with code 3 (`FAILURE_ALREADY_ACTIVE`) in logs

**Phase to address:** LAN phase, specifically the service discovery and lobby screen.

---

### Pitfall 4: Blocking Socket I/O on the Main Thread

**What goes wrong:**
`Socket.getInputStream().read()` and `ServerSocket.accept()` are blocking calls. If called on the main thread or on `Dispatchers.Main`, they trigger an `android.os.NetworkOnMainThreadException` crash on API 28+ (which is the min SDK for this project).

**Why it happens:**
Early prototypes often put socket setup directly in a `LaunchedEffect` without switching dispatchers, or start a coroutine without `withContext(Dispatchers.IO)`.

**Consequences:**
- Instant crash on device (not on emulator, which may not enforce StrictMode by default)
- If somehow not crashing, blocked main thread = frozen UI, ANR

**Prevention:**
- All `Socket`, `ServerSocket`, `InputStream.read()`, `OutputStream.write()` calls must be inside `withContext(Dispatchers.IO)`.
- Model the network connection as a repository class with suspend functions that internally dispatch to IO. The `ViewModel` calls the repository; the repository owns the dispatcher.
- Never call `socket.close()` on the main thread either — closing a socket with an active read loop can throw and the exception should be caught on the IO thread.

**Detection warning signs:**
- `NetworkOnMainThreadException` in logcat at connection time
- UI freezes for several seconds when connecting (main thread blocked on socket accept)

**Phase to address:** LAN phase. Establish the IO dispatch pattern in the very first socket prototype.

---

### Pitfall 5: Socket Read Loop Coroutine Not Cancelled on Navigation

**What goes wrong:**
A coroutine launched in `viewModelScope` to continuously read incoming socket messages (`while(isActive) { socket.read() }`) will block on `read()` indefinitely. When the user navigates away, `viewModelScope` is cancelled and `isActive` becomes `false` — but the coroutine is stuck inside `read()`, which is a blocking Java call that does not respond to Kotlin coroutine cancellation. The coroutine stays alive, the socket stays open, and the ViewModel cannot be garbage collected.

**Why it happens:**
Kotlin coroutine cancellation is cooperative. `kotlinx.coroutines` suspend functions (`delay`, `withContext`) check for cancellation. Raw Java blocking calls (`InputStream.read()`) do not. `ensureActive()` also cannot help because the thread is blocked before it can be reached.

**Consequences:**
- Memory leak: ViewModel held alive by the blocked coroutine
- Socket remains open — the peer sees the connection as still active even after the local player left the game
- If the user returns to the game screen, a second read loop is started, causing duplicate message processing

**Prevention:**
- Wrap the socket in a `BufferedReader` and use a pattern where closing the socket is the cancellation mechanism: store `socket` in the ViewModel, and in `onCleared()` close it. `socket.close()` causes `read()` to throw `IOException`, which exits the loop.
- Structure the read loop as:
  ```kotlin
  try {
      while (true) {
          ensureActive()
          val line = reader.readLine() ?: break  // null = peer disconnected
          // process line
      }
  } catch (e: IOException) {
      // Socket closed — normal shutdown path
  } finally {
      socket.close()
  }
  ```
- In `ViewModel.onCleared()`: `socket?.close()`

**Detection warning signs:**
- Coroutine dump shows a read loop coroutine in `SUSPENDED` state after navigating away
- Peer device still receives messages after local player left

**Phase to address:** LAN phase. Treat this as a design requirement before writing any socket code.

---

### Pitfall 6: Game State Held in Mutable Plain Lists Instead of Observable State

**What goes wrong:**
Representing the 3x3 board as `var board = mutableListOf<CellState>()` in the ViewModel and mutating it in-place. Compose cannot observe mutations to a `MutableList` — only replacements of the state reference trigger recomposition.

**Why it happens:**
Developers familiar with imperative Android code reach for `MutableList` naturally. The bug is subtle: the board visually updates on some moves but not others, depending on whether an unrelated recomposition happened to refresh it.

**Consequences:**
- Board cells do not update on move — the UI shows a stale board
- Intermittent behavior: sometimes works, sometimes doesn't (depends on other recompositions)
- Win detection may be correct but the UI shows no winner because the board never re-renders

**Prevention:**
- Represent the board as `StateFlow<List<CellState>>` in the ViewModel. Emit a new list on every move:
  ```kotlin
  _board.value = _board.value.toMutableList().also { it[index] = newState }
  ```
- Or use `mutableStateListOf<CellState>()` (Compose-observable list) if keeping state inside a composable for simple cases.
- Use immutable data classes for `GameState` and replace the whole object on each update.

**Detection warning signs:**
- UI sometimes shows empty cells after a move was made
- `Log.d` in the ViewModel shows the correct board but the screen does not reflect it

**Phase to address:** Game logic / board UI phase. Establish the state model before implementing any move handling.

---

### Pitfall 7: ViewModel Scoped to Activity Instead of Nav Destination

**What goes wrong:**
Creating `GameViewModel` at Activity scope (`viewModel()` at the Activity level, or passing it down from `MainActivity`). The ViewModel persists for the entire app session — the board state from a finished game bleeds into the next game, score tracking cannot be reset by navigating to a new screen, and the LAN socket is never torn down between games.

**Why it happens:**
The project currently has only one screen. When adding Navigation Compose, it is tempting to keep the ViewModel at Activity scope to avoid "figuring out nav scoping." This works initially but creates architectural debt.

**Consequences:**
- "New Game" starts with the previous board still populated
- LAN socket from game 1 is not closed before game 2 starts
- Score display shows stale data from previous sessions

**Prevention:**
- Scope `GameViewModel` to the game board navigation destination: in the composable, `val vm: GameViewModel = viewModel()` without any explicit owner — Navigation Compose automatically scopes it to the back stack entry.
- Score tracking lives in a `SessionViewModel` scoped to the Activity (or nav graph root) — session-level data, separate from per-game state.
- If LAN multiplayer needs a longer-lived connection object, pass it as a constructor-injected dependency so the ViewModel controls its lifecycle explicitly.

**Detection warning signs:**
- "Play Again" shows pieces from the previous game for a frame before resetting
- Navigating Home and back starts a new ViewModel but the old socket is still sending messages to the new one

**Phase to address:** Navigation phase (must establish VM scoping before game board is implemented).

---

## Moderate Pitfalls

These cause bugs or rework but are fixable without rewriting major components.

---

### Pitfall 8: Minimax Without Alpha-Beta Pruning Hangs the UI Thread

**What goes wrong:**
For a 3x3 board, pure minimax without pruning evaluates at most 9! = 362,880 leaf nodes, which runs in under 1ms. This is not a performance problem on 3x3. The danger is calling minimax synchronously on the main thread — which is safe for 3x3 — and then assuming the same pattern is acceptable if the board representation is generalized. The real risk is that synchronous minimax, even if fast, blocks the UI thread for the duration. On a cold JVM (first CPU move after app start) JIT is not warm, and the call may take 10-20ms, causing a visible frame drop.

**Prevention:**
- Run minimax in a coroutine on `Dispatchers.Default`:
  ```kotlin
  viewModelScope.launch {
      val move = withContext(Dispatchers.Default) { minimax(board) }
      applyMove(move)
  }
  ```
- Show a brief "thinking" indicator during the coroutine to communicate the async gap (even if near-instant).

**Detection warning signs:**
- Systrace shows main thread blocked for >8ms on CPU move calculation
- `StrictMode.ThreadPolicy` violations in debug builds

**Phase to address:** Game logic / CPU player phase.

---

### Pitfall 9: Minimax Returns Wrong Move on Edge Cases

**What goes wrong:**
Minimax implementations commonly fail on:
1. **Empty board on first move** — minimax correctly identifies all moves as equal but may return the last index evaluated (bottom-right) instead of a strategic first move (center). Not a correctness bug but makes "Hard" feel beatable to experienced players.
2. **Terminal state not checked before recursion** — if the win check is done at the top of the recursive call instead of before making the recursive call, the function evaluates one extra layer past the terminal state, returning wrong scores.
3. **Score not adjusted for depth** — returning flat `+10`/`-10` without subtracting depth means minimax may choose a losing-in-3 over a winning-in-1 move (they score the same). Subtract depth from win scores: `+10 - depth`.

**Prevention:**
- Always check terminal state first: win, lose, draw → return score immediately before recursing.
- Use depth-adjusted scores: `score = 10 - depth` for a win, `-10 + depth` for a loss.
- Write unit tests for every edge case: empty board, board with one move remaining, boards with forced wins in 1/2 moves.

**Detection warning signs:**
- "Hard" CPU does not take an immediate win when available
- "Hard" CPU does not block an immediate human win
- CPU plays bottom-right corner as first move on an empty board (minor but testable)

**Phase to address:** Game logic / CPU player phase. Unit-test minimax independently before wiring to the UI.

---

### Pitfall 10: Compose Animation Object Allocation Inside DrawScope

**What goes wrong:**
The existing `SplashScreen.kt` already has this pattern (noted in CONCERNS.md): constructing `Path` objects inside `DrawScope` lambdas on every frame. For the game board animations (piece placement scale/fade, win line draw animation), this means allocating `Path` or `Paint` objects on the render thread every frame, causing GC pressure and jank.

**Prevention:**
- Hoist `Path` and any reusable drawing objects to `remember { }` blocks outside the `Canvas` call.
- For win line animations, use `Animatable(0f)` with `drawLine` parameterized by the animated fraction — no path allocation needed.
- Prefer `animateFloatAsState` / `Animatable` for value-based animations over manual frame callbacks.

**Detection warning signs:**
- Systrace shows GC events correlated with animation frames
- Layout Inspector shows the `Canvas` composable recomposing every frame during a static (non-animating) board state

**Phase to address:** Board UI / animation phase.

---

### Pitfall 11: Compose State Read Scope Too Wide — Whole Screen Recomposes on Every Move

**What goes wrong:**
Collecting `gameState: StateFlow<GameState>` at the top of the screen composable and passing the entire object into child composables causes every cell, the score bar, the turn indicator, and all buttons to recompose on every single state change — including a simple cell fill.

**Prevention:**
- Use `derivedStateOf` or split the `StateFlow` into granular streams:
  - `boardFlow: StateFlow<List<CellState>>` — only the board
  - `currentTurnFlow: StateFlow<Player>` — only the turn indicator
  - `scoreFlow: StateFlow<Score>` — only the score bar
- Pass only the needed slice to each composable. A cell only needs `CellState` for its own index; it should not recompose when the turn indicator changes.
- For cell composables, use `key(index)` in `LazyVerticalGrid` or accept stable, immutable parameters so Compose's smart recomposition can skip unchanged cells.

**Detection warning signs:**
- Android Studio Layout Inspector shows all 9 cells highlighted as recomposed when only one was tapped
- Frame timing shows >2ms compose time for a move that should only repaint one cell

**Phase to address:** Board UI phase.

---

### Pitfall 12: LAN Disconnect Leaves Game State Inconsistent

**What goes wrong:**
When a peer disconnects mid-game, `readLine()` returns `null` (clean disconnect) or throws `IOException` (abrupt disconnect). If the disconnect handler immediately resets game state (board, turn, scores), the remaining player loses their view of the last valid board state. If the handler does nothing, the game freezes with no feedback.

**Prevention:**
- Distinguish "disconnect during my turn" from "disconnect during peer's turn":
  - During peer's turn: show a reconnection spinner, preserve board state, wait for reconnect (as per PROJECT.md requirement).
  - During my turn after peer disconnects: show disconnect dialog with option to "Wait for reconnect" or "Exit to menu."
- Store the last known `GameState` snapshot. On reconnect (new socket connection from same peer), send this snapshot so both sides re-synchronize.
- Never mutate `GameState` in the disconnect handler — only change connection status.

**Detection warning signs:**
- Killing one device's app mid-game causes the other to show a blank board
- Score counters reset on disconnect

**Phase to address:** LAN phase — specifically the disconnect/reconnect flow.

---

### Pitfall 13: Missing CHANGE_WIFI_STATE / ACCESS_WIFI_STATE Permissions for NSD

**What goes wrong:**
On Android 12+ (API 31+), using `WifiManager` APIs (which NSD internally may trigger) without `CHANGE_WIFI_STATE` or `ACCESS_WIFI_STATE` declared in the manifest causes silent failures or `SecurityException`. NSD itself requires `ACCESS_WIFI_STATE` on some device manufacturers' custom Android builds.

**Why it happens:**
The official NSD sample only shows declaring `INTERNET` (which is actually not needed for NSD-only usage). Manufacturer-specific requirements are not in the base AOSP docs.

**Prevention:**
- Declare in `AndroidManifest.xml`:
  ```xml
  <uses-permission android:name="android.permission.INTERNET" />
  <uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
  <uses-permission android:name="android.permission.CHANGE_WIFI_STATE" />
  ```
  Note: `INTERNET` is a normal permission (no runtime grant needed) but is required for TCP sockets even on LAN.
- Test on a physical device from a non-Google manufacturer (Samsung, Xiaomi) — emulators often have looser permission enforcement.

**Detection warning signs:**
- `SecurityException` in logcat when calling `discoverServices()` on a physical device but not on the emulator
- NSD works on the development device (Pixel) but not on a friend's Samsung

**Phase to address:** LAN phase, before first physical device test.

---

## Minor Pitfalls

These cause minor friction but are low-effort to fix.

---

### Pitfall 14: NSD Service Name Must Be Captured From onServiceRegistered, Not Assumed

**What goes wrong:**
The string passed to `NsdServiceInfo.setServiceName()` is a suggestion. If another device on the network is already advertising a service with the same name, the NSD subsystem will rename yours (e.g., "ZenithGrid" → "ZenithGrid (2)"). If you hard-code the name you requested for use in `onServiceFound()` filtering (to skip self-discovery), you will fail to skip your own re-announced service under the modified name.

**Prevention:**
- Capture the actual registered name in `onServiceRegistered(info)`:
  ```kotlin
  override fun onServiceRegistered(info: NsdServiceInfo) {
      registeredServiceName = info.serviceName  // May differ from requested name
  }
  ```
- Use `registeredServiceName` (not the originally requested name) in `onServiceFound()` self-filtering.

**Phase to address:** LAN phase.

---

### Pitfall 15: Hard-Coding Port 0 vs Known Port

**What goes wrong:**
Using a hard-coded port (e.g., 9000) for the game server means that if another instance of the app is running on the same device (unusual but possible during development), `ServerSocket(9000)` throws `BindException: Address already in use`. Port 0 lets the OS assign an available port, which is then communicated to peers via NSD `serviceInfo.port`.

**Prevention:**
- Always: `ServerSocket(0)` then read back `serverSocket.localPort` and register that port with NSD.

**Phase to address:** LAN phase.

---

### Pitfall 16: Score Tracker Resets on Configuration Change Without rememberSaveable

**What goes wrong:**
If the session score (`X wins: 3, O wins: 1, Draws: 0`) is held in a `ViewModel` that is scoped to the game screen navigation destination (not the Activity), rotating the device destroys the back-stack entry's ViewModel (in some Navigation Compose configurations) and resets scores.

**Prevention:**
- Score tracking belongs in a `SessionViewModel` scoped to the Activity (or the nav graph root), not the per-game `GameViewModel`.
- Alternatively, use `SavedStateHandle` in the `SessionViewModel` to survive process death.

**Phase to address:** Game logic phase when score tracking is first introduced.

---

### Pitfall 17: CancellationException Swallowed in Socket Error Handling

**What goes wrong:**
Wrapping the entire socket read loop in `catch (e: Exception)` swallows `CancellationException`. Kotlin coroutines use `CancellationException` to signal cancellation; catching and not rethrowing it prevents the coroutine from being cancelled properly, breaking structured concurrency.

**Prevention:**
- Catch only `IOException` in socket error handlers.
- If using a broad catch: `catch (e: Exception) { if (e is CancellationException) throw e; /* handle IO error */ }`

**Phase to address:** LAN phase.

---

### Pitfall 18: Compose BOM Version Skew With Current Toolchain

**What goes wrong:**
The project currently pins `composeBom = "2024.09.00"` (September 2024) while running Kotlin 2.2.10 and AGP 9.1.0 (noted in CONCERNS.md). The Compose compiler plugin version embedded in Kotlin 2.x may not be validated against the September 2024 BOM, causing subtle compiler warnings or missed optimization passes.

**Prevention:**
- Upgrade the Compose BOM to a 2025.x release before adding any new Compose screens. The BOM is the single version pin — all Compose library versions follow from it.
- Do this in the foundation phase, before the game board screen is implemented.

**Phase to address:** Foundation/setup phase (first task).

---

## Phase-Specific Warnings

| Phase Topic | Likely Pitfall | Mitigation |
|---|---|---|
| Foundation setup | Compose BOM skew with Kotlin 2.2.10 | Upgrade BOM before any new screens |
| Navigation setup | ViewModel scoped to wrong owner (Activity vs destination) | Establish nav-scoped VM pattern immediately |
| Game board UI | Mutable list not triggering recomposition | Use `StateFlow<List<CellState>>` with immutable emissions |
| Game board UI | Over-wide recomposition scope | Split state into granular flows per UI region |
| CPU player (minimax) | Missing depth in score, wrong terminal check order | Unit-test every edge case before wiring to UI |
| CPU player (minimax) | Blocking main thread (even sub-millisecond) | Always dispatch to `Dispatchers.Default` |
| Animations | Path/object allocation inside DrawScope | Hoist allocations into `remember {}` |
| LAN matchmaking | NSD listener reuse on resume | Create fresh listeners each session |
| LAN matchmaking | Duplicate `resolveService` calls | AtomicBoolean guard before resolving |
| LAN matchmaking | Missing WIFI permissions on physical devices | Add all three Wi-Fi permissions up front |
| LAN game | Socket read blocking after navigation | Close socket in `ViewModel.onCleared()` |
| LAN game | Swallowed `CancellationException` | Catch only `IOException` in socket loops |
| LAN game | Inconsistent state on disconnect | Never mutate board state in disconnect handler |
| Score tracking | Score lost on config change | Scope to Activity-level `SessionViewModel` |

---

## Sources

- Android Developer — [Connect with Network Service Discovery](https://developer.android.com/develop/connectivity/wifi/use-nsd) — HIGH confidence
- Android Developer — [ViewModel overview](https://developer.android.com/topic/libraries/architecture/viewmodel) — HIGH confidence
- Android Developer — [Kotlin coroutines best practices on Android](https://developer.android.com/kotlin/coroutines/coroutines-best-practices) — HIGH confidence
- Android Developer — [Jetpack Compose performance best practices](https://developer.android.com/jetpack/compose/performance/bestpractices) — HIGH confidence
- Android Developer — [State and Jetpack Compose](https://developer.android.com/jetpack/compose/state) — HIGH confidence
- Android Developer — [Navigation back stack](https://developer.android.com/guide/navigation/backstack) — HIGH confidence
- Android Developer — [Navigate with Compose](https://developer.android.com/develop/ui/compose/navigation) — HIGH confidence
- Project file: `/Users/choxxy/Projects/tictactoe/.planning/codebase/CONCERNS.md` — existing tech debt that compounds several pitfalls above
