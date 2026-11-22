# UX Flows

## Flow 1 — First Launch & Onboarding (Zero Sign-In)
1. User opens FossPulse for the first time.
2. App initializes local SQLite database immediately—no login screen is shown.
3. Empty Dashboard displays two Neo-Brutalist action cards:
   - **Scan Installed Apps (Android):** Requests package visibility, scans installed apps, matches open-source package names against curated/known GitHub mappings, auto-detects device CPU ABI (`arm64-v8a`), and pre-fills Regex rules.
   - **Add GitHub Repo Manually:** User pastes `https://github.com/owner/repo` (or `owner/repo`), selects Target OS (`Android`, `iOS`, `Windows`, `macOS`), reviews auto-suggested Regex filter, and saves.
4. App performs an immediate initial fetch to populate `latestTag`, matched asset URL, and `ETag`.

---

## Flow 2 — Hourly Release Watcher (Background)
1. Hourly background worker wakes up (constrained to `NetworkType.CONNECTED`).
2. Iterates through all tracked repos in the local database:
   - Sends `GET /repos/{owner}/{repo}/releases/latest` with header `If-None-Match: {savedETag}`.
   - If HTTP `304 Not Modified`: Skips repo (0 rate-limit cost).
   - If HTTP `200 OK`: Updates `savedETag`, extracts `tag_name`, and filters `assets[]` using the repo's `assetRegex`.
3. **Asset Gate Check:**
   - If `assetRegex` matches **no asset** in the release (e.g., dev uploaded source code only, binary still building): Do NOT notify yet; wait for the next hourly cycle.
   - If a matching binary IS found AND `tag_name != lastNotifiedTag`:
     - Reads current `installedVersion` (live from `PackageManager` on Android; from DB on Desktop/iOS).
     - **Branch A (`installedVersion < tag_name`):** Fires notification:
       - *Title:* `"{AppName} {tag_name} is out"`
       - *Body:* `"Update available ({installedVersion} → {tag_name}). Tap to download."`
     - **Branch B (`installedVersion >= tag_name`):** Fires notification:
       - *Title:* `"{AppName} updated to {tag_name}"`
       - *Body:* `"You're on the latest version — check out what's new!"`
     - Updates `lastNotifiedTag = tag_name`.

---

## Flow 3 — Daily Version Audit (Once per 24 Hours)
1. Daily background worker runs once every 24 hours.
2. Refreshes live `installedVersion` for all tracked apps and compares against `latestTag`.
3. Collects all repos where `installedVersion < latestTag`:
   - **0 apps outdated:** No notification sent.
   - **1 app outdated:** Sends notification with that app's name (`"Update available: {AppName}"`) and version jump.
   - **2+ apps outdated:** Sends single grouped notification (`"{N} apps have updates available"`).

---

## Flow 4 — Release Details, Local AI Summary & Direct Download
1. User taps a repo card on the Dashboard (or taps a notification).
2. Opens **Release Detail Sheet**:
   - Displays version comparison badge (`INSTALLED: v1.1.2` vs `LATEST: v1.1.4`), matched filename (`app-arm64-v8a.apk`), and file size.
   - **Primary CTA (`DOWNLOAD {ASSET_NAME}`):** Triggers direct binary download via system `DownloadManager` (or marks installed on Desktop/iOS).
   - **AI Summary Section:**
     - If local model is downloaded: Shows 3–4 bullet points (*Features, Fixes, Breaking Changes*) cached locally.
     - If local model is not downloaded yet: Shows a `DOWNLOAD LOCAL AI MODEL (900 MB)` Neo-Brutalist button.
   - **Bottom Pinned Button:** `READ FULL CHANGELOG ON GITHUB` → opens the release `html_url` in the user's browser.

---

## Flow 5 — Optional Google Drive Sync (Settings)
1. User opens **Settings → Cloud Sync**.
2. Taps `CONNECT GOOGLE DRIVE`.
3. Completes Google OAuth consent for `drive.appdata` scope only.
4. App checks `appDataFolder` for `fosspulse_sync.json`:
   - If found: Merges remote repos and Regex rules with local DB (newest `updatedAt` wins; device-specific `installedVersion` stays local).
   - If not found: Uploads initial local watchlist snapshot to `appDataFolder`.
5. Subsequent repo additions/edits trigger a background sync upload when connected.