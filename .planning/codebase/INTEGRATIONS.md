# External Integrations

**Analysis Date:** 2026-03-30

## APIs & External Services

None detected. The application has no runtime calls to external HTTP APIs or third-party cloud services.

**Note on Google Fonts:** `androidx.compose.ui:ui-text-google-fonts` 1.10.6 is declared as a dependency in `app/build.gradle.kts`, which would normally enable runtime font fetching from Google Fonts. However, inspection of `app/src/main/java/com/jna/tictactoe/ui/theme/Type.kt` shows that the `ManropeFamily` font is loaded entirely from bundled local resources (`app/src/main/res/font/manrope_*.ttf`). The Google Fonts library appears unused at runtime; no `GoogleFont.Provider` or `GoogleFont()` calls exist in the codebase.

## Data Storage

**Databases:**
- None. No database library (Room, SQLite, Realm, etc.) is present.

**File Storage:**
- None. No file I/O or cloud storage integration detected.

**Caching:**
- None. No caching layer detected.

## Authentication & Identity

**Auth Provider:**
- None. No authentication library or service is integrated.

## Monitoring & Observability

**Error Tracking:**
- None. No Crashlytics, Sentry, or similar SDK present.

**Analytics:**
- None. No Firebase Analytics, Mixpanel, Amplitude, or similar SDK present.

**Logs:**
- Standard Android `android.util.Log` available but no log aggregation service configured.

## CI/CD & Deployment

**Hosting:**
- Not configured. No CI/CD pipeline files (`.github/workflows/`, `Jenkinsfile`, `bitrise.yml`, etc.) detected.

**Play Store:**
- Not configured. No `play-publisher` plugin or release signing config present. Release build type has `isMinifyEnabled = false` and no signing config beyond the default debug keystore.

## Environment Configuration

**Required env vars:**
- None. The application requires no environment variables or API keys at runtime.

**Secrets location:**
- `local.properties` — auto-generated file containing local SDK path only; no secrets.

## Webhooks & Callbacks

**Incoming:**
- None.

**Outgoing:**
- None.

## Network Permissions

The `AndroidManifest.xml` declares no `INTERNET` permission, confirming the app is fully offline with no network access at runtime.

---

*Integration audit: 2026-03-30*
