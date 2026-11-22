# Architecture

## 1. System Overview
FossPulse is a **local-first, zero-server Kotlin Multiplatform (KMP)** application targeting **Android, iOS, Windows, and macOS**. All tracking state, Regex matching, version comparisons, and local AI changelog summaries execute directly on the client device. Cross-device synchronization is an opt-in cloud backup layer using Google Drive's hidden `appDataFolder` via Google Drive REST API v3.

---

## 2. Architecture Pattern
- **Pattern:** Clean Architecture + MVI/MVVM (`StateFlow` unidirectional data flow) inside a shared Kotlin Multiplatform (`composeApp`) module.
- **Reason:** Maximizes code reuse (>90% shared UI, networking, Regex engine, semantic version comparator, and SQLite database across all 4 platforms) while isolating OS-specific APIs (`PackageManager`, `DownloadManager`, `WorkManager`, `BGAppRefreshTask`, System Tray, and local GGUF inference) behind clean `expect/actual` interfaces.

---

## 3. Technology Stack
- **Language & Compiler:** Kotlin `2.4.20` (K2 Compiler enabled)
- **UI Framework:** Compose Multiplatform `1.12.2` (custom Neo-Brutalism design tokens)
- **Architecture & Navigation:** JetBrains Lifecycle ViewModel KMP (`2.11.1`) + Navigation3 KMP (`1.2.2`)
- **Dependency Injection:** Koin Multiplatform (`koin-core`, `koin-compose-viewmodel`)
- **Networking:** Ktor Client `3.x` (`OkHttp` on Android/Desktop, `Darwin` on iOS) + `kotlinx.serialization`
- **Local Database:** Room KMP (`2.8.5`) + Bundled SQLite Driver (`sqlite-bundled` `2.7.1`)
- **Preferences & Secrets:** DataStore Preferences KMP (`1.3.0`) + OS Keystore/Keychain wrapper for optional GitHub PAT and Google OAuth tokens
- **Background Scheduling:**
  - **Android:** `WorkManager` (`PeriodicWorkRequest` at 1h and 24h intervals) + `ACTION_PACKAGE_REPLACED` BroadcastReceiver
  - **iOS:** `BGTaskScheduler` (`BGAppRefreshTask`) + foreground on-launch sync
  - **Windows / macOS:** Compose Desktop `Tray` background coroutine scheduler + native OS notifications
- **On-Device AI:** On-demand downloadable GGUF model via `llama.cpp` JNI (`androidMain`/`jvmMain`) & C-interop (`iosMain`)

---

## 4. Project Structure
```text
fosspulse/
├── docs/                           # Source of truth for Antigravity agent
│   ├── PRD.md
│   ├── DESIGN_SYSTEM.md
│   ├── UX_FLOWS.md
│   ├── ARCHITECTURE.md
│   ├── DATABASE.md
│   ├── API.md
│   ├── SECURITY.md
│   ├── CODE_STYLE.md
│   ├── TESTING.md
│   └── AGENTS.md
├── composeApp/
│   └── src/
│       ├── commonMain/kotlin/io/github/raghavg02/fosspulse/
│       │   ├── app/                # Root App composable, Navigation3 graph, Koin modules
│       │   ├── domain/
│       │   │   ├── model/          # TrackedRepo, ReleaseInfo, SyncPayload, TargetPlatform
│       │   │   ├── engine/         # RegexAssetMatcher, SemVerComparator, NotificationEvaluator
│       │   │   └── usecase/        # SyncReposUseCase, RunDailyAuditUseCase, DriveSyncUseCase
│       │   ├── data/
│       │   │   ├── local/          # Room KMP Database, DAOs, Entities
│       │   │   ├── remote/         # GitHubApiService, GoogleDriveApiService (Ktor)
│       │   │   └── repository/     # RepoRepositoryImpl, ChangelogAiRepositoryImpl
│       │   ├── platform/           # expect declarations (AppScanner, FileDownloader, SecureStorage, LocalLlmEngine, Notifier)
│       │   └── ui/
│       │       ├── theme/          # Neo-Brutalism colors, typography, brutalShadow modifier
│       │       ├── components/     # BrutalCard, BrutalButton, BrutalBadge, BrutalTextField
│       │       ├── dashboard/      # DashboardScreen & ViewModel
│       │       ├── scanner/        # InstalledAppsScannerScreen & ViewModel
│       │       ├── detail/         # ReleaseDetailSheet, AI Summary & GitHub button
│       │       └── settings/       # SettingsScreen (PAT, Google Drive Sync, AI Model manager)
│       ├── androidMain/            # WorkManager workers, PackageManager scanner, DownloadManager, JNI llama.cpp
│       ├── iosMain/                # BGAppRefreshTask, Darwin Ktor engine, Keychain storage
│       └── jvmMain/                # Windows & macOS Tray scheduler, desktop file downloader
└── iosApp/                         # Xcode Swift wrapper entry point