# Product Requirements Document (PRD)

## 1. Product Overview
- **Product Name:** FossPulse
- **One-Line Description:** A local-first, cross-platform GitHub release watcher and version auditor that matches exact OS binaries via Regex, detects installed app versions, summarizes changelogs on-device, and optionally syncs watchlists via Google Drive.
- **Product Type:** Cross-Platform Open-Source Utility Application (Kotlin Multiplatform)
- **Primary Platforms:** Android, iOS, Windows, and macOS (Compose Multiplatform 1.12.2 / Kotlin 2.4.x)

---

## 2. Problem
- **What problem are we solving?** Users who rely on open-source software across mobile and desktop (APKs on Android, `.ipa` on iOS, `.exe`/`.msi` on Windows, `.dmg`/`.pkg` on macOS) fall behind on updates because GitHub repositories require manual checking, developer "Watch" notifications create noise, and single-OS tools (like Obtainium or UniGetUI) do not cover all devices together.
- **Why does this problem matter?** Outdated open-source apps miss security patches and new features, while multi-device users lack a unified watchlist that knows both what is published on GitHub and what is actually installed on their device.
- **Current alternatives & gaps:**
  - *Obtainium:* Android-only; ignores Windows, macOS, and iOS tools.
  - *UniGetUI:* Windows-only; misses mobile apps and GitHub repos not packaged in WinGet/Scoop.
  - *GitHub Watch / RSS:* No Regex binary filtering, misses tag-only repos, and does not compare against locally installed versions.

---

## 3. Goal
- **Primary Goal:** Deliver instant, accurate release notifications and daily version audits across Android, iOS, Windows, and macOS without requiring a central backend server or mandatory account sign-in.
- **Secondary Goals:**
  - Eliminate false-positive alerts when a GitHub release lacks an installer matching the user's CPU architecture/OS.
  - Surface concise, locally generated AI changelog summaries while always linking back to the official GitHub release page.
  - Enable seamless watchlist backup and multi-device sync via Google Drive `appDataFolder` as an opt-in setting.

---

## 4. Target Users
### Persona 1 — Multi-Device FOSS Power User
- **Description:** Uses open-source utilities across an Android phone and a Windows/macOS laptop (e.g., sideloaded APKs, GitHub-hosted desktop utilities, browser mods).
- **Needs:** One unified watchlist, exact architecture asset matching (`arm64-v8a`, `x64`, `universal`), direct binary downloads, and optional cross-device sync.
- **Pain Points:** Forgetting to update desktop tools, downloading the wrong architecture build, and getting spammed by irrelevant pre-releases.

---

## 5. User Stories
- As a user, I want FossPulse to work immediately offline with zero sign-in so I can start tracking repos right away.
- As a user, I want to scan my installed Android apps and auto-match them to their open-source GitHub repositories with pre-filled architecture Regex rules.
- As a user, I want hourly background checks that wait until a release actually attaches my Regex-matched binary before notifying me.
- As a user, I want FossPulse to check my installed version before pinging me—notifying *"Update available"* if outdated, or *"Updated to vX — check out what's new!"* if I already installed the latest version.
- As a user, I want a once-daily version audit that sends the app name if 1 app is outdated, or a single grouped count (*"4 apps need updates"*) if multiple apps are outdated.
- As a user, I want to tap a release card to read a local AI summary of the changelog with a button at the bottom to open the full changelog on GitHub.
- As a user, I want an optional Google Drive sync toggle so my tracked repos and Regex rules sync across my phone and laptop without exposing files in my main Drive.

---

## 6. Core Features

### Feature 1 — Discovery & Regex Asset Matcher
- **Purpose:** Track GitHub repositories across Android, iOS, Windows, and macOS and isolate the exact downloadable binary.
- **Inputs:** GitHub repo URL (`owner/repo`), target OS (`ANDROID`, `IOS`, `WINDOWS`, `MACOS`), Regex pattern (auto-suggested from device ABI/OS), pre-release toggle.
- **Outputs:** Filtered release object containing exact matched `asset_name`, `browser_download_url`, file size, and version tag.
- **Acceptance Criteria:**
  - Automatically detects Android CPU ABI (`Build.SUPPORTED_ABIS[0]`) and suggests default Regex (e.g., `.*(arm64|universal).*\.apk$`).
  - Scans installed Android apps (`PackageManager`) to match known open-source package IDs and read live `versionName`.
  - Falls back to `/repos/{owner}/{repo}/tags` when `/releases/latest` returns `404`.
  - One-tap direct asset download using Android `DownloadManager` (or native OS download handler on Desktop/iOS).

### Feature 2 — Two-Tier Background Engine (Hourly Watcher + Daily Audit)
- **Purpose:** Keep version status current while respecting GitHub API rate limits and battery life.
- **Acceptance Criteria:**
  - **Hourly Check:** Polls tracked repos using `If-None-Match` (`ETag`). Only triggers notification when a new tag has a Regex-matched asset AND `tag != lastNotifiedTag`.
    - *If `installedVersion < latestTag`:* Notify `"{AppName} {latestTag} is out"` — `"Update available ({installedVersion} → {latestTag}). Tap to download."`
    - *If `installedVersion >= latestTag`:* Notify `"{AppName} updated to {latestTag}"` — `"You're on the latest version — check out what's new!"`
  - **Daily Audit (Once every 24h):** Compares all tracked repos' `installedVersion` vs. `latestTag`.
    - *0 outdated:* Silent.
    - *1 outdated:* Notify with that specific app's name and version jump.
    - *2+ outdated:* Notify with `"{Count} apps have updates available"`.
  - **Rate-Limit Safety:** Works out of the box without any API key (60 req/hr per IP via `ETag` `304` caching); provides an optional Settings field for a user-supplied GitHub PAT (5,000 req/hr).

### Feature 3 — On-Device AI Changelog Reader
- **Purpose:** Summarize verbose GitHub release notes locally without cloud LLM costs.
- **Acceptance Criteria:**
  - On-demand downloadable local model (kept out of the base app binary).
  - Generates 3–4 bullet points (*Features, Fixes, Breaking Changes*) and caches the summary in the local database by `(repoId, tagName)`.
  - Every summary view anchors a high-contrast **"READ FULL CHANGELOG ON GITHUB"** button at the bottom that opens `html_url`.

### Feature 4 — Local-First Storage & Opt-In Google Drive Sync
- **Purpose:** Provide instant local usage with optional multi-device synchronization.
- **Acceptance Criteria:**
  - Local SQLite database (Room KMP) is the primary source of truth.
  - Opt-in Google Sign-In using `https://www.googleapis.com/auth/drive.appdata` scope reads/writes `fosspulse_sync.json` inside Google Drive's hidden `appDataFolder`.
  - Syncs repo list, target OS tags, and Regex rules across devices without overwriting per-device `installedVersion` records.

---

## 7. Non-Goals (Out of Scope for v1)
- Hosting a custom cloud backend server or proprietary user authentication database.
- Automatic silent root/Shizuku APK installation (direct download + standard OS installer handoff only).
- Supporting non-GitHub hosts (GitLab/Codeberg deferred to v2).

---

## 8. Definition of Done
- [ ] All 4 features implemented in Kotlin Multiplatform (`commonMain` + platform actuals).
- [ ] Neo-Brutalism design tokens applied consistently across all screens.
- [ ] Unit tests pass for Regex asset matching, semantic version comparison, and ETag caching.
- [ ] Android APK builds cleanly in Android Studio with zero lint/compiler errors.