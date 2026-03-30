# Phase 02: Core Game Logic - Research

**Researched:** 2026-03-30
**Domain:** Pure Kotlin Game Engine & AI
**Confidence:** HIGH

## Summary

This research establishes the foundational data structures and logic for the Tic-Tac-Toe engine and AI. Following a strict unidirectional-data-flow (UDF) pattern, the game engine and AI are implemented as pure Kotlin objects with no Android dependencies. This ensures that the core game rules, win detection, and "unbeatable" minimax AI are trivially testable with standard JVM unit tests, providing a stable foundation for the upcoming UI and networking layers.

**Primary recommendation:** Use immutable data classes for `GameState` and pure functions in `GameEngine` and `CpuPlayer` objects to ensure complete testability and predictable state management.

## Standard Stack

### Core
| Library | Version | Purpose | Why Standard |
|---------|---------|---------|--------------|
| Kotlin | 2.2.10 | Language | Current project standard |
| JUnit | 4.13.2 | Unit Testing | Standard for JVM testing in current project |

**Installation:**
No new installations required. Standard Kotlin and JUnit are already configured in `gradle/libs.versions.toml`.

## Architecture Patterns

### Recommended Project Structure
```
app/src/main/java/com/jna/tictactoe/game/
├── model/
│   ├── GameState.kt     # Immutable game state
│   ├── GameMode.kt      # VS_CPU, VS_HUMAN_LOCAL, VS_LAN
│   ├── GamePhase.kt     # PLAYING, WIN, DRAW
│   ├── Player.kt        # X, O, NONE
│   └── CellState.kt     # X, O, EMPTY
├── GameEngine.kt        # Pure functions for move validation, win detection
└── CpuPlayer.kt         # Minimax and heuristic implementations
```

### Pattern 1: Pure Function Engine
**What:** `GameEngine` is a Kotlin `object` containing only pure functions. It takes the current state and an action (move), and returns the next state.
**When to use:** All state transitions and game rule evaluations.
**Example:**
```kotlin
object GameEngine {
    fun isValidMove(state: GameState, index: Int): Boolean =
        state.phase == GamePhase.PLAYING && state.board[index] == CellState.EMPTY

    fun applyMove(state: GameState, index: Int): GameState {
        // ... returns new GameState with updated board, turn, and phase check
    }

    fun checkWin(board: List<CellState>): List<Int>? {
        // ... returns indices of winning cells if found, else null
    }
}
```

### Pattern 2: Immutable GameState
**What:** The entire game status is represented by a single immutable `data class`.
**When to use:** Used as the single source of truth in the ViewModel and as input to all engine functions.
**Why:** Prevents state drift and makes time-travel debugging/state snapshots trivial.

## Don't Hand-Roll

| Problem | Don't Build | Use Instead | Why |
|---------|-------------|-------------|-----|
| Optimal AI | Custom heuristics | Minimax | 3x3 board tree is small enough for exhaustive search; guarantees unbeatable play. |
| State Management | Manual observers | StateFlow | Official Android/Kotlin standard for UI state observation. |

## Common Pitfalls

### Pitfall 1: Mutating Board In-Place
**What goes wrong:** Representing the board as a `MutableList` and changing an element.
**Why it happens:** Natural imperative programming tendency.
**How to avoid:** Always use `board.toMutableList().also { it[i] = val }.toList()` (or similar) to emit a new list. Compose will not detect changes inside a list if the list reference remains the same.

### Pitfall 2: Missing Score Adjustment in Minimax
**What goes wrong:** Minimax picks a "long" win over an "immediate" win.
**Why it happens:** If every win scores `+10`, the AI doesn't care if it wins in 1 move or 5.
**How to avoid:** Subtract the search depth from the win score: `10 - depth`. AI will prefer shallower (faster) wins.

## Code Examples

### Win Detection Logic
```kotlin
val winLines = listOf(
    listOf(0, 1, 2), listOf(3, 4, 5), listOf(6, 7, 8), // Rows
    listOf(0, 3, 6), listOf(1, 4, 7), listOf(2, 5, 8), // Cols
    listOf(0, 4, 8), listOf(2, 4, 6)                   // Diagonals
)

fun checkWin(board: List<CellState>): List<Int>? {
    for (line in winLines) {
        val a = board[line[0]]
        val b = board[line[1]]
        val c = board[line[2]]
        if (a != CellState.EMPTY && a == b && a == c) {
            return line
        }
    }
    return null
}
```

### Minimax Implementation (Hard CPU)
```kotlin
fun minimax(board: List<CellState>, player: Player, depth: Int): Int {
    val winLine = checkWin(board)
    if (winLine != null) {
        return if (board[winLine[0]].toPlayer() == cpuPlayer) 10 - depth else depth - 10
    }
    if (board.none { it == CellState.EMPTY }) return 0

    return if (player == cpuPlayer) {
        board.indices.filter { board[it] == CellState.EMPTY }.maxOf {
            minimax(board.updated(it, cpuPlayer), humanPlayer, depth + 1)
        }
    } else {
        board.indices.filter { board[it] == CellState.EMPTY }.minOf {
            minimax(board.updated(it, humanPlayer), cpuPlayer, depth + 1)
        }
    }
}
```

### Heuristics for Medium and Easy
- **Easy:** `board.indices.filter { board[it] == CellState.EMPTY }.random()`
- **Medium:**
  1. Check for immediate CPU win -> Take it.
  2. Else check for immediate Human win -> Block it.
  3. Else pick a random empty cell.

## State of the Art

| Old Approach | Current Approach | When Changed | Impact |
|--------------|------------------|--------------|--------|
| In-place mutations | Immutable Data Classes | Modern Kotlin/UDF | Thread safety, better Compose integration |
| Manual Result Listeners | Flow / StateFlow | Coroutines era | Cleaner UI updates, lifecycle awareness |

## Validation Architecture

### Test Framework
| Property | Value |
|----------|-------|
| Framework | JUnit 4.13.2 |
| Config file | `app/build.gradle.kts` |
| Quick run command | `./gradlew testDebugUnitTest` |
| Full suite command | `./gradlew test` |

### Phase Requirements → Test Map
| Req ID | Behavior | Test Type | Automated Command | File Exists? |
|--------|----------|-----------|-------------------|-------------|
| REQ-01 | Win detection (8 lines) | unit | `./gradlew test -Ptest.filter=*GameEngineTest*` | ❌ Wave 0 |
| REQ-02 | Draw detection | unit | `./gradlew test -Ptest.filter=*GameEngineTest*` | ❌ Wave 0 |
| REQ-03 | Move validation | unit | `./gradlew test -Ptest.filter=*GameEngineTest*` | ❌ Wave 0 |
| REQ-04 | Hard AI (Unbeatable) | unit | `./gradlew test -Ptest.filter=*CpuPlayerTest*` | ❌ Wave 0 |

### Sampling Rate
- **Per task commit:** `./gradlew testDebugUnitTest --tests "com.jna.tictactoe.game.*"`
- **Per wave merge:** Full unit test suite.
- **Phase gate:** 100% coverage on GameEngine and CpuPlayer logic.

## Sources

### Primary (HIGH confidence)
- Official Android Architecture Guide (UDF/StateFlow)
- Standard Minimax algorithm theory (Classic AI)

### Secondary (MEDIUM confidence)
- JUnit 4 integration patterns for pure Kotlin in Android projects

## Metadata

**Confidence breakdown:**
- Standard stack: HIGH - Project already uses these.
- Architecture: HIGH - Follows official Android modern recommendations.
- Pitfalls: HIGH - Common documented Compose/Minimax issues.

**Research date:** 2026-03-30
**Valid until:** 2026-04-30
