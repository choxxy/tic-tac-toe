---
phase: 01
slug: foundation-navigation
status: draft
nyquist_compliant: true
wave_0_complete: false
created: 2025-03-04
---

# Phase 01 — Validation Strategy

> Per-phase validation contract for feedback sampling during execution.

---

## Test Infrastructure

| Property | Value |
|----------|-------|
| **Framework** | Compose UI Test (JUnit4) |
| **Config file** | app/build.gradle.kts |
| **Quick run command** | `./gradlew help` |
| **Full suite command** | `./gradlew connectedDebugAndroidTest` |
| **Estimated runtime** | ~60 seconds |

---

## Sampling Rate

- **After every task commit:** Run `./gradlew help` (compilation check)
- **After every plan wave:** Run `./gradlew connectedDebugAndroidTest` (UI behavior)
- **Before `/gsd:verify-work`:** Full suite must be green
- **Max feedback latency:** 120 seconds

---

## Per-Task Verification Map

| Task ID | Plan | Wave | Requirement | Test Type | Automated Command | File Exists | Status |
|---------|------|------|-------------|-----------|-------------------|-------------|--------|
| 01-00-01 | 00 | 0 | NAV-02 | smoke | `ls app/src/androidTest/java/com/jna/tictactoe/NavigationTest.kt` | ❌ W0 | ⬜ pending |
| 01-01-01 | 01 | 1 | INFRA-01 | grep | `grep -E "navigationCompose\|kotlinxSerialization" gradle/libs.versions.toml` | ✅ | ⬜ pending |
| 01-01-02 | 01 | 1 | INFRA-01 | build | `./gradlew help` | ✅ | ⬜ pending |
| 01-01-03 | 01 | 1 | INFRA-01 | fs | `ls -R app/src/main/java/com/jna/tictactoe/` | ✅ | ⬜ pending |
| 01-02-01 | 02 | 2 | INFRA-01 | build | `./gradlew help` | ✅ | ⬜ pending |
| 01-02-02 | 02 | 2 | NAV-01 | grep | `grep "@Serializable object" app/src/main/java/com/jna/tictactoe/navigation/Routes.kt` | ✅ | ⬜ pending |
| 01-03-01 | 03 | 3 | MENU-01 | grep | `grep "MainMenuScreen" app/src/main/java/com/jna/tictactoe/screen/menu/MainMenuScreen.kt` | ✅ | ⬜ pending |
| 01-03-02 | 03 | 3 | NAV-02 | grep | `grep -E "NavHost\|popUpTo\(Splash\)" app/src/main/java/com/jna/tictactoe/MainActivity.kt` | ✅ | ⬜ pending |
| 01-03-03 | 03 | 3 | SPLASH-01 | UI Test | `./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.jna.tictactoe.NavigationTest` | ✅ | ⬜ pending |

*Status: ⬜ pending · ✅ green · ❌ red · ⚠️ flaky*

---

## Wave 0 Requirements

- [ ] `app/src/androidTest/java/com/jna/tictactoe/NavigationTest.kt` — stubs for NAV-02

---

## Manual-Only Verifications

| Behavior | Requirement | Why Manual | Test Instructions |
|----------|-------------|------------|-------------------|
| Visual UX check | SPLASH-01 | Aesthetic | Run app, ensure splash is centered and high quality. |
| Back button exit | NAV-02 | OS interaction | From Main Menu, press system back button, verify app exits. |

---

## Validation Sign-Off

- [x] All tasks have `<automated>` verify or Wave 0 dependencies
- [x] Sampling continuity: no 3 consecutive tasks without automated verify
- [x] Wave 0 covers all MISSING references
- [x] No watch-mode flags
- [x] Feedback latency < 120s
- [x] `nyquist_compliant: true` set in frontmatter

**Approval:** pending
