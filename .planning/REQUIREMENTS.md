# Requirements: Zenith Grid (Tic-Tac-Toe)

## Overview
A polished, modern Android Tic-Tac-Toe game focusing on seamless local play (same device) and low-friction LAN multiplayer. The app emphasizes visual quality, "unbeatable" AI, and zero-config networking.

## Target Audience
- Casual gamers looking for a quick, high-quality classic game.
- Pairs of players in physical proximity (same room/Wi-Fi).
- Android users who appreciate Material 3 aesthetics and fluid animations.

---

## Functional Requirements

### 1. Game Core
- **Board:** Classic 3x3 grid.
- **Rules:** Standard Tic-Tac-Toe (3 in a row/column/diagonal wins).
- **Turn Management:** Strictly enforced alternating turns (X then O).
- **Win Detection:** Immediate detection of row, column, or diagonal victory.
- **Draw Detection:** Detection of full board with no winner.
- **Reset:** "Play Again" (keeps session score) and "New Match" (resets score) actions.

### 2. Game Modes
- **Human vs CPU:**
  - Easy: Random move selection.
  - Medium: Heuristic-based (blocks immediate wins, takes immediate wins, otherwise random).
  - Hard: Minimax-based optimal play (unbeatable).
- **Human vs Human (Local):** Same-device play where players alternate taps.
- **Human vs Human (LAN):**
  - Auto-discovery via Android NSD (mDNS) — no IP entry.
  - One player acts as Host, the other as Guest.
  - Real-time move synchronization over TCP sockets.
  - Disconnect recovery: 9-second reconnection window with spinner overlay before offering fallback to CPU or exit.

### 3. Session Management
- **Score Tracking:** Track X wins, O wins, and Draws within the current app session.
- **Persistence:** Scores reset when the app is fully closed (no database persistence required).

### 4. User Interface & UX
- **Navigation:** Single Activity with Jetpack Navigation (Splash → Menu → Lobby → Game).
- **Branding:** Modern Manrope typography and TictactoeTheme (Material 3).
- **Feedback:**
  - Piece placement animations (scale/fade).
  - Win line drawing animation.
  - Haptic feedback on piece placement.
  - Sound effects for placement, win, and draw (bundled assets).
- **Celebration:** Brief win celebration (particles or pulse) before showing result dialog.

---

## Non-Functional Requirements

### 1. Performance
- **Responsiveness:** Cell taps must render feedback in <100ms.
- **AI Latency:** CPU moves (even Hard minimax) must be calculated in <500ms (near-instant on 3x3).
- **Frame Rate:** Maintain 60fps during animations (no allocations in `DrawScope`).

### 2. Networking
- **Discovery:** Peers must be discovered within 2-5 seconds of entering the lobby.
- **Latency:** Move synchronization should feel near-instant on a standard Wi-Fi network.

### 3. Usability
- **Touch Targets:** Minimum 48dp for all interactive elements.
- **Dark Mode:** Full support for dark/light themes via MaterialTheme.

---

## Constraints & Assumptions
- **Platform:** Android 9.0 (API 28) minimum.
- **Connectivity:** LAN multiplayer requires both devices on the same Wi-Fi or hotspot.
- **Hardware:** Physical device required for full LAN validation (emulator NSD support is inconsistent).
- **Permissions:** `INTERNET` permission required for TCP/NSD. No runtime permissions (Location/Storage) required.

---

## Future Scope (Out of Scope)
- Internet/Online multiplayer (Global matchmaking).
- Persistent player profiles or global leaderboards.
- 4x4 or 5x5 board variations.
- Undo/Redo move functionality.
- Custom piece skins or themes beyond Dark/Light.
