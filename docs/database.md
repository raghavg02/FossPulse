---

### 5. `docs/DATABASE.md`

```markdown
# Database Specification

## 1. Database Engine
- **Engine:** SQLite via **Room KMP (`2.8.5`)** + `BundledSQLiteDriver` (`2.7.1`) for identical SQLite behavior across Android, iOS, Windows, and macOS.
- **Database Name:** `fosspulse.db`

---

## 2. Naming Conventions
- **Tables:** `snake_case`, plural (`tracked_repos`, `cached_releases`, `ai_summaries`).
- **Columns:** `snake_case` (`repo_id`, `asset_regex`, `installed_version`).
- **Timestamps:** `Long` storing Unix epoch milliseconds (`UTC`).

---

## 3. Tables

### `tracked_repos`
Stores user-tracked repositories, platform regex filters, local installed state, and HTTP caching metadata.
- `id`: `String` (Primary Key — canonical `"owner/repo"` lowercase, e.g., `"imranr98/obtainium"`)
- `owner`: `String` (Required)
- `repo_name`: `String` (Required)
- `display_name`: `String` (Required)
- `android_package_name`: `String?` (Nullable — e.g., `"dev.imranr.obtainium"`, used for `PackageManager` lookup)
- `target_platform`: `String` (Enum stored as String: `"ANDROID"`, `"IOS"`, `"WINDOWS"`, `"MACOS"`)
- `asset_regex`: `String` (Required — e.g., `".*(arm64|universal).*\\.apk$"`)
- `include_prereleases`: `Boolean` (Default `false`)
- `installed_version`: `String?` (Local device state — NOT overwritten by Google Drive sync)
- `latest_remote_version`: `String?` (Latest tag fetched from GitHub)
- `last_notified_tag`: `String?` (Prevents duplicate hourly notifications for the same release)
- `etag`: `String?` (HTTP `ETag` header for `If-None-Match` conditional requests)
- `last_checked_at`: `Long` (Epoch ms)
- `updated_at`: `Long` (Epoch ms — used for Google Drive sync conflict resolution)
- `is_deleted`: `Boolean` (Soft delete flag for Google Drive tombstone sync; default `false`)

### `cached_releases`
Stores the latest matched release metadata and binary download URL for each repo.
- `repo_id`: `String` (Primary Key, Foreign Key → `tracked_repos.id` ON DELETE CASCADE)
- `tag_name`: `String` (Required)
- `release_title`: `String` (Required)
- `raw_changelog_md`: `String` (Full Markdown release body from GitHub)
- `html_url`: `String` (Direct URL to GitHub release page)
- `matched_asset_name`: `String?` (Filename matching `asset_regex`, null if binary not yet uploaded)
- `matched_asset_url`: `String?` (`browser_download_url` for one-tap download)
- `matched_asset_size_bytes`: `Long?`
- `published_at`: `Long` (Epoch ms)

### `ai_summaries`
Caches locally generated LLM changelog summaries so inference runs only once per release.
- `cache_key`: `String` (Primary Key — formatted as `"{repo_id}:{tag_name}"`)
- `repo_id`: `String` (Foreign Key → `tracked_repos.id` ON DELETE CASCADE)
- `tag_name`: `String` (Required)
- `summary_bullets`: `String` (3–4 concise Markdown bullet points)
- `model_name`: `String` (e.g., `"qwen2.5-1.5b-instruct-q4_k_m"`)
- `generated_at`: `Long` (Epoch ms)

---

## 4. Google Drive Sync Payload (`fosspulse_sync.json`)
Stored inside Google Drive's hidden `appDataFolder`. Excludes device-specific `installed_version`, `etag`, and `last_notified_tag`.
```json
{
  "schemaVersion": 1,
  "syncedAt": 1795320000000,
  "repos": [
    {
      "id": "imranr98/obtainium",
      "owner": "ImranR98",
      "repoName": "Obtainium",
      "displayName": "Obtainium",
      "androidPackageName": "dev.imranr.obtainium",
      "targetPlatform": "ANDROID",
      "assetRegex": ".*(arm64-v8a|universal).*\\.apk$",
      "includePrereleases": false,
      "updatedAt": 1795319000000,
      "isDeleted": false
    }
  ]
}