---
phase: 02
slug: core-game-logic
status: draft
nyquist_compliant: false
wave_0_complete: false
created: 2026-03-31
---

# Phase 02 — Validation Strategy

> Per-phase validation contract for feedback sampling during execution.

---

## Test Infrastructure

| Property | Value |
|----------|-------|
| **Framework** | JUnit 4 (Kotlin) |
| **Config file** | build.gradle.kts |
| **Quick run command** | `./gradlew test --tests "*GameEngineTest*"` |
| **Full suite command** | `./gradlew test` |
| **Estimated runtime** | ~30 seconds |

---

## Sampling Rate

- **After every task commit:** Run `./gradlew test --tests "*[Target]Test*"`
- **After every plan wave:** Run `./gradlew test`
- **Before `/gsd:verify-work`:** Full suite must be green
- **Max feedback latency:** 60 seconds

---

## Per-Task Verification Map

| Task ID | Plan | Wave | Requirement | Test Type | Automated Command | File Exists | Status |
|---------|------|------|-------------|-----------|-------------------|-------------|--------|
| 02-01-01 | 01 | 1 | CORE-01 | unit | `./gradlew test --tests "*GameStateTest*"` | ❌ W0 | ⬜ pending |
| 02-01-02 | 01 | 1 | CORE-02 | unit | `./gradlew test --tests "*GameEngineTest*"` | ❌ W0 | ⬜ pending |
| 02-01-03 | 01 | 1 | CORE-03 | unit | `./gradlew test --tests "*GameEngineTest*"` | ❌ W0 | ⬜ pending |
| 02-02-01 | 02 | 2 | AI-01 | unit | `./gradlew test --tests "*CpuPlayerTest*"` | ❌ W0 | ⬜ pending |
| 02-02-02 | 02 | 2 | AI-02 | unit | `./gradlew test --tests "*CpuPlayerTest*"` | ❌ W0 | ⬜ pending |
| 02-02-03 | 02 | 2 | AI-03 | unit | `./gradlew test --tests "*CpuPlayerTest*"` | ❌ W0 | ⬜ pending |

*Status: ⬜ pending · ✅ green · ❌ red · ⚠️ flaky*

---

## Wave 0 Requirements

- [ ] `app/src/test/java/com/jna/tictactoe/game/GameEngineTest.kt` — stubs for CORE-02, CORE-03
- [ ] `app/src/test/java/com/jna/tictactoe/game/GameStateTest.kt` — stubs for CORE-01
- [ ] `app/src/test/java/com/jna/tictactoe/game/CpuPlayerTest.kt` — stubs for AI-01, AI-02, AI-03

*If none: "Existing infrastructure covers all phase requirements."*

---

## Manual-Only Verifications

| Behavior | Requirement | Why Manual | Test Instructions |
|----------|-------------|------------|-------------------|
| None | - | - | - |

*If none: "All phase behaviors have automated verification."*

---

## Validation Sign-Off

- [ ] All tasks have `<automated>` verify or Wave 0 dependencies
- [ ] Sampling continuity: no 3 consecutive tasks without automated verify
- [ ] Wave 0 covers all MISSING references
- [ ] No watch-mode flags
- [ ] Feedback latency < 60s
- [ ] `nyquist_compliant: true` set in frontmatter

**Approval:** pending
