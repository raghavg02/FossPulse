# Design System — Neo-Brutalism

## 1. Design Direction
- **Style:** Neo-Brutalism (High-contrast, tactile, raw structural clarity).
- **Mood:** Bold, utilitarian, unapologetically crisp—built like a precision developer tool with arcade/print-zine energy.
- **Core Visual Rules (Strict):**
  - **Hard Borders:** Every interactive card, button, badge, input field, and modal MUST have a solid `2.dp` (or `3.dp` for primary CTAs) `#111111` border.
  - **Hard Offset Shadows:** Zero blur (`blurRadius = 0.dp`). Shadows are solid `#111111` rectangles offset by `4.dp` X and `4.dp` Y (`Modifier.brutalShadow()`).
  - **Zero Gradients & Zero Glassmorphism:** Flat, solid color fills only. No translucent blur surfaces.
  - **Tactile Press Physics:** Pressing a button translates it by `(+3.dp, +3.dp)` while shrinking its offset shadow to `(1.dp, 1.dp)`.

---

## 2. Brand
- **Brand Name:** FossPulse
- **Brand Voice:** Direct, technical, concise, zero fluff. Action labels use uppercase verbs (`SCAN APPS`, `DOWNLOAD APK`, `SYNC DRIVE`, `READ ON GITHUB`).

---

## 3. Color Palette

### Light Mode (Default Canvas)
- **Background (Canvas):** `#F7F5EE` (Warm Off-White / Paper Cream)
- **Surface (Cards/Sheets):** `#FFFFFF` (Pure White)
- **Primary Accent:** `#FFE600` (Acid Cyber Yellow — primary CTAs, active tabs)
- **Secondary Accent:** `#A388EE` (Electric Lavender — AI summary badges, tags)
- **Success / Up-to-Date:** `#5CE67E` (Brutal Mint Green)
- **Warning / Update Ready:** `#FF9F1C` (Signal Tangerine)
- **Error / Rate Limit:** `#FF4D4D` (Raw Crimson)
- **Ink / Border / Shadow:** `#111111` (Pitch Charcoal Black)
- **Muted Text:** `#4A4A4A` (Dark Slate)

### Dark Mode (High-Contrast Terminal Brutalism)
- **Background (Canvas):** `#121212`
- **Surface (Cards/Sheets):** `#1E1E1E`
- **Primary Accent:** `#FFE600` (with `#111111` text)
- **Border / Shadow:** `#F7F5EE` (Off-White crisp outlines) or `#000000` hard shadow offset

---

## 4. Typography
- **Heading Font:** `Space Grotesk` (Bold `700`, tight letter spacing `-0.5.sp`)
- **Body Font:** `Inter` or `Plus Jakarta Sans` (`500` Medium / `600` SemiBold)
- **Monospace Font (Version Tags, Regex, Package IDs):** `JetBrains Mono` (`700` Bold)
- **Scale:**
  - **H1 (Screen Title):** `28.sp` / Bold / Line Height `34.sp`
  - **H2 (Card Header / App Name):** `20.sp` / Bold / Line Height `26.sp`
  - **Mono Badge (v1.2.4 / Regex):** `13.sp` / Bold / `JetBrains Mono`
  - **Body:** `15.sp` / Medium / Line Height `22.sp`
  - **Button Label:** `14.sp` / Bold / Uppercase / Letter Spacing `0.8.sp`

---

## 5. Spacing & Geometry
- **Base Unit:** `4.dp`
- **Spacing Scale:** `4.dp`, `8.dp`, `12.dp`, `16.dp`, `20.dp`, `24.dp`, `32.dp`
- **Border Radius:**
  - **Cards & Containers:** `6.dp` (sharp, slightly chamfered corners—never `24.dp` soft pills)
  - **Buttons & Inputs:** `4.dp`
  - **Status Badges (OS / Version):** `2.dp`
- **Border Width:** `2.dp` standard, `3.dp` emphasized CTA.
- **Shadow Offset:** `4.dp` X, `4.dp` Y, `#111111` solid fill.

---

## 6. Core Reusable Components (Compose Multiplatform)
1. **`BrutalCard`:** Surface fill + `2.dp` `#111111` border + `4.dp` hard offset shadow + `16.dp` internal padding.
2. **`BrutalButton`:** Solid fill (`#FFE600` primary, `#FFFFFF` secondary, `#FF4D4D` danger) + `2.dp` border + `4.dp` shadow + press-offset animation.
3. **`BrutalBadge`:** Compact `2.dp`-radius tag with `1.5.dp` border and `JetBrains Mono` text (e.g., `ANDROID`, `ARM64`, `v1.4.2 → v1.5.0`).
4. **`BrutalTextField`:** Pure `#FFFFFF` box, `2.dp` `#111111` border, monospace font for Regex and GitHub URLs.
5. **`BrutalTopBar`:** Flat header with bottom `2.dp` `#111111` divider line, bold `Space Grotesk` title, and instant sync button.

---

## 7. States & Accessibility
- **Contrast:** Minimum `7:1` contrast ratio between `#111111` ink and pastel/acid fills (`#FFE600`, `#5CE67E`, `#FFFFFF`).
- **Loading State:** Stepped brutalist block progress bar (no thin material spinners).
- **Empty State:** Thick-bordered card with bold headline (`NO REPOS TRACKED YET`) and primary `SCAN INSTALLED APPS` + `ADD GITHUB REPO` buttons.