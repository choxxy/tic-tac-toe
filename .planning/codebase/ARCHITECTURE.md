# Architecture

**Analysis Date:** 2026-03-30

## Pattern Overview

**Overall:** Single-Activity Composable UI — early-stage scaffold with no MVVM/state-management layers yet

**Key Characteristics:**
- One `ComponentActivity` (`MainActivity`) owns the entire Compose tree
- No ViewModels, repositories, or use-case layers exist — all logic is inline in composables
- No navigation library wired up; screen routing is implicit (only `SplashScreen` renders currently)
- No state management beyond static composables — no `remember`, `mutableStateOf`, or `StateFlow` in any current source file
- Design system (`designs/zenith_grid/DESIGN.md`) is fully specified ahead of gameplay implementation

## Layers

**Activity Layer:**
- Purpose: Android OS entry point; bootstraps the Compose host and applies the app theme
- Location: `app/src/main/java/com/jna/tictactoe/MainActivity.kt`
- Contains: `MainActivity` — calls `enableEdgeToEdge()` then `setContent { TictactoeTheme { SplashScreen() } }`
- Depends on: `TictactoeTheme`, `SplashScreen`
- Used by: Android OS via launcher intent

**Screen Layer:**
- Purpose: Full-screen composable units — each file is one app screen
- Location: `app/src/main/java/com/jna/tictactoe/` (package root, same level as `MainActivity`)
- Contains: `SplashScreen.kt` — purely decorative branding screen, no interactive state
- Depends on: Theme color tokens (imported by alias directly, not via `MaterialTheme.colorScheme`)
- Used by: `MainActivity.setContent`

**Theme Layer:**
- Purpose: Material Design 3 design system — color palette, typography scale, theme wrapper
- Location: `app/src/main/java/com/jna/tictactoe/ui/theme/`
- Contains: `Color.kt` (Zenith palette), `Type.kt` (Manrope typography), `Theme.kt` (scheme + wrapper)
- Depends on: `androidx.compose.material3`, Manrope font resources in `app/src/main/res/font/`
- Used by: All composables; `TictactoeTheme` wraps the entire app tree

## Data Flow

**Current (splash screen only):**

1. Android OS launches `MainActivity`
2. `onCreate` calls `enableEdgeToEdge()` then `setContent { TictactoeTheme { SplashScreen() } }`
3. `SplashScreen` renders a fully static composable tree — no user input, no state transitions
4. Sub-composables (`LogoCard`, `BrandIdentity`, `BottomAnchor`) receive no parameters; all values are hardcoded or pulled directly from `Color.kt` constants

**Planned screens (inferred from `designs/` directory):**
- Splash → Home Menu (with settings button) → Game Board
- Player profile editing, local matchmaking dialog, Wi-Fi LAN lobby, settings

**State Management:**
- None implemented. When game state is added, the idiomatic approach for Jetpack Compose is MVVM: `ViewModel` + `StateFlow`/`State<T>` + `collectAsStateWithLifecycle()` in composables.

## Key Abstractions

**`TictactoeTheme` (Theme Wrapper):**
- Purpose: Applies `LightColorScheme`/`DarkColorScheme` and `Typography` to the Compose tree
- Location: `app/src/main/java/com/jna/tictactoe/ui/theme/Theme.kt`
- Pattern: Thin wrapper around `MaterialTheme`; dynamic color is hard-coded `false` to preserve the Zenith palette

**Zenith Color Tokens:**
- Purpose: Named semantic constants mapping the Zenith Grid design palette to Material 3 roles
- Location: `app/src/main/java/com/jna/tictactoe/ui/theme/Color.kt`
- Pattern: Top-level `val` constants with `Zenith` prefix (e.g., `ZenithPrimary`, `ZenithSurfaceContainerHigh`)
- Usage: Composables import tokens directly with aliases — e.g., `import ... ZenithPrimary as PrimaryColor`
- Key semantics: `primary` (#005BC1, blue) = X player; `secondary` (#C1000A, red) = O player; surface tiers (Lowest → Highest) define board depth without borders

**Manrope Typography System:**
- Purpose: Full Material3 `Typography` object using Manrope (5 weights loaded from `res/font/`)
- Location: `app/src/main/java/com/jna/tictactoe/ui/theme/Type.kt`
- Pattern: All 13 Material3 type tokens mapped; display/headline carry `-0.02em` letter spacing per `DESIGN.md`

**Private Screen Sub-Composables:**
- Purpose: Break large screens into named visual regions scoped to the file
- Pattern: `private fun` composables (e.g., `LogoCard`, `BrandIdentity`, `BottomAnchor` in `SplashScreen.kt`)
- Canvas drawing logic is encapsulated in a `private fun DrawScope.drawZenithIcon()` extension function

## Entry Points

**Application Entry:**
- Location: `app/src/main/java/com/jna/tictactoe/MainActivity.kt`
- Triggers: Android launcher (`android.intent.action.MAIN` + `LAUNCHER` intent filter in `AndroidManifest.xml`)
- Responsibilities: Enable edge-to-edge display, apply `TictactoeTheme`, mount root composable

**Compose Root:**
- The `TictactoeTheme { }` call in `MainActivity.onCreate` is the single root of all UI
- Every composable in the app lives inside this theme wrapper

## Error Handling

**Strategy:** Not implemented — app is pre-alpha scaffold with no business logic or I/O

**Patterns:**
- No try/catch, no error states, no error composables exist

## Cross-Cutting Concerns

**Theming:** `TictactoeTheme` in `MainActivity` is the global provider. Dark/light is system-driven (`isSystemInDarkTheme()`). Dynamic color explicitly disabled.

**Edge-to-Edge:** `enableEdgeToEdge()` called before `setContent`. Screens must handle system bar insets via Compose `WindowInsets` padding — not yet applied.

**Navigation:** Not implemented. No `NavHost` or navigation graph. Future navigation should use Jetpack Navigation Compose (`androidx.navigation:navigation-compose`).

**Design System Source of Truth:** `designs/zenith_grid/DESIGN.md` defines visual rules all future screens must follow:
- No borders on the 3x3 grid — use surface container tier shifts instead
- `ZenithSurfaceContainerHighest` for board background, `ZenithSurfaceContainerLowest` for cells
- Glassmorphism overlays: `surfaceContainerLowest` at 70% opacity + backdrop blur for win/result dialogs
- Never use pure black; use `ZenithOnBackground` (#2C333D)
- Corner radius: `rounded-xl` (16.dp) for containers; avoid `rounded-md`

**Logging:** Not implemented.

**Authentication:** Not applicable.

---

*Architecture analysis: 2026-03-30*
