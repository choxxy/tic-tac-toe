# Codebase Structure

**Analysis Date:** 2026-03-30

## Directory Layout

```
tictactoe/                              # Project root
├── app/                                # Android application module
│   ├── build.gradle.kts                # App-level build config (deps, SDK versions, Compose flag)
│   ├── proguard-rules.pro              # ProGuard rules (minification disabled in current build)
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml     # App manifest (single activity, launcher intent)
│       │   ├── java/com/jna/tictactoe/ # Kotlin source root
│       │   │   ├── MainActivity.kt     # App entry point
│       │   │   ├── SplashScreen.kt     # Splash/branding screen composable
│       │   │   └── ui/
│       │   │       └── theme/          # Material 3 design system
│       │   │           ├── Color.kt    # Zenith palette color tokens
│       │   │           ├── Theme.kt    # TictactoeTheme wrapper + color schemes
│       │   │           └── Type.kt     # Manrope typography scale
│       │   └── res/
│       │       ├── drawable/           # Launcher icon vectors
│       │       ├── font/               # Manrope typeface (5 weights: light, regular, medium, semibold, bold)
│       │       ├── mipmap-*/           # Launcher icon bitmaps (mdpi → xxxhdpi + anydpi)
│       │       ├── values/
│       │       │   ├── colors.xml      # Legacy XML colors (minimal; theme uses Compose Color.kt)
│       │       │   ├── strings.xml     # App name string resource
│       │       │   ├── themes.xml      # XML theme (used for splash window background before Compose loads)
│       │       │   └── font_certs.xml  # Downloadable font certificate (not actively used)
│       │       └── xml/
│       │           ├── backup_rules.xml           # Android backup configuration
│       │           └── data_extraction_rules.xml  # Android data extraction rules
│       ├── androidTest/java/com/jna/tictactoe/
│       │   └── ExampleInstrumentedTest.kt         # Placeholder instrumented test
│       └── test/java/com/jna/tictactoe/
│           └── ExampleUnitTest.kt                 # Placeholder unit test
├── designs/                            # Design assets and specification documents
│   ├── zenith_grid/
│   │   └── DESIGN.md                  # Canonical design system specification
│   ├── splash_screen/screen.png        # Splash screen design mockup
│   ├── home_menu_with_settings_button/screen.png
│   ├── game_board/screen.png
│   ├── edit_player_profile/screen.png
│   ├── local_matchmaking_dialog/screen.png
│   ├── settings/screen.png
│   └── wi_fi_lan_lobby/screen.png
├── gradle/
│   ├── libs.versions.toml             # Version catalog — all dependency versions and aliases
│   ├── wrapper/                       # Gradle wrapper binaries
│   └── gradle-daemon-jvm.properties   # JVM settings for Gradle daemon
├── build.gradle.kts                   # Root build script (plugin declarations)
├── settings.gradle.kts                # Module inclusion and repository configuration
├── gradle.properties                  # Gradle/JVM flags (e.g., AndroidX, Kotlin code style)
├── gradlew / gradlew.bat              # Gradle wrapper scripts
├── local.properties                   # SDK path (machine-local, not committed)
├── CLAUDE.md                          # AI assistant instructions for this repo
└── .planning/codebase/                # GSD planning documents (auto-generated)
```

## Directory Purposes

**`app/src/main/java/com/jna/tictactoe/` (package root):**
- Purpose: Kotlin source for all screen-level composables and the activity
- Contains: `MainActivity.kt`, one composable per screen file (currently `SplashScreen.kt`)
- Key files: `MainActivity.kt`, `SplashScreen.kt`

**`app/src/main/java/com/jna/tictactoe/ui/theme/`:**
- Purpose: Complete Material 3 design system implementation
- Contains: Color tokens, typography scale, theme wrapper composable
- Key files: `Color.kt`, `Type.kt`, `Theme.kt`

**`app/src/main/res/font/`:**
- Purpose: Bundled Manrope typeface assets used by `Type.kt`
- Contains: `manrope_light.ttf`, `manrope_regular.ttf`, `manrope_medium.ttf`, `manrope_semibold.ttf`, `manrope_bold.ttf`

**`designs/`:**
- Purpose: Screen mockup images and the canonical design specification
- Contains: PNG screenshots per planned screen, `DESIGN.md` system spec
- Note: Not shipped in the APK; used only as implementation reference

**`gradle/libs.versions.toml`:**
- Purpose: Single source of truth for all dependency versions
- Key entries: `kotlin = "2.2.10"`, `composeBom = "2024.09.00"`, `agp = "9.1.0"`

## Key File Locations

**Entry Points:**
- `app/src/main/java/com/jna/tictactoe/MainActivity.kt`: Activity entry, theme mount, first screen call
- `app/src/main/AndroidManifest.xml`: App manifest with single `MainActivity` as launcher

**Theme/Design System:**
- `app/src/main/java/com/jna/tictactoe/ui/theme/Color.kt`: All color constants — reference this when using colors
- `app/src/main/java/com/jna/tictactoe/ui/theme/Type.kt`: `Typography` object + `ManropeFamily`
- `app/src/main/java/com/jna/tictactoe/ui/theme/Theme.kt`: `TictactoeTheme` — light/dark scheme definitions
- `designs/zenith_grid/DESIGN.md`: Visual rules, component specs, do/don't guidelines

**Screens:**
- `app/src/main/java/com/jna/tictactoe/SplashScreen.kt`: Splash/branding screen (only screen implemented)

**Build Config:**
- `gradle/libs.versions.toml`: Dependency version catalog
- `app/build.gradle.kts`: App module build file (minSdk 28, targetSdk 36, Compose enabled)

**Testing:**
- `app/src/test/java/com/jna/tictactoe/ExampleUnitTest.kt`: Unit test placeholder
- `app/src/androidTest/java/com/jna/tictactoe/ExampleInstrumentedTest.kt`: Instrumented test placeholder

## Naming Conventions

**Files:**
- Screen composables: `PascalCase` matching the composable name — e.g., `SplashScreen.kt`
- One top-level public composable per file, named identically to the file

**Directories:**
- Kotlin packages: `lowercase` (`ui/theme/`)
- Resource directories: Android conventions (`mipmap-hdpi`, `values`, `font`)

## Where to Add New Code

**New Screen:**
- Implementation: `app/src/main/java/com/jna/tictactoe/<ScreenName>.kt`
- Pattern: Public top-level `@Composable fun <ScreenName>()`, private `@Composable` sub-functions for internal sections
- Registration: Wire into navigation in `MainActivity.kt` (add `NavHost` + `NavController` when implementing navigation)
- Reference design: `designs/<screen_name>/screen.png`

**New ViewModel (when state is needed):**
- Location: `app/src/main/java/com/jna/tictactoe/<Feature>ViewModel.kt` (or a `viewmodels/` sub-package if count grows)
- Pattern: `class <Feature>ViewModel : ViewModel()` with `StateFlow`; collect in composable with `collectAsStateWithLifecycle()`

**New Theme Colors:**
- Add to: `app/src/main/java/com/jna/tictactoe/ui/theme/Color.kt` with `Zenith` prefix
- Register in: Light/dark scheme inside `Theme.kt`

**New String Resources:**
- Add to: `app/src/main/res/values/strings.xml`

**New Dependency:**
- Add version to: `gradle/libs.versions.toml` under `[versions]`
- Add library alias under `[libraries]`
- Reference in: `app/build.gradle.kts` via `libs.<alias>`

**Unit Tests:**
- Location: `app/src/test/java/com/jna/tictactoe/`
- Pattern: One test file per class/composable under test; mirroring source package structure

**Instrumented Tests:**
- Location: `app/src/androidTest/java/com/jna/tictactoe/`

## Special Directories

**`designs/`:**
- Purpose: Screen PNG mockups and design specification document
- Generated: No — hand-curated design assets
- Committed: Yes — serves as implementation reference for all developers/AI

**`.planning/codebase/`:**
- Purpose: GSD planning documents generated by `/gsd:map-codebase`
- Generated: Yes — by GSD tooling
- Committed: Yes — shared planning context

**`app/build/`:**
- Purpose: Gradle build outputs
- Generated: Yes
- Committed: No (in `.gitignore`)

---

*Structure analysis: 2026-03-30*
