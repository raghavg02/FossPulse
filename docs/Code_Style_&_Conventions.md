# Code Style & Conventions

## 1. Kotlin & Compose Multiplatform Conventions
- Follow official Kotlin coding conventions (`ktlint` compatible, 4-space indentation, max line length 120 chars).
- Keep all business logic, domain models, and Compose UI inside `commonMain` whenever possible.
- Use `expect` / `actual` strictly for OS-level capabilities (`AppScanner`, `FileDownloader`, `SecureStorage`, `LocalLlmEngine`, `Notifier`, `BackgroundScheduler`).

## 2. Naming Conventions
- **Composables:** `PascalCase` nouns (`BrutalRepoCard`, `DashboardScreen`, `ReleaseDetailSheet`). Always pass `modifier: Modifier = Modifier` as the first optional parameter.
- **ViewModels:** `{Feature}ViewModel` exposing `val uiState: StateFlow<{Feature}UiState>` and `fun onEvent(event: {Feature}UiEvent)`.
- **UseCases:** Verb-first `PascalCase` with `operator fun invoke(...)` (`EvaluateReleaseNotificationUseCase`).
- **Coroutines:** Inject `CoroutineDispatcher` (`Dispatchers.IO` / `Dispatchers.Default`) rather than hardcoding dispatchers inside repositories or engines so unit tests can use `StandardTestDispatcher`.

## 3. Neo-Brutalism UI Rules in Code
- Do NOT use `Card`, `ElevatedCard`, or default Material `Button` directly in feature screens—always use `BrutalCard`, `BrutalButton`, `BrutalBadge`, and `BrutalTextField` from `ui/components/`.
- Do NOT use `shadowElevation` or blur shadows; apply `Modifier.brutalShadow(offsetX = 4.dp, offsetY = 4.dp)`.

## 4. Error Handling
- Domain and data methods return Kotlin `Result<T>` or sealed `NetworkResult<T>` (`Success`, `NotModified`, `RateLimited(resetEpoch)`, `Error(message)`). Never swallow exceptions silently.s