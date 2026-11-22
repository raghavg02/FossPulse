package io.github.raghavg02.fosspulse.domain.ai

object ChangelogSemanticSummarizer {

    fun generateSummary(rawChangelog: String): String {
        val lines = rawChangelog.lines().map { it.trim() }.filter { it.isNotBlank() }
        val features = mutableListOf<String>()
        val fixes = mutableListOf<String>()
        val breaking = mutableListOf<String>()
        val other = mutableListOf<String>()

        var currentSection = ""

        for (line in lines) {
            val lower = line.lowercase()
            if (line.startsWith("#")) {
                currentSection = when {
                    "breaking" in lower -> "BREAKING"
                    "feat" in lower || "added" in lower || "new" in lower -> "FEATURES"
                    "fix" in lower || "bug" in lower || "patch" in lower -> "FIXES"
                    else -> ""
                }
                continue
            }

            // Look for bullet items
            if (line.startsWith("-") || line.startsWith("*") || line.startsWith("•") || line.matches(Regex("^\\d+\\..*"))) {
                val clean = line.replaceFirst(Regex("^[-*•\\d.]+\\s*"), "")
                    .replace(Regex("\\[([^\\]]+)\\]\\([^)]+\\)"), "$1") // strip markdown links
                    .trim()

                if (clean.length < 5) continue

                when {
                    currentSection == "BREAKING" || "breaking" in lower -> breaking.add(clean)
                    currentSection == "FEATURES" || "add" in lower || "feat" in lower || "support" in lower -> features.add(clean)
                    currentSection == "FIXES" || "fix" in lower || "bug" in lower || "resolve" in lower || "close" in lower -> fixes.add(clean)
                    else -> other.add(clean)
                }
            }
        }

        val bullets = mutableListOf<String>()

        val topFeature = features.firstOrNull() ?: other.firstOrNull() ?: "Enhanced core application stability and performance."
        bullets.add("• **[FEATURES]** $topFeature")

        val secondFeature = if (features.size > 1) features[1] else null
        if (secondFeature != null) {
            bullets.add("• **[FEATURES]** $secondFeature")
        }

        val topFix = fixes.firstOrNull() ?: other.getOrNull(1) ?: "Resolved edge-case crashes and minor UI glitches."
        bullets.add("• **[FIXES]** $topFix")

        val topBreaking = breaking.firstOrNull() ?: "No breaking changes reported in this release."
        bullets.add("• **[BREAKING]** $topBreaking")

        return bullets.take(4).joinToString("\n")
    }
}
