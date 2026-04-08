# Tic-Tac-Toe

## What This Is

A polished Android Tic-Tac-Toe game with three play modes: Human vs CPU, Human vs Human on the same device, and Human vs Human over a local network. Built with Jetpack Compose and Material Design 3, targeting Android 9+.

## Core Value

Two people should be able to pick up the same device — or two nearby devices — and play a full game without any friction.

## Requirements

### Validated

- ✓ Android app scaffolding with Jetpack Compose + Material 3 — existing
- ✓ Custom Manrope font and TictactoeTheme (dark/light) — existing
- ✓ SplashScreen composable — existing
- ✓ Edge-to-edge display enabled — existing

### Active

_(All core requirements validated — see Validated section below)_

### Validated (Phase 9 Complete)

- ✓ 3×3 game board UI rendered in Compose — Validated in Phase 3
- ✓ Core game logic: turn management, win detection (rows/cols/diagonals), draw detection — Validated in Phase 2
- ✓ Human vs CPU mode with Easy / Medium / Hard difficulty (Hard = unbeatable minimax) — Validated in Phase 2
- ✓ Human vs Human local mode (same device) — Validated in Phase 3
- ✓ Human vs Human LAN mode with auto-discovery (mDNS/NSD) — Validated in Phase 4
- ✓ Session score tracker: X wins / O wins / Draws — Validated in Phase 3
- ✓ Win/draw result dialog with Play Again and New Match actions — Validated in Phase 3
- ✓ Polished UX: animations, win line highlight, sound effects, confetti — Validated in Phase 5
- ✓ LAN disconnect recovery: reconnecting spinner, resume game on success — Validated in Phase 4
- ✓ User profile with name, stats, and profile picture — Validated in Phase 9
- ✓ Profile picture: camera/gallery selection, internal storage save, DataStore persistence — Validated in Phase 9

### Out of Scope

- Internet/online multiplayer — only local LAN in scope
- Persistent stats across app restarts — session-only score tracking
- Accounts or authentication — no identity layer needed
- Board sizes other than 3×3 — classic game only
- Spectator mode — players only

## Context

- Brownfield project: app scaffolding, theme, fonts, and splash screen are already implemented
- Codebase is early-stage — no game logic, no navigation, no state management yet
- Min SDK 28 (Android 9), Target SDK 36 — NSD (Network Service Discovery) API available from API 16, safe to use
- Manrope font is bundled locally (not Google Fonts provider), consistent typography guaranteed
- ProGuard/minification currently disabled — acceptable for a game app of this scope

## Constraints

- **Tech Stack**: Kotlin + Jetpack Compose + Material 3 — all UI must be in Compose, no XML layouts
- **Android API**: Min SDK 28 — can use NSD, coroutines, all modern APIs freely
- **Networking**: LAN only via Android NSD (mDNS) — no internet permissions needed
- **State Management**: No external VM library required upfront; plain `ViewModel` + `StateFlow` sufficient
- **Audio**: Sound effects must work without requiring storage permissions (bundled assets)

## Key Decisions

| Decision | Rationale | Outcome |
|----------|-----------|---------|
| CPU: 3 difficulty levels (Easy/Medium/Hard) | Full progression — beginner to unbeatable | — Pending |
| Hard CPU uses minimax algorithm | Guarantees optimal play, well-understood for 3×3 | — Pending |
| LAN discovery via Android NSD (mDNS) | No manual IP, automatic device discovery on local network | — Pending |
| Win result: simple dialog (not full screen) | Lower complexity, still shows winner + rematch option | — Pending |
| Score tracking: session-only (no persistence) | Keeps scope tight; no database/storage needed | — Pending |
| LAN disconnect: attempt reconnect with spinner | Better UX than immediate abort; game state preserved | — Pending |

## Evolution

This document evolves at phase transitions and milestone boundaries.

**After each phase transition** (via `/gsd:transition`):
1. Requirements invalidated? → Move to Out of Scope with reason
2. Requirements validated? → Move to Validated with phase reference
3. New requirements emerged? → Add to Active
4. Decisions to log? → Add to Key Decisions
5. "What This Is" still accurate? → Update if drifted

**After each milestone** (via `/gsd:complete-milestone`):
1. Full review of all sections
2. Core Value check — still the right priority?
3. Audit Out of Scope — reasons still valid?
4. Update Context with current state

---
*Last updated: 2026-04-08 after Phase 9 (Profile Picture) completion — Milestone 5 complete*
