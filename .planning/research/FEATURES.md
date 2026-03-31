# Feature Landscape

**Domain:** Android two-player casual game (Tic-Tac-Toe)
**Researched:** 2026-03-30
**Confidence note:** Based on training knowledge of Android game UX conventions, Material Design 3 patterns,
Android NSD documentation, and minimax algorithm behavior. WebSearch unavailable; flagged claims are
LOW confidence where they depend on current Play Store trends.

---

## Table Stakes

Features users expect. Missing = product feels broken or incomplete.

| Feature | Why Expected | Complexity | Notes |
|---------|--------------|------------|-------|
| 3x3 board rendered clearly, tap targets at least 48dp | Any phone-sized tap area below 48dp causes misfire frustration | Low | Material 3 minimum touch target; enforce in Compose with `Modifier.size` minimums |
| Immediate visual feedback on cell tap (piece appears at finger lift) | Lag or delayed render breaks the tactile connection between tap and result | Low | State update in ViewModel on tap; recomposition is fast enough |
| Current player indicator always visible | Players lose track of whose turn it is without a persistent indicator | Low | "X's turn" / "O's turn" label or icon highlight at top of screen |
| Win detection with win-line highlight | Players need to see _which_ cells won — not just a dialog saying "X wins" | Low | Draw a line or color the three winning cells distinctly |
| Draw detection | Without draw detection the game hangs on a full board | Low | Check after every move; show draw result immediately |
| Result dialog: winner / draw announcement + Play Again | Ending the game without an action path is a dead end | Low | Dialog with player-readable message ("X wins!" / "It's a draw") and Play Again button |
| New Match / Reset score option | Session score accumulates; players need a way to reset it | Low | Distinct from Play Again (which keeps score); a "New Match" resets score |
| Back/home navigation that doesn't silently lose game state | Android back behavior should offer confirmation before abandoning a game in progress | Low | BackHandler in Compose intercepts; show "Abandon game?" confirmation |
| Mode selection screen (vs CPU / vs Local / vs LAN) | Entry point must be clear; no hidden modes | Low | Simple menu composable |
| CPU difficulty selection before starting vs CPU game | Players choose their challenge level; forcing Hard on beginners kills engagement | Low | Three-button difficulty picker (Easy / Medium / Hard) |
| CPU makes moves without indefinite pause | A "thinking" CPU that takes 2+ seconds feels broken even if correct | Low-Med | Hard minimax on 3x3 is near-instant; no artificial delay needed unless desired for Easy/Medium |
| Score displayed on game screen (X wins / O wins / Draws) | Session stats should be glanceable during play, not hidden in menus | Low | Three counters, always visible on board screen |

---

## Differentiators

Features that set the product apart. Not expected, but make the experience feel polished.

| Feature | Value Proposition | Complexity | Notes |
|---------|-------------------|------------|-------|
| Piece placement animation (scale-in or fade-in) | Transforms a flat grid into something that feels alive | Low-Med | Compose `AnimatedVisibility` or `animateFloatAsState` on alpha/scale |
| Win line draw animation | The highlight sweeping across the winning cells creates a satisfying "aha" moment | Med | Canvas-based animated path draw or animated `Divider`-style composable |
| Win celebration effect (confetti or pulse) | Brief celebration rewards the winner without being obnoxious | Med | Lottie file or custom Canvas particle system; keep it under 1.5 seconds |
| Sound effects: piece placement, win, draw | Audio cues reinforce feedback for players not watching the screen closely | Med | Bundled raw assets via `SoundPool`; no storage permission needed |
| Distinct mute/unmute toggle | Some players are in quiet environments; forcing sound is annoying | Low | Single icon toggle in top bar; persisted in-memory (session only is fine) |
| CPU difficulty labels with plain-language descriptions | "Easy: makes random moves / Hard: unbeatable" removes confusion about what levels mean | Low | Subtitle text under each difficulty option |
| LAN: auto-discovery room list (no IP entry) | Eliminates the friction point that kills most LAN game attempts | Med | NSD + composable list of discovered hosts |
| LAN: player name entry before joining | Personalizes the opponent name shown on the board ("Alex's turn") | Low | Single text field on lobby entry screen |
| LAN: "Waiting for opponent..." lobby state with cancel | Clear loading state with an escape hatch | Low | CircularProgressIndicator + Cancel button |
| LAN: reconnect spinner with timeout | Network hiccups should not immediately end a game; attempt reconnect before giving up | Med | Coroutine retry loop with countdown; surface UI overlay, not a dialog |
| Dark/light theme support | Expected on modern Android; Material 3 dynamic color support is a bonus | Low | Already scaffolded (TictactoeTheme); verify board colors follow theme |
| Haptic feedback on piece placement | Subtle vibration reinforces the tap | Low | `HapticFeedbackType.LongPress` or `ContextCompat.getSystemService(VIBRATOR_SERVICE)` |
| Animated "thinking" indicator for Hard CPU | Even if computation is instant, a 300-400ms artificial pause with a thinking dot makes the AI feel alive without feeling slow | Low | Optional; purely cosmetic |

---

## Anti-Features

Features to explicitly NOT build. Keeping scope tight is a feature in itself.

| Anti-Feature | Why Avoid | What to Do Instead |
|--------------|-----------|-------------------|
| Manual IP / port entry for LAN | Breaks the "no friction" core value; most players don't know their local IP | Use NSD/mDNS auto-discovery exclusively |
| Internet / online multiplayer | Requires a backend, auth, matchmaking, latency handling — out of scope entirely | LAN only; document clearly in any "About" screen |
| Accounts / login / profiles | Adds identity complexity, backend dependency, privacy surface | Session-only names; no persistence beyond the session |
| Board sizes other than 3x3 | 4x4 and 5x5 require different win-detection and minimax depth tuning; out of scope | Classic 3x3 only |
| Persistent stats across app restarts | Requires SharedPreferences or database; adds complexity for marginal value | Session score resets on app restart; document this behavior in UI |
| In-app purchases or ads | Breaks the casual pick-up-and-play feel; not a monetized app | No monetization layer |
| Spectator mode | Adds a third-party connection flow and read-only game state broadcast | Players only; host/guest model |
| Replay / move history | Replay requires move recording and a separate playback composable; not needed for casual play | No replay; Play Again resets the board |
| Undo last move | In vs-CPU mode, undo favors the human unfairly; in LAN mode it requires opponent consent protocol | No undo; mistakes are part of the game |
| Tournament / bracket mode | Multi-game scheduling infrastructure; out of scope for a two-player casual app | Best-of-N via session score is sufficient |

---

## Feature Dependencies

```
Mode Selection Screen
  ├── vs CPU Mode
  │     ├── Difficulty Selection (Easy / Medium / Hard)
  │     └── Game Board
  │           ├── Core Game Logic (turn, win, draw detection)
  │           ├── CPU AI (random / heuristic / minimax)
  │           ├── Score Tracker
  │           └── Result Dialog → Play Again / New Match
  │
  ├── vs Local (Same Device) Mode
  │     └── Game Board
  │           ├── Core Game Logic
  │           ├── Score Tracker
  │           └── Result Dialog
  │
  └── vs LAN Mode
        ├── LAN Lobby Screen
        │     ├── NSD Host Registration (host path)
        │     ├── NSD Service Discovery + Room List (guest path)
        │     └── Player Name Entry
        ├── Connection Handshake
        └── Game Board
              ├── Core Game Logic (authority on host device)
              ├── Move Sync (host ↔ guest via TCP/socket)
              ├── Score Tracker
              ├── Result Dialog
              └── Disconnect Recovery (reconnect spinner → resume or abandon)

Core Game Logic ← required by all three modes; build first
Score Tracker ← depends on Core Game Logic; add after board works
Result Dialog ← depends on Score Tracker; needs Play Again + New Match actions
LAN Mode ← depends on Game Board working in Local mode first
```

---

## Game Mode Behavioral Specifications

### vs CPU Mode

**Flow:**
1. Player taps "vs CPU" on mode selection screen.
2. Difficulty picker appears (Easy / Medium / Hard) with one-line descriptions.
3. Player selects difficulty; board screen opens.
4. Human is always X and moves first (simpler; no need for side-selection in CPU mode).
5. After human moves, CPU responds. On Hard, response is near-instant (minimax depth-limited to 9
   cells); on Easy/Medium, a 300-400ms artificial delay is optional but recommended for feel.
6. Game ends on win or draw; result dialog appears.
7. Play Again: reset board, keep score, same difficulty.
8. New Match: reset board AND score, return to difficulty picker.

**CPU Difficulty Behavior:**
- Easy: picks a random empty cell. Win rate against a human playing normally is low.
- Medium: blocks an immediate human win 100% of the time; takes an immediate CPU win 100% of the time;
  otherwise random. Does not plan ahead.
- Hard: minimax with no depth limit (3x3 tree is tiny — max 9 moves, 9! = 362,880 nodes before pruning,
  alpha-beta reduces this drastically). Always plays optimally; a perfect human draws, an imperfect
  human loses.

**Expected outcome:** Hard CPU is unbeatable. Players who understand optimal Tic-Tac-Toe strategy
can always draw; everyone else will lose eventually.

---

### vs Local (Same Device) Mode

**Flow:**
1. Player taps "vs Local" on mode selection screen.
2. Board screen opens immediately — no setup required.
3. X moves first. Players alternate tapping cells, physically passing the device between turns.
4. Current player indicator is prominent (a subtle turn change animation helps, e.g., the indicator
   slides or cross-fades between "X's turn" and "O's turn").
5. Game ends; result dialog appears.
6. Play Again: reset board, keep score.
7. New Match: reset board AND score.

**Key UX concern:** No prevent-cheating mechanism is needed (this is casual). The only requirement
is that the current turn is always unambiguous.

---

### vs LAN Mode

**Discovery and Lobby Flow:**

```
Host Path:
  1. Tap "vs LAN" → choose "Host Game"
  2. Optionally enter player name (default: "Player 1")
  3. "Waiting for opponent..." screen with CircularProgressIndicator
  4. NSD service registered: _tictactoe._tcp on a random available port
  5. Guest connects → lobby shows guest name → "Start Game" button appears
  6. Host taps Start → both devices transition to board screen simultaneously

Guest Path:
  1. Tap "vs LAN" → choose "Join Game"
  2. Optionally enter player name (default: "Player 2")
  3. NSD discovery starts; list of available games populates (room name = host device name or
     host player name)
  4. Guest taps a game → connects → waits for host to start
  5. Host starts game → board screen loads
```

**During Game:**
- Host device is the authority for game state (canonical board, whose turn it is).
- Guest sends move intentions to host; host validates, updates state, broadcasts back.
- Each move triggers a state sync message over the TCP socket.
- Player names from the lobby replace "X" / "O" labels in the current-player indicator.

**Disconnect Recovery Flow:**

```
If connection is lost mid-game:
  1. Detect disconnect (socket closed / IO exception / NSD service lost).
  2. Immediately show a non-blocking overlay (not a dialog that blocks interaction):
     "Connection lost. Reconnecting... [spinner]"
  3. Attempt reconnect up to N times (recommended: 3 attempts, 3-second intervals = 9 seconds total).
  4. On success: dismiss overlay, resume game from preserved state.
  5. On failure after N attempts: show "Connection failed" dialog with two actions:
     - "Play vs CPU" (swap opponent for Easy CPU to continue the game)
     - "Exit to Menu"
  6. Never silently drop the game — always give the player a choice.
```

**NSD Service name format:** `"TicTacToe-{hostPlayerName}"` — human-readable in the discovery list.

**Port handling:** Bind to port 0 (OS assigns available port); advertise via NSD TXT record so guest
can connect without hardcoding.

**LAN assumptions:**
- Both devices on the same Wi-Fi network or direct hotspot connection.
- NSD (mDNS) works on WPA2/WPA3 home networks; enterprise networks with mDNS suppression will fail
  (out of scope; acceptable for this use case).
- IPv4 only is sufficient; no IPv6 dual-stack requirement.

---

## Result Dialog Behavioral Specification

**Trigger:** Immediately after win or draw is detected (same frame as last move).

**Content:**
- Win: "[Player name] wins!" — use player name, not "X wins" in LAN mode where names are set.
  In CPU/Local mode where no names are set, "X wins!" / "O wins!" is acceptable.
- Draw: "It's a draw!" — neutral tone.
- Win line animation should complete (or be well underway) before dialog appears. Recommended:
  300ms delay after win detection before showing dialog, to let the win animation play.

**Actions:**
- "Play Again" — reset board only; increment score was already done; same mode/difficulty.
- "New Match" — reset board AND score; return to mode selection (or difficulty picker for vs CPU).

**Dialog dismissal:** The dialog should NOT be dismissable by tapping outside or back gesture.
Force an explicit choice. A game left in limbo (result shown but no action taken) is confusing.

---

## MVP Recommendation

Build in this order — each layer is a prerequisite for the next.

**Must ship:**
1. Core game logic (turn, win, draw) — foundation for everything.
2. Board UI with piece placement and current-player indicator.
3. Win-line highlight (static color, no animation yet).
4. Result dialog with Play Again and New Match.
5. Session score tracker (three counters).
6. vs Local mode (no additional logic beyond core — just the board screen).
7. vs CPU mode with all three difficulty levels including minimax Hard.
8. Mode selection screen with difficulty picker for CPU.

**Polish layer (must ship for "polished" requirement):**
9. Piece placement animation.
10. Win line animation (sweep or fade).
11. Sound effects (placement, win, draw) via SoundPool.
12. Win celebration effect (brief, not obnoxious).
13. Haptic feedback on placement.

**LAN layer (must ship per requirements, build last):**
14. NSD host registration and guest discovery.
15. TCP socket move sync.
16. LAN lobby screens (host/join/waiting).
17. Disconnect recovery overlay.

**Defer without regret:**
- Animated CPU "thinking" indicator (cosmetic; add if time permits).
- Persistent stats (explicitly out of scope).
- Replay / undo (explicitly out of scope).

---

## Confidence Assessment

| Area | Confidence | Notes |
|------|------------|-------|
| Core game logic behavior (minimax, win detection) | HIGH | Well-established algorithms; 3x3 Tic-Tac-Toe is fully solved |
| Android UX conventions (tap targets, feedback timing) | HIGH | Material Design 3 guidelines are stable and well-documented |
| Android NSD API behavior | HIGH | API 16+; well-documented; used in production apps |
| LAN disconnect recovery UX patterns | MEDIUM | Based on general mobile game UX conventions; no Play Store survey conducted |
| Sound via SoundPool (no storage permission) | HIGH | SoundPool with raw assets does not require storage permission |
| Win celebration complexity estimate | MEDIUM | Lottie or Canvas; complexity depends on chosen approach |
| NSD mDNS suppression on enterprise Wi-Fi | HIGH | Known documented limitation; acceptable for home use case |

---

## Sources

- Material Design 3 touch target guidelines: https://m3.material.io/foundations/accessible-design/accessibility-basics
- Android NSD documentation: https://developer.android.com/training/connect-devices-wirelessly/nsd
- Android SoundPool documentation: https://developer.android.com/reference/android/media/SoundPool
- Minimax algorithm for Tic-Tac-Toe: well-established; original Newell and Simon, 1972; alpha-beta
  pruning by Knuth and Moore, 1975
- Jetpack Compose animation APIs: https://developer.android.com/develop/ui/compose/animation/overview
- Project requirements: /Users/choxxy/Projects/tictactoe/.planning/PROJECT.md
