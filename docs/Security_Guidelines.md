# Security Guidelines

## 1. Zero-Backend & Privacy Architecture
- FossPulse operates without a developer-owned server. User watchlists, installed app lists, and local AI summaries stay strictly on the user's device unless the user explicitly enables Google Drive sync.
- Installed package lists (`PackageManager` scan) must NEVER be transmitted to any third-party analytics or external server.

## 2. Secrets & Credential Storage
- **No Hardcoded Keys:** Never embed developer API keys or client secrets inside source files or `BuildConfig`.
- **Optional GitHub PAT:** Stored exclusively in OS-native encrypted storage (`Android Keystore` via EncryptedDataStore, `iOS Keychain`, `Windows Credential Manager` / `macOS Keychain`).
- **Google OAuth Tokens:** Request minimal `drive.appdata` scope only; store refresh/access tokens in secure storage and clear them immediately if the user taps `DISCONNECT GOOGLE DRIVE`.

## 3. Input & Regex Safety (ReDoS Prevention)
- Validate user-entered `asset_regex` patterns before saving using a safe compilation check and enforce a maximum pattern length of `200` characters to prevent catastrophic backtracking (ReDoS) when matching GitHub asset filenames.
- Validate GitHub repository inputs strictly against `^[a-zA-Z0-9_.-]+/[a-zA-Z0-9_.-]+$`.

## 4. Binary Download Verification
- Enforce `https://` on all `browser_download_url` links returned by GitHub before handing off to `DownloadManager`.
- Display the asset's `sha256` digest (when provided by GitHub's release asset payload) in the Release Detail sheet so users can verify binary integrity.

## 5. Logging Rules
- NEVER log GitHub Personal Access Tokens, Google OAuth bearer tokens, or full `Authorization` headers in Logcat or desktop console logs.