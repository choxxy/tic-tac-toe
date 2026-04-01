# Roadmap: Zenith Grid (Tic-Tac-Toe)

## Goal
Build a polished, "unbeatable" Tic-Tac-Toe app with local and LAN multiplayer, using modern Android practices (Compose, UDF, Coroutines).

---

## Phase 1: Foundation & Navigation
**Prerequisite:** Existing project scaffolding (theme, splash).
**Goal:** Establish clean navigation and architectural structure.
**Plans:** 5 plans

Plans:
- [ ] 01-00-PLAN.md — Create navigation UI test scaffold.
- [ ] 01-01-PLAN.md — Configure dependencies and architectural packages.
- [ ] 01-02-PLAN.md — Refactor existing files and define type-safe routes.
- [ ] 01-03-PLAN.md — Implement NavHost, Main Menu, and Splash logic.
- [ ] 01-04-PLAN.md — Verify navigation flow and backstack management.

### 1.1 Infrastructure Setup
- [x] Add `lifecycle-viewmodel-compose`, `lifecycle-runtime-compose`, and `navigation-compose` to `libs.versions.toml` (use existing `lifecycleRuntimeKtx` and stable navigation version).
- [x] Establish package structure: `screen/`, `game/`, `network/`, `ui/`.

### 1.2 Typed Navigation
- [x] Define typed routes (Kotlin Serializable) for all screens: `Splash`, `MainMenu`, `Lobby`, `Game`.
- [x] Implement `NavHost` in `MainActivity`.
- [x] Auto-advance `SplashScreen` to `MainMenu` after delay.

### 1.3 Main Menu
- [x] Build `MainMenuScreen` with buttons for VS CPU, VS HUMAN (Local), VS LAN (Lobby).
- [x] Implement basic state-driven UI for navigation callbacks.

---

## Phase 2: Core Game Logic (The Engine)
**Prerequisite:** None (Pure Kotlin logic).
**Goal:** Fully testable, pure-Kotlin game state machine.
**Plans:** 3 plans

Plans:
- [x] 02-00-PLAN.md — Create core engine and AI test stubs.
- [x] 02-01-PLAN.md — Define models and core GameEngine logic.
- [x] 02-02-PLAN.md — Implement Easy, Medium, and Hard (Minimax) CPU AI.

### 2.1 State Models
- [x] Define `GameState`, `Player`, `CellState`, `GamePhase`, `GameMode` data classes/enums.
- [x] Ensure `GameState` is immutable and replaces entire board on move.

### 2.2 Game Engine
- [x] Implement move validation (index bounds, empty cell check).
- [x] Implement win detection (8 possible lines).
- [x] Implement draw detection (all cells filled, no winner).
- [x] **Validation:** 100% unit test coverage for engine logic.

### 2.3 CPU AI (Minimax)
- [x] Implement `Easy` strategy (random).
- [x] Implement `Medium` strategy (heuristic: block win, take win, else random).
- [x] Implement `Hard` strategy (unbeatable minimax algorithm).
- [x] **Validation:** Unit tests for AI scenarios (forced win, forced block).

---

## Phase 3: Game UI & Interaction
**Prerequisite:** Phase 1 (Navigation) & Phase 2 (Logic).
**Goal:** Functional single-device gameplay with local and CPU modes.
**Plans:** 3 plans

Plans:
- [ ] 03-00-PLAN.md — Infrastructure & Test Stubs.
- [ ] 03-01-PLAN.md — Game ViewModel (UDF, AI Turn Delay).
- [ ] 03-02-PLAN.md — Game Board UI & Result Overlay (Compose).

### 3.1 Game ViewModel
- [ ] Implement `GameViewModel` with `MutableStateFlow<GameState>`.
- [ ] Integrate `GameEngine` and `CpuPlayer` for state transitions.
- [ ] Add session score tracking (survives screen navigation, resets on New Match).

### 3.2 Game Board Screen
- [ ] Render 3x3 grid using Jetpack Compose (LazyVerticalGrid or Column/Row).
- [ ] Implement cell tap handling → ViewModel update.
- [ ] Current player turn indicator ("X's Turn" / "O's Turn").
- [ ] Session score display (X wins / O wins / Draws).

### 3.3 Result Overlay
- [ ] Build `ResultDialog` (Material 3) for Win/Draw outcomes.
- [ ] Actions: "Play Again" (reset board) and "New Match" (return to menu, reset score).

---

## Phase 4: LAN Multiplayer
**Prerequisite:** Phase 3 (Working Local Game).
**Goal:** Low-friction peer-to-peer networking.
**Plans:** 4 plans

Plans:
- [ ] 04-01-PLAN.md — Networking Infrastructure (NSD & Sockets).
- [ ] 04-02-PLAN.md — LAN Lobby & Handshake UI.
- [ ] 04-03-PLAN.md — LAN Game Integration & Move Sync.
- [ ] 04-04-PLAN.md — Disconnect Recovery & Resiliency.

### 4.1 Networking Infrastructure
- [ ] Build `NsdDiscoveryManager` wrapping `NsdManager` callbacks in `callbackFlow`.
- [ ] Build `GameSocketManager` for TCP `ServerSocket` (host) and `Socket` (client).
- [ ] Define `GameMessage` sealed class for JSON-delimited communication.

### 4.2 LAN Lobby
- [ ] Build `LanLobbyScreen` for Host vs Join selection.
- [ ] Implement discovery list showing active hosts on network.
- [ ] Handle connection handshake and player name exchange.

### 4.3 LAN Game Integration
- [ ] Synchronize board state between devices (Host as authority).
- [ ] Implement move sync (Guest move → Host → Validation → Broadcast).
- [ ] Disconnect recovery: 9-second timeout with reconnect spinner overlay.

---

## Phase 5: Polish & Aesthetics
**Prerequisite:** Phase 4 (Functional Networking).
**Goal:** Transform functional game into a premium experience.

### 5.1 Animations
- [ ] Piece placement animation (Scale + Fade-in).
- [ ] Win line drawing animation (Animated Canvas path).
- [ ] Result dialog entrance animation (Slide + Fade).

### 5.2 Audio & Haptics
- [ ] Implement `SoundManager` using `SoundPool` for sub-10ms latency.
- [ ] Add sound effects for placement, win, and draw.
- [ ] Integrate haptic feedback on piece placement.

### 5.3 Final Polish
- [ ] Dark Mode audit (ensure zero hard-coded color constants).
- [ ] Confetti/Particle effect on win celebration.
- [ ] "About" screen with credits and technical details (optional).

---

## Success Criteria
1. [ ] **Unbeatable:** "Hard" CPU never loses a game in automated testing.
2. [ ] **Low Friction:** LAN game connection starts within 3 taps and <5 seconds discovery.
3. [ ] **Stability:** No crashes on rotation or sudden LAN disconnect.
4. [ ] **Quality:** 60fps animations on mid-range Android 9+ devices.
