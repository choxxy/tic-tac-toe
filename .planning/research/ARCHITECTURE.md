# Architecture Patterns

**Domain:** Android Tic-Tac-Toe with LAN multiplayer
**Researched:** 2026-03-30
**Confidence:** HIGH (Android official docs + established Compose/ViewModel patterns)

---

## Recommended Architecture

The app uses a single-activity, Compose-first, unidirectional-data-flow (UDF) architecture with a clear
three-layer separation: UI layer (composables), domain/logic layer (ViewModels + pure game logic), and
network layer (NSD manager + socket I/O). All state flows down from ViewModels to composables via
`StateFlow`; all user actions flow up via lambda callbacks.

```
┌─────────────────────────────────────────────────────────────────────────┐
│  MainActivity (single Activity)                                         │
│  ┌──────────────────────────────────────────────────────────────────┐   │
│  │  NavHost  (Jetpack Navigation Compose)                           │   │
│  │                                                                  │   │
│  │  splash → mainMenu → game → (result dialog overlay on game)     │   │
│  └──────────────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────────────┘

UI LAYER (Composables — observe StateFlow, emit events up)
  SplashScreen        MainMenuScreen        GameScreen
        │                   │                   │
        └───────────────────┴───────────────────┘
                            │
              (collectAsStateWithLifecycle)
                            │
DOMAIN/LOGIC LAYER (ViewModels — pure logic + orchestration)
  ┌─────────────────────────────────────────────────────────────┐
  │  GameViewModel                                              │
  │    - GameState (board, turn, phase, scores)                 │
  │    - GameEngine (win/draw detection — pure Kotlin object)   │
  │    - CpuPlayer (minimax — pure Kotlin object)               │
  │    - LanGameCoordinator (delegates to network layer)        │
  └─────────────────────────────────────────────────────────────┘

NETWORK LAYER (not a ViewModel — owned by ViewModel via delegation)
  ┌──────────────────────────┐    ┌──────────────────────────────┐
  │  NsdDiscoveryManager     │    │  GameSocketManager           │
  │  - NsdManager callbacks  │    │  - ServerSocket (host)       │
  │  - Exposes Flow<NsdEvent>│    │  - Socket (client)           │
  │  - Coroutine-wrapped     │    │  - Exposes Flow<GameMessage> │
  └──────────────────────────┘    │  - Dispatchers.IO            │
                                  └──────────────────────────────┘
```

---

## Component Boundaries

| Component | Package | Responsibility | Communicates With |
|-----------|---------|---------------|-------------------|
| `MainActivity` | root | OS entry point; mounts `TictactoeTheme` + `NavHost` | NavController |
| `NavHost` (in MainActivity) | root | Navigation graph; routes between screens | All screen composables |
| `SplashScreen` | `screen/splash` | Static branding; auto-navigates after delay | NavController (via callback) |
| `MainMenuScreen` | `screen/menu` | Mode selection (vs CPU / vs Human / vs LAN) | `MainMenuViewModel`, NavController |
| `GameScreen` | `screen/game` | Board rendering, turn indicator, score bar | `GameViewModel` |
| `ResultDialog` | `screen/game` | Win/draw overlay on GameScreen | `GameViewModel` |
| `LanLobbyScreen` | `screen/lan` | NSD discovery list; host/join selection | `LanViewModel`, NavController |
| `GameViewModel` | `viewmodel` | Single source of truth for all game state; orchestrates CPU moves and LAN message dispatch | `GameEngine`, `CpuPlayer`, `GameSocketManager` |
| `LanViewModel` | `viewmodel` | NSD lifecycle; peer list state | `NsdDiscoveryManager` |
| `GameEngine` | `game` | Pure functions: win detection, draw detection, move validation | `GameViewModel` (called directly) |
| `CpuPlayer` | `game` | Minimax algorithm at three difficulty levels | `GameViewModel` (called directly) |
| `NsdDiscoveryManager` | `network` | Wraps `NsdManager` callbacks into `Flow<NsdEvent>`; lifecycle-aware | `LanViewModel` |
| `GameSocketManager` | `network` | Owns `ServerSocket`/`Socket`; serialises/deserialises `GameMessage`; exposes `Flow<GameMessage>` | `GameViewModel` |

---

## Data Flow

### Local Game (vs CPU or vs Human same-device)

```
User taps cell
      │
GameScreen.onCellTap(index)   [UI thread, composable lambda]
      │
GameViewModel.onCellTapped(index)   [main thread, ViewModel fun]
      │
GameEngine.validateMove(state, index)   [pure, synchronous]
      │
GameEngine.applyMove(state, index) → newState
      │
GameEngine.checkWin(newState) / checkDraw(newState)
      │
_gameState.update { newState }   [MutableStateFlow update]
      │
if mode == CPU && turn == CPU turn:
  viewModelScope.launch(Dispatchers.Default) {
    val cpuMove = CpuPlayer.selectMove(newState, difficulty)
    withContext(Dispatchers.Main) {
      _gameState.update { applyMove(it, cpuMove) }
    }
  }
      │
GameScreen collects gameState via collectAsStateWithLifecycle()
      │
Compose recomposition → board redraws
```

### LAN Game (host side)

```
User taps cell
      │
GameViewModel.onCellTapped(index)
      │
GameEngine.validateMove / applyMove / checkWin
      │
_gameState.update { newState }
      │
GameSocketManager.sendMessage(MoveMessage(index))
      │   (Dispatchers.IO coroutine — non-blocking)
      │
[Opponent's device receives MoveMessage via socket]
      │
GameSocketManager.incomingMessages: Flow<GameMessage>
      │
GameViewModel collects incomingMessages in viewModelScope
      │
GameViewModel.onRemoteMove(index) → same engine path as local move
```

### LAN Discovery Flow

```
LanLobbyScreen opened
      │
LanViewModel.startDiscovery()
      │
NsdDiscoveryManager.startDiscovery()
      │   [NsdManager callbacks → callbackFlow → LanViewModel collects]
      │
LanViewModel._peers: MutableStateFlow<List<NsdServiceInfo>>
      │
LanLobbyScreen renders peer list via collectAsStateWithLifecycle()
      │
User selects peer → LanViewModel.connectToPeer(serviceInfo)
      │
NsdDiscoveryManager.resolveService(serviceInfo) → host + port
      │
GameSocketManager.connectAsClient(host, port)
      │
Navigate to GameScreen with LAN mode
```

---

## Key State Model

```kotlin
// Owned by GameViewModel; the single source of truth
data class GameState(
    val board: List<CellState>,          // 9 cells, index 0-8
    val currentTurn: Player,             // Player.X or Player.O
    val phase: GamePhase,                // PLAYING, WIN, DRAW
    val winner: Player? = null,
    val winLine: List<Int>? = null,      // indices of winning 3 cells
    val scores: ScoreState,
    val mode: GameMode                   // VS_CPU, VS_HUMAN, VS_LAN
)

enum class GamePhase { PLAYING, WIN, DRAW }
enum class Player { X, O, NONE }
enum class GameMode { VS_CPU, VS_HUMAN_LOCAL, VS_LAN }

data class ScoreState(
    val xWins: Int = 0,
    val oWins: Int = 0,
    val draws: Int = 0
)
```

The board is a flat `List<CellState>` of size 9. Index layout:

```
0 | 1 | 2
---------
3 | 4 | 5
---------
6 | 7 | 8
```

Win lines are constant: rows `[0,1,2]`, `[3,4,5]`, `[6,7,8]`; cols `[0,3,6]`, `[1,4,7]`, `[2,5,8]`; diagonals `[0,4,8]`, `[2,4,6]`. `GameEngine` checks all 8 lines on each move — O(1) for 3x3.

---

## Patterns to Follow

### Pattern 1: ViewModel + StateFlow UDF

**What:** `GameViewModel` holds a single `_gameState: MutableStateFlow<GameState>`. All mutations go through `_gameState.update { }`. The UI observes the read-only `gameState: StateFlow<GameState>`.

**When:** Every game state change — cell taps, CPU moves, remote moves, game reset, score increment.

```kotlin
class GameViewModel : ViewModel() {
    private val _gameState = MutableStateFlow(GameState.initial())
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    fun onCellTapped(index: Int) {
        val current = _gameState.value
        if (!GameEngine.isValidMove(current, index)) return
        val next = GameEngine.applyMove(current, index)
        _gameState.update { next }
        if (next.phase == GamePhase.PLAYING && next.mode == GameMode.VS_CPU) {
            scheduleCpuMove(next)
        }
    }

    private fun scheduleCpuMove(state: GameState) {
        viewModelScope.launch(Dispatchers.Default) {
            val move = CpuPlayer.selectMove(state, difficulty)
            val afterCpu = GameEngine.applyMove(state, move)
            _gameState.update { afterCpu }
        }
    }
}
```

**Why:** Single source of truth, survives configuration changes, trivially testable (mutate state, assert new state).

### Pattern 2: NSD Callbacks Wrapped in callbackFlow

**What:** `NsdDiscoveryManager` wraps `NsdManager`'s listener callbacks in a `callbackFlow`. This bridges the callback-based Android API to Kotlin coroutines/`Flow` cleanly.

**When:** Service registration, discovery start/stop, and peer resolution.

```kotlin
fun discoverPeers(): Flow<NsdEvent> = callbackFlow {
    val listener = object : NsdManager.DiscoveryListener {
        override fun onServiceFound(service: NsdServiceInfo) {
            trySend(NsdEvent.PeerFound(service))
        }
        override fun onServiceLost(service: NsdServiceInfo) {
            trySend(NsdEvent.PeerLost(service))
        }
        override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) {
            close(NsdException("Discovery failed: $errorCode"))
        }
        // ... other required overrides
    }
    nsdManager.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, listener)
    awaitClose { nsdManager.stopServiceDiscovery(listener) }
}
```

**Why:** `awaitClose` guarantees the NSD listener is always stopped when the collector cancels — prevents battery drain and ghost discovery sessions. `trySend` is safe to call from any thread (NSD callbacks are off-main-thread).

### Pattern 3: Socket I/O on Dispatchers.IO

**What:** `GameSocketManager` uses a dedicated `Dispatchers.IO` coroutine for blocking socket reads and another for writes.

**When:** All socket operations — connecting, reading incoming messages, writing outgoing messages.

```kotlin
class GameSocketManager {
    private val _incoming = MutableSharedFlow<GameMessage>()
    val incoming: SharedFlow<GameMessage> = _incoming.asSharedFlow()

    fun startReading(socket: Socket, scope: CoroutineScope) {
        scope.launch(Dispatchers.IO) {
            val reader = BufferedReader(InputStreamReader(socket.inputStream))
            try {
                while (isActive) {
                    val line = reader.readLine() ?: break
                    val message = GameMessage.deserialize(line)
                    _incoming.emit(message)
                }
            } catch (e: IOException) {
                _incoming.emit(GameMessage.Disconnected)
            }
        }
    }
}
```

**Why:** `Socket.readLine()` is blocking — it must not run on the main thread. `Dispatchers.IO` is designed for blocking I/O with an expandable thread pool.

### Pattern 4: Navigation via Callbacks, Not Direct NavController Passing

**What:** Screen composables receive `() -> Unit` callbacks for navigation. `NavController` stays in `MainActivity`/the composable that owns `NavHost`.

**When:** All navigation triggers — splash auto-advance, mode selection, post-game navigation.

```kotlin
// In NavHost (MainActivity or root composable)
composable<MainMenu> {
    MainMenuScreen(
        onVsCpuSelected = { navController.navigate(Game(mode = GameMode.VS_CPU)) },
        onVsLanSelected = { navController.navigate(LanLobby) }
    )
}

// Screen composable — no NavController reference
@Composable
fun MainMenuScreen(
    onVsCpuSelected: () -> Unit,
    onVsLanSelected: () -> Unit,
    viewModel: MainMenuViewModel = viewModel()
) { ... }
```

**Why:** Composables are easier to preview and test without `NavController`. Keeps navigation logic in one place.

### Pattern 5: GameEngine as Pure Object (No Android Dependencies)

**What:** `GameEngine` and `CpuPlayer` are plain Kotlin objects with pure functions — no `Context`, no coroutines, no Android imports.

**When:** Win/draw detection, move validation, minimax calculation.

```kotlin
object GameEngine {
    fun isValidMove(state: GameState, index: Int): Boolean =
        state.phase == GamePhase.PLAYING && state.board[index] == CellState.EMPTY

    fun applyMove(state: GameState, index: Int): GameState { ... }
    fun checkWin(board: List<CellState>): List<Int>? { ... }
    fun checkDraw(board: List<CellState>): Boolean { ... }
}
```

**Why:** Pure functions are trivially unit-testable in JVM tests (`app/src/test/`) — no emulator, no Android framework. This is the highest-value testing investment in the project.

---

## Anti-Patterns to Avoid

### Anti-Pattern 1: Business Logic in Composables

**What:** Placing win detection, move validation, or score logic inside `@Composable` functions.

**Why bad:** Logic runs during recomposition (uncontrolled timing), cannot be unit tested without a Compose test harness, and breaks when composables are reconstructed.

**Instead:** All logic lives in `GameViewModel` or `GameEngine`. Composables are pure rendering functions of `GameState`.

### Anti-Pattern 2: Accessing NsdManager or Socket Directly from UI

**What:** Starting NSD discovery or creating sockets from a composable or activity lifecycle callback.

**Why bad:** Network operations triggered by UI lifecycle (rather than app lifecycle) will be destroyed on configuration changes (screen rotation). State is lost.

**Instead:** `NsdDiscoveryManager` and `GameSocketManager` are owned by `LanViewModel`/`GameViewModel`. ViewModels survive configuration changes; sockets do not need to be recreated on rotation.

### Anti-Pattern 3: Blocking the Main Thread with Socket Reads

**What:** Calling `Socket.readLine()` or any blocking I/O on `Dispatchers.Main` or inside a composable.

**Why bad:** Causes ANR (Application Not Responding) if the read blocks for more than ~5 seconds.

**Instead:** All socket reads run on `Dispatchers.IO`. Results are emitted via `Flow`/`SharedFlow` and collected on the main thread via `viewModelScope`.

### Anti-Pattern 4: One Listener Instance Reused for Multiple NSD Operations

**What:** Passing the same `DiscoveryListener` to both `discoverServices()` and `stopServiceDiscovery()` calls across multiple sessions.

**Why bad:** `NsdManager` throws `IllegalArgumentException` if you attempt to register a listener that is already registered. Each discovery session must use a fresh listener instance.

**Instead:** Create listener instances inside the `callbackFlow` builder (scoped to each `collect` call). `awaitClose` calls `stopServiceDiscovery` with the same instance.

### Anti-Pattern 5: Passing ViewModel Instances to Composables

**What:** `GameScreen(viewModel = gameViewModel)` — composable takes `ViewModel` as a parameter.

**Why bad:** Makes composables hard to preview (need real ViewModel), breaks screenshot testing, and couples composable to ViewModel lifecycle.

**Instead:** Extract the UI state and event lambdas from the ViewModel at the screen level, pass them down as plain data + lambdas.

---

## Package Structure

```
com.jna.tictactoe/
├── MainActivity.kt                  (navigation host)
│
├── screen/
│   ├── splash/
│   │   └── SplashScreen.kt
│   ├── menu/
│   │   ├── MainMenuScreen.kt
│   │   └── MainMenuViewModel.kt
│   ├── game/
│   │   ├── GameScreen.kt
│   │   ├── GameViewModel.kt
│   │   └── ResultDialog.kt
│   └── lan/
│       ├── LanLobbyScreen.kt
│       └── LanViewModel.kt
│
├── game/                            (pure Kotlin — no Android imports)
│   ├── GameEngine.kt
│   ├── CpuPlayer.kt
│   └── model/
│       ├── GameState.kt
│       ├── GameMode.kt
│       ├── GamePhase.kt
│       └── Player.kt
│
├── network/
│   ├── NsdDiscoveryManager.kt
│   ├── GameSocketManager.kt
│   └── model/
│       └── GameMessage.kt           (sealed class for move/ping/disconnect)
│
└── ui/
    └── theme/
        ├── Color.kt
        ├── Theme.kt
        └── Type.kt
```

---

## Thread Model

| Thread | What Runs There | Mechanism |
|--------|----------------|-----------|
| Main (UI) thread | Composable recomposition, `_gameState.update {}`, ViewModel methods | Default for coroutines in `viewModelScope` |
| `Dispatchers.Default` | Minimax computation (`CpuPlayer.selectMove`) — CPU-bound | `viewModelScope.launch(Dispatchers.Default)` |
| `Dispatchers.IO` | Socket reads/writes, `ServerSocket` accept loop | `viewModelScope.launch(Dispatchers.IO)` in GameSocketManager |
| NSD callback thread | `NsdManager` callbacks (OS-managed, not main thread) | `callbackFlow` + `trySend` bridges to collectors |

Key constraint: `_gameState.update {}` is thread-safe (`MutableStateFlow.update` uses atomic compare-and-set). CPU moves and network moves can post updates from background threads safely.

---

## LAN Protocol

Use newline-delimited JSON over a plain TCP socket. The protocol is minimal:

```
// GameMessage sealed class — serialised as JSON lines
sealed class GameMessage {
    data class Move(val index: Int) : GameMessage()
    data class Rematch(val accepted: Boolean) : GameMessage()
    object Ping : GameMessage()
    object Disconnected : GameMessage()
}

// Wire format examples:
{"type":"move","index":4}
{"type":"rematch","accepted":true}
{"type":"ping"}
```

The host opens `ServerSocket(0)` (OS picks free port) and registers the port with NSD. The client connects via the resolved `InetAddress` + port. All messages are line-delimited so a `BufferedReader.readLine()` loop is the complete read loop.

---

## Suggested Build Order

Dependencies determine this order. Each item must be complete before the next can start.

```
Phase 1 — Foundation (no prerequisites)
  1a. Navigation setup: NavHost in MainActivity, typed routes, SplashScreen auto-advance
  1b. Package structure: create screen/, game/, network/ packages

Phase 2 — Core Game Logic (prerequisite: nothing — pure Kotlin)
  2a. GameState model, Player, GamePhase, GameMode data classes
  2b. GameEngine: move validation, win detection, draw detection (unit-test immediately)
  2c. CpuPlayer: Easy (random), Medium (random + block wins), Hard (full minimax)
         → minimax is pure and testable; write tests before GameScreen exists

Phase 3 — Game UI (prerequisite: Phase 1 navigation + Phase 2 logic)
  3a. GameViewModel: wraps GameEngine, exposes StateFlow<GameState>
  3b. GameScreen: board grid, turn indicator, cell tap → ViewModel call
  3c. ResultDialog: win/draw overlay, Play Again / New Match actions
  3d. MainMenuScreen: mode selection (VS_CPU, VS_HUMAN, VS_LAN placeholder)
  3e. Score tracking in GameViewModel (session-only, no persistence)

Phase 4 — LAN Multiplayer (prerequisite: Phase 3 GameViewModel)
  4a. GameSocketManager: ServerSocket host path + Socket client path, Flow<GameMessage>
  4b. NsdDiscoveryManager: callbackFlow wrapping NsdManager callbacks
  4c. LanViewModel: orchestrates NSD discovery, peer list state
  4d. LanLobbyScreen: peer list UI, host/join selection
  4e. GameViewModel LAN integration: inject GameSocketManager, handle incoming Move messages,
      send outgoing Move messages after local move is applied
  4f. Disconnect recovery: spinner overlay state, reconnect attempt loop in GameViewModel

Phase 5 — Polish (prerequisite: all gameplay working end-to-end)
  5a. Animations: cell placement spring animation, win line draw animation
  5b. Sound effects: MediaPlayer with bundled assets in res/raw/ (no storage permission)
  5c. Win celebration: particle/confetti overlay on GameScreen
  5d. Dark mode validation: confirm all screens use MaterialTheme.colorScheme, not direct constants
```

**Why this order:**
- Game logic (Phase 2) has zero dependencies — build and fully test it before any UI exists. This is the highest-ROI testing investment.
- The ViewModel (Phase 3a) can only be built after the model exists (Phase 2a) and navigation exists (Phase 1) so screens can be reached.
- LAN (Phase 4) requires a working `GameViewModel` because the socket layer feeds into it. Building sockets before the game state machine is ready produces untestable integration points.
- Polish (Phase 5) is last because animations and sound layered on top of broken state machines compound debugging difficulty enormously.

---

## Scalability Considerations

This is a 2-player, fixed 3x3 board game. Scalability concerns are intentionally minimal by domain. The relevant operational limits are:

| Concern | At launch | Notes |
|---------|-----------|-------|
| Concurrent game sessions | 1 (this device) | Single `GameViewModel` instance per navigation destination |
| LAN peers | 1 opponent | `NsdDiscoveryManager` can show a list but `GameSocketManager` opens exactly one connection |
| Board state complexity | O(1) | 9 cells, 8 win lines, all evaluations are constant-time |
| Minimax depth | 9 moves max | 3x3 board; full search tree is ~255k nodes; completes in <1ms even without alpha-beta pruning |
| Socket message volume | ~9 messages per game | One `Move` message per cell tap; negligible bandwidth |

---

## Sources

- Android Developers — Use Network Service Discovery: https://developer.android.com/develop/connectivity/wifi/use-nsd (MEDIUM confidence — official docs, callback teardown patterns verified)
- Android Architecture Guide — UI Layer: https://developer.android.com/topic/architecture/ui-layer (HIGH confidence — official recommendation)
- Android Architecture Guide — State Holders: https://developer.android.com/topic/architecture/ui-layer/stateholders (HIGH confidence — official recommendation)
- Android Architecture Recommendations: https://developer.android.com/topic/architecture/recommendations (HIGH confidence — official recommendation)
- Jetpack Navigation Compose: https://developer.android.com/guide/navigation/design (HIGH confidence — official recommendation)
- `callbackFlow` for NSD wrapping: MEDIUM confidence (established Kotlin coroutines pattern, widely used in community, not explicitly in official NSD docs)
- Minimax for 3x3 Tic-Tac-Toe: HIGH confidence — well-established algorithm, no external source required

---

*Architecture research: 2026-03-30*
