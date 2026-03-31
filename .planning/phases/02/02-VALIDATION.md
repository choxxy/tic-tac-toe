---
phase: 02
slug: core-game-logic
status: draft
nyquist_compliant: true
wave_0_complete: true
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
| 02-00-01 | 00 | 0 | 2.1, 2.2 | unit | `ls app/src/test/java/com/jna/tictactoe/game/GameEngineTest.kt` | ✅ | ⬜ pending |
| 02-00-02 | 00 | 0 | 2.3 | unit | `ls app/src/test/java/com/jna/tictactoe/game/CpuPlayerTest.kt` | ✅ | ⬜ pending |
| 02-01-01 | 01 | 1 | 2.1 | unit | `./gradlew test --tests "*GameStateTest*"` | ✅ | ⬜ pending |
| 02-01-02 | 01 | 1 | 2.2 | unit | `./gradlew test --tests "*GameEngineTest*"` | ✅ | ⬜ pending |
| 02-02-01 | 02 | 2 | 2.3 | unit | `./gradlew test --tests "*CpuPlayerTest*"` | ✅ | ⬜ pending |
| 02-02-02 | 02 | 2 | 2.3 | unit | `./gradlew test --tests "*CpuPlayerTest*"` | ✅ | ⬜ pending |

*Status: ⬜ pending · ✅ green · ❌ red · ⚠️ flaky*

---

## Wave 0 Requirements

- [x] `app/src/test/java/com/jna/tictactoe/game/GameEngineTest.kt` — stubs for 2.2
- [x] `app/src/test/java/com/jna/tictactoe/game/GameStateTest.kt` — stubs for 2.1
- [x] `app/src/test/java/com/jna/tictactoe/game/CpuPlayerTest.kt` — stubs for 2.3

---

## Manual-Only Verifications

| Behavior | Requirement | Why Manual | Test Instructions |
|----------|-------------|------------|-------------------|
| None | - | - | - |

*If none: "All phase behaviors have automated verification."*

---

## Validation Sign-Off

- [x] All tasks have `<automated>` verify or Wave 0 dependencies
- [x] Sampling continuity: no 3 consecutive tasks without automated verify
- [x] Wave 0 covers all MISSING references
- [x] No watch-mode flags
- [x] Feedback latency < 60s
- [x] `nyquist_compliant: true` set in frontmatter

**Approval:** pending
