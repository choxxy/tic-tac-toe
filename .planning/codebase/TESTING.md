# Testing

## Test Infrastructure

| Type | Location | Runner |
|---|---|---|
| Unit tests | `app/src/test/` | JUnit 4 (host machine) |
| Instrumented tests | `app/src/androidTest/` | AndroidJUnit4 + Espresso |

## Dependencies

```toml
# From libs.versions.toml
testImplementation(libs.junit)
androidTestImplementation(libs.androidx.junit)
androidTestImplementation(libs.androidx.espresso.core)
androidTestImplementation(libs.androidx.compose.ui.test.junit4)  # Present but unused
```

## Current Coverage

### Unit Tests
- `ExampleUnitTest.kt` — scaffold placeholder only (single addition assertion)
- **No real business logic tests exist yet**

### Instrumented Tests
- `ExampleInstrumentedTest.kt` — scaffold placeholder only (package name assertion)
- **No Compose UI tests written** despite `compose.ui.test.junit4` being on the classpath

## Test Gaps

- No game logic tests (win detection, draw detection, move validation)
- No Compose UI tests (no screen interaction tests, no state verification)
- No ViewModel tests (if/when ViewModels are added)
- Compose UI testing dependency is wired up but completely unused

## How to Run

```bash
./gradlew test                        # All unit tests
./gradlew testDebugUnitTest           # Debug unit tests only
./gradlew connectedDebugAndroidTest   # Instrumented tests (requires device/emulator)
```

## Recommended Test Strategy

Given the game domain, prioritize:
1. Unit tests for game logic (pure Kotlin — fast, no Android)
2. Compose UI tests for screen state transitions (using `compose.ui.test.junit4` already present)
