# Phase 3: Game UI & Interaction - Research

**Researched:** 2025-02-17
**Domain:** Jetpack Compose UI, ViewModel, Unidirectional Data Flow (UDF)
**Confidence:** HIGH

## Summary

This phase focuses on the visual and interactive core of Zenith Grid. It implements the "Architectural Serenity" design system for the 3x3 game board, where traditional grid lines are replaced by tonal depth shifts using Material 3 `surface-container` tiers. The interaction model follows strict Unidirectional Data Flow (UDF) using `MutableStateFlow` in a `ViewModel`, integrating the Phase 2 `GameEngine` and `CpuPlayer`.

**Primary recommendation:** Use `LazyVerticalGrid` or nested `Row`/`Column` with `Arrangement.spacedBy` to create the "no-line" grid. The board background should be `surface-container-high`, and cells `surface-container-lowest`. Leverage `viewModelScope` with a 600ms-800ms delay for CPU turns to maintain the "serene" feel.

## User Constraints (from CONTEXT.md)

*None provided. Following instructions from the phase prompt.*

## Standard Stack

### Core
| Library | Version | Purpose | Why Standard |
|---------|---------|---------|--------------|
| Jetpack Compose | BOM 2024.09.00 | UI Framework | Official Android declarative UI. |
| ViewModel | 2.10.0 | State Management | Survives configuration changes, manages UDF. |
| Kotlin Coroutines | 1.9.0 | Asynchrony | Non-blocking CPU turns and delays. |
| StateFlow/SharedFlow | 1.9.0 | Reactive Streams | Native Kotlin alternative to LiveData. |

### Supporting
| Library | Version | Purpose | When to Use |
|---------|---------|---------|-------------|
| Navigation Compose | 2.8.8 | Screen Navigation | Handling transitions between Menu and Game. |
| Material 3 | 1.3.0 | Theming | Utilizing elevated surface tokens and typography. |

**Installation:**
```bash
# All dependencies are already present in build.gradle.kts
```

## Architecture Patterns

### Recommended Project Structure
```
app/src/main/java/com/jna/tictactoe/
├── screen/
│   └── game/
│       ├── GameScreen.kt       # Composable UI
│       ├── GameViewModel.kt    # Logic and State
│       ├── GameUiState.kt      # Data model for UI
│       └── components/         # Game-specific components
│           ├── GameBoard.kt
│           ├── GameCell.kt
│           ├── ScoreBoard.kt
│           └── ResultDialog.kt
```

### Pattern 1: Unidirectional Data Flow (UDF)
**What:** The View emits events to the ViewModel, which updates a single StateFlow. The View observes this StateFlow and renders accordingly.
**When to use:** All Compose screens.
**Example:**
```typescript
// Pattern: Event -> ViewModel -> State -> UI
class GameViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(GameUiState())
    val uiState = _uiState.asStateFlow()

    fun handleIntent(intent: GameIntent) {
        when (intent) {
            is GameIntent.CellClicked -> makeMove(intent.index)
            is GameIntent.ResetGame -> reset()
        }
    }
}
```

### Anti-Patterns to Avoid
- **State in Composables:** Avoid `var` inside `@Composable` unless it's strictly transient (like animation state). Use ViewModel for game logic.
- **Blocking the Main Thread:** Never call `CpuPlayer.getMove` (especially Hard difficulty) on the main thread; use `Dispatchers.Default`.
- **Hardcoded Colors:** Never use hex codes in UI. Always use `MaterialTheme.colorScheme.surfaceContainerHigh`, etc.

## Don't Hand-Roll

| Problem | Don't Build | Use Instead | Why |
|---------|-------------|-------------|-----|
| Dialogs | Custom overlay logic | `androidx.compose.ui.window.Dialog` | Handles back press, accessibility, and focus. |
| Grid Layout | Custom `Canvas` grid | `LazyVerticalGrid` or `spacedBy` | Easier to manage click targets and responsive sizing. |
| Delays | `Thread.sleep` | `delay()` | Non-blocking coroutine suspension. |

## Runtime State Inventory

*N/A - Greenfield UI implementation.*

## Common Pitfalls

### Pitfall 1: CPU "Teleportation"
**What goes wrong:** CPU makes a move instantly after the player, making the game feel mechanical and stressful.
**Why it happens:** Logic runs faster than human perception.
**How to avoid:** Use a minimum `delay(600)` in the `viewModelScope` before applying the CPU move.

### Pitfall 2: Double-Click Race Conditions
**What goes wrong:** Player clicks two cells very quickly, or clicks during CPU's thinking time.
**Why it happens:** State updates haven't propagated or move validation is missing.
**How to avoid:** Always check `isValidMove` in the ViewModel and disable click handling in the UI if `phase != PLAYING`.

### Pitfall 3: Navigation State Loss
**What goes wrong:** Navigating to Settings and back resets the session score.
**Why it happens:** ViewModel is scoped to the screen and gets cleared when navigating away.
**How to avoid:** Scope the `GameViewModel` to the `Activity` or the `NavGraph` to persist the "Session Score".

## Code Examples

### Architectural Serenity Grid (No Lines)
```kotlin
// Source: Project DESIGN.md + Compose Best Practices
@Composable
fun GameBoard(board: List<CellState>, onCellClick: (Int) -> Unit) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh) // Level 3
            .padding(12.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            repeat(3) { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    repeat(3) { col ->
                        val index = row * 3 + col
                        GameCell(
                            state = board[index],
                            onClick = { onCellClick(index) },
                            modifier = Modifier.weight(1f).aspectRatio(1f)
                        )
                    }
                }
            }
        }
    }
}
```

### Glassmorphism Dialog
```kotlin
// Source: Android 12+ Modifier.blur with fallback
@Composable
fun ResultDialog(onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = 0.7f))
                .then(
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        Modifier.blur(20.dp)
                    } else Modifier
                )
                .padding(24.dp)
        ) {
            // Content
        }
    }
}
```

## State of the Art

| Old Approach | Current Approach | When Changed | Impact |
|--------------|------------------|--------------|--------|
| LiveData | StateFlow | 2021+ | Native Kotlin support, more consistent with Coroutines. |
| XML + Canvas | Jetpack Compose | 2021 | Declarative UI, much faster iteration. |
| 1px Borders | Surface Tiers | Material 3 | Modern, cleaner aesthetics using light/depth instead of lines. |

## Open Questions

1. **CPU Thinking Indicator?**
   - What we know: Design system is "Serene".
   - What's unclear: Should we show a small "Thinking..." indicator during the 600ms delay?
   - Recommendation: Start without one to keep it clean. Add a subtle opacity change to the board if needed later.

2. **Winning Line Visuals?**
   - What we know: No lines are allowed.
   - What's unclear: How to highlight the winning 3 cells.
   - Recommendation: Use a "glowing" shadow or background shift to the player's primary/secondary color at low opacity for the winning cells.

## Environment Availability

| Dependency | Required By | Available | Version | Fallback |
|------------|------------|-----------|---------|----------|
| Android SDK 36 | App Compile | ✓ | 36 | — |
| Compose BOM | UI | ✓ | 2024.09.00 | — |
| Gradle | Build | ✓ | 9.3.1 | — |
| JDK | Runtime | ✓ | 21 | — |

## Validation Architecture

### Test Framework
| Property | Value |
|----------|-------|
| Framework | JUnit 4 + Compose Test Rule |
| Config file | build.gradle.kts |
| Quick run command | `./gradlew test` |
| Full suite command | `./gradlew connectedAndroidTest` |

### Phase Requirements → Test Map
| Req ID | Behavior | Test Type | Automated Command | File Exists? |
|--------|----------|-----------|-------------------|-------------|
| UI-01 | 3x3 Grid No-Line Rule | Screenshot | Manual Verification | ❌ Wave 0 |
| INT-01 | Cell tap updates Board | Unit | `./gradlew test` | ❌ Wave 0 |
| CPU-01 | CPU turn delay (600ms+) | Unit/Integration | `./gradlew test` | ❌ Wave 0 |
| SES-01 | Score survives navigation | E2E | `./gradlew connectedCheck` | ❌ Wave 0 |

### Wave 0 Gaps
- [ ] `app/src/test/java/com/jna/tictactoe/screen/game/GameViewModelTest.kt` — covers INT-01, CPU-01
- [ ] `app/src/androidTest/java/com/jna/tictactoe/screen/game/GameScreenTest.kt` — covers UI-01

## Sources

### Primary (HIGH confidence)
- `designs/zenith_grid/DESIGN.md` - Design system rules.
- `app/src/main/java/com/jna/tictactoe/ui/theme/Color.kt` - Token availability.
- Official Android Documentation - Compose UDF, ViewModel, and Dialogs.

### Secondary (MEDIUM confidence)
- Chris Banes / Haze library documentation - For glassmorphism patterns.

## Metadata

**Confidence breakdown:**
- Standard stack: HIGH - Current versions verified in libs.versions.toml.
- Architecture: HIGH - Strictly follows design docs.
- Pitfalls: HIGH - Common Android game dev challenges.

**Research date:** 2025-02-17
**Valid until:** 2025-03-19
