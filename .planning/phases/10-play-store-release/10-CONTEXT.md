# Phase 10: Play Store Release Prep - Context

**Gathered:** 2026-04-08
**Status:** Ready for planning

<domain>
## Phase Boundary

Sign a release AAB, complete the Play Store store listing, upload ASO screenshots, and submit the app for review. This phase ends when the app is submitted to Google Play Console and awaiting review.

</domain>

<decisions>
## Implementation Decisions

### App title
- **D-01:** Play Store title is **"Tic Tac Toe – Zenith Grid"** (keyword-first for discovery, 25 chars — fits 30-char limit).

### Store description
- **D-02:** Full description built around the 3 confirmed ASO benefits as section anchors:
  1. PLAY FRIENDS OVER LOCAL WI-FI
  2. CHALLENGE AN UNBEATABLE AI
  3. PASS AND PLAY WITH FAMILY
- **D-03:** Claude generates the copy; user reviews and approves before committing.
- **D-04:** Short description (80 chars max) should also lead with the most compelling benefit.

### Category & rating
- **D-05:** Category: **Games → Board**
- **D-06:** Content rating: **Everyone (US) / PEGI 3 (EU)** — no violence, no IAP, no ads, no UGC. Straightforward IARC questionnaire.

### Claude's Discretion
- Keystore generation: use standard `keytool` flow, store credentials in `local.properties` (gitignored). Never commit keystore or passwords to repo.
- Release build hardening: enable `isMinifyEnabled = true` and R8 shrinking for the release build type. Currently `false` in `app/build.gradle.kts`.
- Signing config: add `signingConfigs.release` block reading from `local.properties` via `gradle.properties` or direct file reference.
- Play Store setup: create new app in Play Console, publish to internal test track first to verify upload, then promote to production.
- Google Play App Signing: enroll so Google manages the final signing key (upload key separate from app signing key) — recommended for Play Store apps.

</decisions>

<specifics>
## Specific Ideas

- Title: "Tic Tac Toe – Zenith Grid" — keyword first so users searching "tic tac toe" find it, but the brand name anchors it as distinct.
- ASO screenshots (all 3 already finalized): `screenshots/final/01-play-friends-wifi.jpg`, `02-challenge-unbeatable-ai.jpg`, `03-pass-and-play-family.jpg` — Electric Blue #005BC1, 1290×2796px.

</specifics>

<canonical_refs>
## Canonical References

No external specs — requirements are fully captured in decisions above.

### Screenshots (already generated)
- `screenshots/final/01-play-friends-wifi.jpg` — Benefit: PLAY FRIENDS OVER LOCAL WI-FI
- `screenshots/final/02-challenge-unbeatable-ai.jpg` — Benefit: CHALLENGE AN UNBEATABLE AI
- `screenshots/final/03-pass-and-play-family.jpg` — Benefit: PASS AND PLAY WITH FAMILY

### Build config
- `app/build.gradle.kts` — Release build type, signingConfigs, versionCode/versionName (currently 1 / "1.0")

</canonical_refs>

<code_context>
## Existing Code Insights

### Reusable Assets
- `screenshots/final/` — All 3 ASO screenshots ready for upload. No action needed.
- `app/build.gradle.kts` — `applicationId = "com.jna.tictactoe"`, `versionCode = 1`, `versionName = "1.0"` already set.

### Established Patterns
- No signing config exists yet — `release` build type has `isMinifyEnabled = false` and no `signingConfig` block.
- ProGuard rules file exists at `app/proguard-rules.pro` — available when minify is enabled.

### Integration Points
- `app/build.gradle.kts` → add `signingConfigs` block + update `release` build type.
- `local.properties` → store keystore path, passwords (already gitignored by default Android `.gitignore`).

</code_context>

<deferred>
## Deferred Ideas

- Crash reporting (Firebase Crashlytics) — separate phase post-launch.
- Analytics / play events — separate phase.
- In-app review prompt (Google Play In-App Review API) — future enhancement.
- CI/CD signing pipeline (GitHub Actions) — future improvement.

</deferred>

---

*Phase: 10-play-store-release*
*Context gathered: 2026-04-08*
