# Codebase Concerns

**Analysis Date:** 2026-03-30

---

## Missing Critical Features

**All game logic is absent:**
- Problem: No game board, no game state, no win/draw detection, no turn management, no move validation.
- Blocks: The app cannot be played. Everything past the splash screen is unimplemented.
- Files: No game logic files exist anywhere under `app/src/main/java/com/jna/tictactoe/`.

**No navigation system:**
- Problem: `MainActivity` hard-codes `SplashScreen()` directly in `setContent`. There is no `NavHost`, no navigation graph, and no back-stack management.
- Blocks: Adding any second screen (home menu, game board, settings, matchmaking lobby) requires a navigation layer first or the `MainActivity` becomes an unmanageable conditional dispatcher.
- Files: `app/src/main/java/com/jna/tictactoe/MainActivity.kt`
- Fix approach: Add `androidx.navigation:navigation-compose` and establish a `NavHost` with named routes before building any additional screen.

**No ViewModel or state management:**
- Problem: Zero state infrastructure exists. `SplashScreen` is stateless; no `ViewModel`, `StateFlow`, or `remember`/`mutableStateOf` usage anywhere.
- Blocks: Game state (board cells, scores, current turn, winner) cannot be maintained across recompositions or configuration changes (rotation, font scale).
- Files: `app/src/main/java/com/jna/tictactoe/SplashScreen.kt`
- Fix approach: Introduce `androidx.lifecycle:lifecycle-viewmodel-compose`. Create a `GameViewModel` before implementing the game board screen.

**Seven screens designed but only one implemented:**
- Problem: Design assets exist for: splash screen, home menu + settings button, game board, edit player profile, local matchmaking dialog, settings, and Wi-Fi/LAN lobby. Only the splash screen exists in code.
- Files: `designs/` directory contains PNGs for all screens; only `app/src/main/java/com/jna/tictactoe/SplashScreen.kt` maps to a design.
- Impact: Every remaining screen is unstarted work.

**Splash screen has no exit behaviour:**
- Problem: `SplashScreen` renders static branding but has no auto-advance timer, no tap-to-proceed handler, and no navigation call. The app shows the splash and stays there indefinitely.
- Files: `app/src/main/java/com/jna/tictactoe/SplashScreen.kt`
- Fix approach: Add a `LaunchedEffect` with a delay (or a tap gesture) that triggers navigation to the home menu once navigation is wired up.

---

## Tech Debt

**Direct color token imports bypass `MaterialTheme.colorScheme`:**
- Issue: `SplashScreen.kt` imports raw `Color.kt` constants via type aliases (e.g., `ZenithPrimary as PrimaryColor`) instead of reading `MaterialTheme.colorScheme.primary` at runtime.
- Files: `app/src/main/java/com/jna/tictactoe/SplashScreen.kt` (lines 36–43)
- Impact: These composables are unresponsive to theme switching at runtime. When the system switches between light and dark mode while the app is running, `SplashScreen` will not update. The dark color scheme defined in `Theme.kt` is wired into `MaterialTheme` but never reaches `SplashScreen`.
- Fix approach: Replace direct constant references with `MaterialTheme.colorScheme.*` reads inside composables. The `Color.kt` constants can remain as the backing values for `lightColorScheme`/`darkColorScheme` but should not be consumed directly in UI code.

**App name in `strings.xml` is "Tictactoe", not "Zenith Grid":**
- Issue: `app/src/main/res/values/strings.xml` declares `app_name` as `"Tictactoe"`. The launcher label, `AndroidManifest.xml`, and the Android task switcher will all display "Tictactoe" while the brand identity implemented in `SplashScreen.kt` says "ZENITH GRID".
- Files: `app/src/main/res/values/strings.xml`, `app/src/main/AndroidManifest.xml`
- Fix approach: Update `app_name` to `"Zenith Grid"` (or the final product name) before any public testing.

**`isMinifyEnabled = false` in release build:**
- Issue: `app/build.gradle.kts` line 27 disables minification for the release variant. ProGuard/R8 is not configured.
- Files: `app/build.gradle.kts`, `app/proguard-rules.pro`
- Impact: Release APK/AAB will be significantly larger than necessary and will not benefit from dead-code elimination or obfuscation. This is a common default that is almost always wrong for production.
- Fix approach: Set `isMinifyEnabled = true` for the release build type and populate `proguard-rules.pro` with any keep rules needed once libraries are added.

**Compose BOM is pinned to `2024.09.00` while the project targets SDK 36:**
- Issue: `gradle/libs.versions.toml` pins `composeBom = "2024.09.00"` (September 2024). The app targets compile SDK 36 (released mid-2025) with AGP 9.1.0 and Kotlin 2.2.10, creating a significant version skew between the Compose BOM and the rest of the toolchain.
- Files: `gradle/libs.versions.toml`
- Impact: Possible incompatibilities with newer Compose features or compiler plugins. The `kotlin.compose` plugin at Kotlin 2.2.10 may not be tested against the September 2024 BOM.
- Fix approach: Upgrade Compose BOM to a 2025.x release that aligns with Kotlin 2.x and AGP 9.x.

**`ui-text-google-fonts` dependency is declared but unused:**
- Issue: `app/build.gradle.kts` lists `implementation(libs.androidx.compose.ui.text.google.fonts)` (version 1.10.6). No code in the project calls `GoogleFont` or `GoogleFont.Provider`. Fonts are loaded from local resources in `res/font/`.
- Files: `app/build.gradle.kts`, `gradle/libs.versions.toml`
- Impact: Unnecessary dependency adds APK size and unused network font-fetching infrastructure.
- Fix approach: Remove the dependency unless Google Fonts network loading is intentionally planned.

**`backup_rules.xml` and `data_extraction_rules.xml` are unconfigured boilerplate:**
- Issue: Both files are unedited Android Studio templates. `backup_rules.xml` has all rules commented out. `data_extraction_rules.xml` has a TODO comment noting that include/exclude rules need to be defined.
- Files: `app/src/main/res/xml/backup_rules.xml`, `app/src/main/res/xml/data_extraction_rules.xml`
- Impact: When the app stores user data (player profiles, scores, settings), it will be backed up to Google Drive with no filtering. Sensitive data (e.g., player names, device-specific tokens) could be unintentionally included in cloud backups.
- Fix approach: Define explicit `<include>`/`<exclude>` rules once persistent data types are known.

---

## Test Coverage Gaps

**Zero meaningful tests exist:**
- What's not tested: All application logic — because no logic has been written yet.
- Files: `app/src/test/java/com/jna/tictactoe/ExampleUnitTest.kt` (only tests `2 + 2 == 4`), `app/src/androidTest/java/com/jna/tictactoe/ExampleInstrumentedTest.kt` (only asserts package name)
- Risk: Once game logic is added (win detection, draw detection, move validation, score tracking), it will have no tests unless test infrastructure is established deliberately at that point.
- Priority: High — game logic is algorithmic and highly testable with pure unit tests.

**No Compose UI tests:**
- What's not tested: `SplashScreen` rendering, composable layout correctness, theme application.
- Files: Test infrastructure exists (`androidx.compose.ui:ui-test-junit4` is in dependencies) but no `@Composable` test functions are written.
- Risk: Layout regressions when new screens are added will go undetected.
- Priority: Medium.

---

## Architectural Risks

**All screens in root package:**
- Risk: `SplashScreen.kt` lives directly in `com.jna.tictactoe` alongside `MainActivity.kt`. As seven screens are implemented, the root package will become a flat list of screen files with no domain grouping.
- Fix approach: Establish a package structure (e.g., `com.jna.tictactoe.screen.splash`, `com.jna.tictactoe.screen.game`, `com.jna.tictactoe.screen.settings`) before adding new screens.

**No separation between UI and logic:**
- Risk: There is no `data/`, `domain/`, or `viewmodel/` package. When game logic arrives, it risks being written directly into composable functions (business logic in UI layer), which makes testing and reuse impossible.
- Fix approach: Create a `viewmodel/` package and a `GameViewModel` before implementing the game board. Treat this as a prerequisite step.

**Dark theme incompleteness:**
- Risk: `DarkColorScheme` in `Theme.kt` carries the comment "not fully specified in DESIGN.md" (line 47). Several dark-mode surface container values are approximations. Because `SplashScreen` bypasses `MaterialTheme.colorScheme`, the dark theme is effectively untested end-to-end.
- Files: `app/src/main/java/com/jna/tictactoe/ui/theme/Theme.kt`
- Fix approach: Validate dark mode against each design screen once screens are implemented. Confirm all composables read from `MaterialTheme.colorScheme` rather than direct constants.

---

## Performance Considerations

**`SplashScreen` Canvas drawing is not cached:**
- Problem: `drawZenithIcon()` runs on every recomposition frame. The function constructs a `Path` object and issues multiple `drawCircle`/`drawLine`/`drawPath` calls on each invocation.
- Files: `app/src/main/java/com/jna/tictactoe/SplashScreen.kt` (lines 151–211)
- Impact: Minor at this stage (static screen, infrequent recomposition), but the pattern of allocating objects inside `DrawScope` lambdas does not scale well. For animated screens (game board, win celebration), this will cause frame drops.
- Fix approach: Hoist the `Path` object to a `remember`-cached value or use `DrawScope`'s caching APIs when adding animation.

---

## Security Considerations

**`android:allowBackup="true"` with no backup rules:**
- Risk: `AndroidManifest.xml` enables cloud backup but `backup_rules.xml` and `data_extraction_rules.xml` have no rules defined. Any SharedPreferences, Room databases, or files the app creates in the future will be backed up automatically.
- Files: `app/src/main/AndroidManifest.xml`, `app/src/main/res/xml/backup_rules.xml`, `app/src/main/res/xml/data_extraction_rules.xml`
- Current mitigation: No user data exists yet; risk is latent.
- Recommendation: Define backup rules before implementing persistent player data.

**No ProGuard/R8 obfuscation:**
- Risk: Class names, method names, and string literals in the release build are fully readable via reverse engineering.
- Files: `app/build.gradle.kts` (`isMinifyEnabled = false`)
- Current mitigation: The app contains no sensitive logic, credentials, or proprietary algorithms at this stage.
- Recommendation: Enable minification for the release build type before any public release.

---

## Dependencies at Risk

**Compose BOM `2024.09.00` is over 18 months old relative to the Kotlin/AGP versions in use:**
- Risk: Version misalignment between the Compose BOM (September 2024) and the rest of the toolchain (Kotlin 2.2.10, AGP 9.1.0, target SDK 36) may surface as compiler warnings, deprecated API usage, or subtle runtime differences.
- Files: `gradle/libs.versions.toml`
- Impact: Low right now (only one screen, no complex Compose usage), but will compound as more Compose features are adopted.
- Migration plan: Update to the latest stable Compose BOM that is validated against Kotlin 2.2.x before adding new screens.

---

*Concerns audit: 2026-03-30*
