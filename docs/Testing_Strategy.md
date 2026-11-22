# Testing Strategy

## 1. Testing Tools
- **Unit Testing (`commonTest`):** `kotlin.test`, `kotlinx-coroutines-test`, `ktor-client-mock`
- **UI Testing (`commonTest`):** Compose Multiplatform UI Testing v2 (`runComposeUiTest`)

## 2. Critical Unit Test Suites (Must Pass Before Build Completion)
1. **`SemVerComparatorTest`:**
   - Normalizes prefixes (`"v1.2.0"` == `"1.2.0"`, `"release-2.0.1"` > `"2.0.0"`).
   - Handles pre-release suffixes (`"1.2.0"` > `"1.2.0-beta.1"`).
   - Handles build metadata (`"1.1.4+45"` vs `"1.1.4"`).
2. **`RegexAssetMatcherTest`:**
   - Matches `arm64-v8a.apk` and ignores `armeabi-v7a.apk` or `.sha256` files.
   - Returns `null` when a GitHub release contains only source archives (`.zip`, `.tar.gz`) and no matching OS binary—verifying that notifications are suppressed until the binary is attached.
3. **`NotificationDecisionEngineTest`:**
   - `installedVersion = "1.1.2"`, `latestTag = "1.1.4"`, `lastNotifiedTag = "1.1.2"` → emits `NotifyUpdateAvailable`.
   - `installedVersion = "1.1.4"`, `latestTag = "1.1.4"`, `lastNotifiedTag = "1.1.2"` → emits `NotifyAlreadyUpdatedWhatsNew`.
   - `lastNotifiedTag == latestTag` → emits `SuppressDuplicate`.
   - Daily audit with `0`, `1`, and `3` outdated repos → verifies silent, single-app, and grouped multi-app notification payloads.
4. **`GitHubApiClientTest` (`MockEngine`):**
   - Verifies `If-None-Match` header is sent and `304 Not Modified` returns `NetworkResult.NotModified`.
   - Verifies `404` on `/releases/latest` triggers fallback to `/tags`.

## 3. Validation Commands
- Run all shared unit tests: `./gradlew :composeApp:jvmTest` (or `:composeApp:testDebugUnitTest`)
- Build Android Debug APK: `./gradlew :composeApp:assembleDebug`