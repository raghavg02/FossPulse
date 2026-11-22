---

### 6. `docs/API.md`

```markdown
# External API Contracts

## 1. GitHub REST API v3 (No Auth Required by Default)
- **Base URL:** `https://api.github.com`
- **Required Headers on All Requests:**
  - `Accept: application/vnd.github+json`
  - `X-GitHub-Api-Version: 2022-11-28`
  - `User-Agent: FossPulse-KMP`
  - `If-None-Match: {etag}` (Included whenever `tracked_repos.etag != null`)
  - `Authorization: Bearer {pat}` (Included ONLY if user configured an optional PAT in Settings)

### Endpoint A — Get Latest Stable Release
- `GET /repos/{owner}/{repo}/releases/latest`
- **Responses:**
  - `200 OK`: Parse `tag_name`, `name`, `body`, `html_url`, `published_at`, and `assets[]` (`name`, `browser_download_url`, `size`). Save response header `ETag`.
  - `304 Not Modified`: No new release; rate limit untouched.
  - `404 Not Found`: Repo has no published releases → trigger fallback to Endpoint C (`/tags`).

### Endpoint B — List Releases (Used when `include_prereleases = true`)
- `GET /repos/{owner}/{repo}/releases?per_page=5`
- Select the first item in the JSON array (`releases[0]`).

### Endpoint C — Fallback Git Tags
- `GET /repos/{owner}/{repo}/tags?per_page=5`
- Used only when `/releases/latest` returns `404`. Maps `tags[0].name` to `tag_name` and constructs `html_url` as `https://github.com/{owner}/{repo}/releases/tag/{tag_name}`.

---

## 2. Google Drive REST API v3 (Opt-In Cloud Sync)
- **OAuth 2.0 Scope:** `https://www.googleapis.com/auth/drive.appdata` (Strictly restricted to hidden app folder; cannot access user's personal Drive files).
- **Find Sync File:**
  - `GET https://www.googleapis.com/drive/v3/files?spaces=appDataFolder&q=name='fosspulse_sync.json'&fields=files(id,modifiedTime)`
- **Create Sync File (First Sync):**
  - `POST https://www.googleapis.com/upload/drive/v3/files?uploadType=multipart` with metadata `{"name": "fosspulse_sync.json", "parents": ["appDataFolder"]}`.
- **Update Sync File (Subsequent Syncs):**
  - `PATCH https://www.googleapis.com/upload/drive/v3/files/{fileId}?uploadType=media` with `Content-Type: application/json`.