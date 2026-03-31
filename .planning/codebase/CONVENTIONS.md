# Conventions

## Coding Style

- **Language:** Kotlin with idiomatic usage (lambdas, data classes, extension functions)
- **UI:** Jetpack Compose — declarative, composable functions only (no XML layouts)
- **Formatting:** Standard Kotlin style; no explicit formatter config found (likely IDE-default)

## Naming Conventions

| Element | Convention | Example |
|---|---|---|
| Composables | PascalCase function | `SplashScreen()`, `TictactoeTheme()` |
| Color tokens | `Zenith`-prefixed | `ZenithPrimary`, `ZenithSurface` |
| Theme wrapper | App-name prefix | `TictactoeTheme` |
| Type styles | M3 role names | `displayLarge`, `bodyMedium` |
| Packages | lowercase dot-separated | `com.jna.tictactoe.ui.theme` |

## Compose Patterns

- **Private sub-composable decomposition:** Large screens broken into private `@Composable` helper functions in the same file
- **Modifier conventions:** `modifier: Modifier = Modifier` as last non-lambda parameter
- **Preview annotations:** `@Preview(showBackground = true)` on each composable for design-time previews
- **Import alias pattern:** Colors imported with aliases to avoid collision with Material color names

## Theming Approach

- `TictactoeTheme` wraps all content — set in `MainActivity`
- Light and dark color schemes defined explicitly in `Color.kt`
- `dynamicColor = false` by design (consistent brand colors, not system wallpaper colors)
- Full Material 3 type scale defined in `Type.kt` with `ManropeFamily` font
- Display letter-spacing set to `-0.02em` (design intent inline-commented)
- Surface container tier system used (surface, surfaceContainer, surfaceContainerHigh, etc.)

## File Organization

```
ui/theme/
  Color.kt     — all color tokens (Zenith-prefixed) + M3 role mapping
  Theme.kt     — TictactoeTheme composable, light/dark scheme selection
  Type.kt      — full M3 type scale with Manrope font family
```

- Theme files are co-located in `ui/theme/`
- Screen-level composables live at the top-level package (`com.jna.tictactoe`)
- Entry point: `MainActivity.kt` (sets theme + root composable)

## Design Intent Comments

Inline comments used to document design decisions (e.g., why `dynamicColor = false`, letter-spacing rationale in `Type.kt`).
