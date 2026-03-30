# Phase 01: Foundation & Navigation - Research

**Researched:** 2025-03-05
**Domain:** Android Jetpack Navigation, Compose, Kotlin Serialization, Splash Screen
**Confidence:** HIGH

## Summary

Phase 1 establishes the architectural backbone of Zenith Grid. It focuses on implementing type-safe navigation using the latest Jetpack Navigation (2.8.x stable series) which introduced Kotlin Serialization support. The project will maintain its 2024.09.00 Compose BOM constraint while adopting the user-requested Lifecycle 2.10.0 libraries.

**Primary recommendation:** Use `navigation-compose:2.8.8` (or 2.8.9 if available) and `kotlinx-serialization-json:1.9.0` to enable type-safe routes, and implement a `LaunchedEffect` based auto-advance for the Splash screen within the NavGraph.

## Standard Stack

### Core
| Library | Version | Purpose | Why Standard |
|---------|---------|---------|--------------|
| `androidx-compose-bom` | `2024.09.00` | Dependency Management | Locked project constraint. |
| `androidx-navigation-compose` | `2.8.8` | Type-safe Navigation | First stable series to support Kotlin Serialization routes; compatible with BOM 2024.09.00. |
| `androidx-lifecycle-runtime-compose` | `2.10.0` | Lifecycle-aware State | User requested 2.10.0; provides `collectAsStateWithLifecycle`. |
| `androidx-lifecycle-viewmodel-compose` | `2.10.0` | Compose ViewModel | User requested 2.10.0; standard for UI state management. |

### Supporting
| Library | Version | Purpose | When to Use |
|---------|---------|---------|--------------|
| `kotlinx-serialization-json` | `1.9.0` | Navigation Type Safety | Required for `@Serializable` routes in Nav 2.8+. |
| `androidx-core-splashscreen` | `1.0.1` | System Splash Handling | Standard for API 23+ backward compatibility for Android 12+ splash. |

### Alternatives Considered
| Instead of | Could Use | Tradeoff |
|------------|-----------|----------|
| `navigation-compose 2.8.x` | `navigation-compose 2.7.x` | 2.7.x uses String routes which are error-prone and lack native Safe Args support. |
| `Manual Delay Splash` | `core-splashscreen` only | `core-splashscreen` is for app startup; Phase 1 requires a navigable Splash screen in the graph. |

**Installation:**
```bash
# Update libs.versions.toml first, then:
# Add plugins and dependencies to app/build.gradle.kts
```

**Version verification:**
- `androidx.navigation:navigation-compose:2.8.8` is the stable choice for type-safe routing.
- `org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0` matches Kotlin 2.2.10.

## Architecture Patterns

### Recommended Project Structure
```
com.jna.tictactoe/
├── ui/              # Global UI theme and components
├── screen/          # Screen-specific composables
│   ├── splash/      # SplashScreen content
│   ├── menu/        # MainMenuScreen content
│   └── game/        # GameBoardScreen content (Phase 3)
├── navigation/      # Navigation graph and routes
└── game/            # Pure Kotlin game logic (Phase 2)
```

### Pattern 1: Type-Safe Navigation (Safe Args)
**What:** Use `@Serializable` objects as routes instead of strings.
**When to use:** All navigation within `NavHost`.
**Example:**
```typescript
// Define routes in navigation/Screen.kt
@Serializable object Splash
@Serializable object Menu
@Serializable object Lobby
@Serializable object Game

// Usage in NavHost
composable<Splash> {
    SplashScreen(onTimeout = { 
        navController.navigate(Menu) {
            popUpTo<Splash> { inclusive = true }
        }
    })
}
```

### Anti-Patterns to Avoid
- **Hard-coded String Routes:** Avoid `navController.navigate("game")`. Use the type-safe objects.
- **Leaking NavController:** Do not pass `NavController` deep into screen composables. Pass lambda callbacks (`onNavigateToMenu`) instead.
- **Long Splash Delay:** Keep splash delay under 2-3 seconds to avoid user frustration.

## Don't Hand-Roll

| Problem | Don't Build | Use Instead | Why |
|---------|-------------|-------------|-----|
| Route Parsing | Custom Regex/String logic | Kotlin Serialization | Native, type-safe, and handles complex arguments. |
| Splash Screen | Custom Activity | `core-splashscreen` | Handles Android 12+ system splash requirements properly. |
| Lifecycle Flow | `Flow.collectAsState` | `collectAsStateWithLifecycle` | Prevents resource leaks when app is in background. |

## Common Pitfalls

### Pitfall 1: Missing Serialization Plugin
**What goes wrong:** Build fails with "Serializer not found" for route objects.
**Why it happens:** Forgot to apply the `org.jetbrains.kotlin.plugin.serialization` plugin.
**How to avoid:** Add the plugin to the root and app `build.gradle.kts`.

### Pitfall 2: Backstack Bloat
**What goes wrong:** Backing out of Main Menu takes user back to Splash Screen.
**Why it happens:** Navigating to Menu without popping Splash.
**How to avoid:** Use `popUpTo<Splash> { inclusive = true }` when navigating from Splash.

## Code Examples

### Navigation Route Definitions
```kotlin
// com/jna/tictactoe/navigation/Routes.kt
import kotlinx.serialization.Serializable

@Serializable sealed interface Screen {
    @Serializable object Splash : Screen
    @Serializable object Menu : Screen
    @Serializable object Lobby : Screen
    @Serializable object Game : Screen
}
```

### SplashScreen Auto-Advance
```kotlin
// com/jna/tictactoe/screen/splash/SplashScreen.kt
@Composable
fun SplashScreen(onTimeout: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(2000) // 2-second delay
        onTimeout()
    }
    // UI layout here
}
```

### MainMenuScreen Layout
```kotlin
// com/jna/tictactoe/screen/menu/MainMenuScreen.kt
@Composable
fun MainMenuScreen(
    onVsCpu: () -> Unit,
    onVsLocal: () -> Unit,
    onVsLan: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Zenith Grid", style = MaterialTheme.typography.headlineLarge)
        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = onVsCpu) { Text("VS CPU") }
        Button(onClick = onVsLocal) { Text("VS Local Human") }
        Button(onClick = onVsLan) { Text("VS LAN Multiplayer") }
    }
}
```

## State of the Art

| Old Approach | Current Approach | When Changed | Impact |
|--------------|------------------|--------------|--------|
| String routes | `@Serializable` routes | Nav 2.8.0 | Compile-time safety for arguments. |
| `collectAsState` | `collectAsStateWithLifecycle` | Lifecycle 2.6.0 | Safer resource management. |
| Custom Splash Activity | `core-splashscreen` library | Android 12 | Unified system splash experience. |

## Open Questions

1. **Shared Element Transitions**
   - What we know: Navigation 2.8.x supports them.
   - What's unclear: If they are stable enough for the specific animations planned in Phase 5.
   - Recommendation: Re-evaluate during Phase 5; keep Phase 1 simple.

## Environment Availability

| Dependency | Required By | Available | Version | Fallback |
|------------|------------|-----------|---------|----------|
| Android Studio | Build/Editor | ✓ | Koala+ | — |
| JDK 11 | Gradle | ✓ | 11 | — |
| Kotlin | Compiler | ✓ | 2.2.10 | — |

## Validation Architecture

### Test Framework
| Property | Value |
|----------|-------|
| Framework | JUnit 4 + Compose Test Rule |
| Config file | `app/src/androidTest` |
| Quick run command | `./gradlew test` |
| Full suite command | `./gradlew connectedAndroidTest` |

### Phase Requirements → Test Map
| Req ID | Behavior | Test Type | Automated Command | File Exists? |
|--------|----------|-----------|-------------------|-------------|
| NAV-01 | Splash auto-advances | E2E/UI | `connectedCheck` | ❌ Wave 0 |
| NAV-02 | Main menu routes correctly | UI | `connectedCheck` | ❌ Wave 0 |

## Sources

### Primary (HIGH confidence)
- Android Developers: Navigation Compose Type Safety - [Official Docs](https://developer.android.com/guide/navigation/design/type-safety)
- Jetpack Navigation Release Notes - [Release Docs](https://developer.android.com/jetpack/androidx/releases/navigation)
- Lifecycle 2.10.0 Release Notes (Assumed current based on user input)

### Secondary (MEDIUM confidence)
- Kotlin Serialization GitHub - Recommended version for Kotlin 2.2.x.

## Metadata

**Confidence breakdown:**
- Standard stack: HIGH - Follows official recommendations and user constraints.
- Architecture: HIGH - Standard UDF and Navigation patterns.
- Pitfalls: HIGH - Well-documented in the Android community.

**Research date:** 2025-03-05
**Valid until:** 2025-04-05
