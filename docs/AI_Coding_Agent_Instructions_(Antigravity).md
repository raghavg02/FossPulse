# AI Coding Agent Instructions (Antigravity)

## 1. Project Context
- **Project:** FossPulse (`io.github.raghavg02.fosspulse`)
- **Stack:** Kotlin `2.4.20` + Compose Multiplatform `1.12.2` (Android, iOS, Windows, macOS) + Room KMP + Ktor Client.
- **Design Language:** Strict Neo-Brutalism (`2.dp` `#111111` borders, `4.dp` hard offset shadows with zero blur, `#F7F5EE` warm off-white canvas, `#FFE600` cyber yellow primary accent).

## 2. Source of Truth
Before creating or modifying any code, read `/docs`:
- `PRD.md` (features, two-tier notification rules, non-goals)
- `DESIGN_SYSTEM.md` (Neo-Brutalism tokens & components)
- `UX_FLOWS.md` (exact user & background worker flows)
- `ARCHITECTURE.md` (KMP folder structure & dependency rules)
- `DATABASE.md` (Room KMP entities & Google Drive `appDataFolder` JSON schema)
- `API.md` (GitHub REST API v3 & Google Drive REST API v3 contracts)
- `SECURITY.md` (no hardcoded keys, ReDoS protection, secure token storage)
- `CODE_STYLE.md` & `TESTING.md`

## 3. Non-Negotiable Rules
1. **Do NOT invent a custom cloud backend server.** FossPulse is local-first with opt-in Google Drive `appDataFolder` sync only.
2. **Do NOT ask the user for a GitHub API key during onboarding.** Public endpoints work without authentication using `ETag` caching; keep PAT optional in Settings.
3. **Do NOT use soft Material 3 shadows, gradients, or glassmorphism.** Enforce `BrutalCard`, `BrutalButton`, and `BrutalBadge` everywhere.
4. **Always anchor the `"READ FULL CHANGELOG ON GITHUB"` button** at the bottom of the AI changelog summary card.
5. **Follow Plan → Implement → Validate:** Present a concise file-level plan before writing multi-file changes, run `./gradlew :composeApp:testDebugUnitTest` and `./gradlew :composeApp:assembleDebug` after implementation, and fix any compiler or test errors before finishing.