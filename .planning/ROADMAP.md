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
- [ ] Define typed routes (Kotlin Serializable) for all screens: `Splash`, `MainMenu`, `Lobby`, `Game`.
- [ ] Implement `NavHost` in `MainActivity`.
- [ ] Auto-advance `SplashScreen` to `MainMenu` after delay.

### 1.3 Main Menu
- [ ] Build `MainMenuScreen` with buttons for VS CPU, VS HUMAN (Local), VS LAN (Lobby).
- [ ] Implement basic state-driven UI for navigation callbacks.

---
... (rest of roadmap unchanged)
