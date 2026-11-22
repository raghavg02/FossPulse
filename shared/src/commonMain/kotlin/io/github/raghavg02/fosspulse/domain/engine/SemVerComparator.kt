package io.github.raghavg02.fosspulse.domain.engine

object SemVerComparator : Comparator<String?> {

    override fun compare(v1: String?, v2: String?): Int {
        if (v1 == null && v2 == null) return 0
        if (v1 == null) return -1
        if (v2 == null) return 1

        val parsed1 = parse(v1)
        val parsed2 = parse(v2)

        val maxLen = maxOf(parsed1.coreNumbers.size, parsed2.coreNumbers.size)
        for (i in 0 until maxLen) {
            val num1 = parsed1.coreNumbers.getOrElse(i) { 0L }
            val num2 = parsed2.coreNumbers.getOrElse(i) { 0L }
            if (num1 != num2) {
                return num1.compareTo(num2)
            }
        }

        val pre1 = parsed1.preRelease
        val pre2 = parsed2.preRelease

        // A stable release has higher precedence than a pre-release version: 1.0.0 > 1.0.0-beta
        if (pre1 == null && pre2 != null) return 1
        if (pre1 != null && pre2 == null) return -1
        if (pre1 != null && pre2 != null) {
            val preComp = comparePreRelease(pre1, pre2)
            if (preComp != 0) return preComp
        }

        return 0
    }

    private fun comparePreRelease(pre1: String, pre2: String): Int {
        val parts1 = pre1.split(".", "-")
        val parts2 = pre2.split(".", "-")
        val maxLen = maxOf(parts1.size, parts2.size)

        for (i in 0 until maxLen) {
            val p1 = parts1.getOrNull(i)
            val p2 = parts2.getOrNull(i)

            if (p1 == null) return -1
            if (p2 == null) return 1

            val n1 = p1.toLongOrNull()
            val n2 = p2.toLongOrNull()

            if (n1 != null && n2 != null) {
                if (n1 != n2) return n1.compareTo(n2)
            } else if (n1 != null && n2 == null) {
                return -1
            } else if (n1 == null && n2 != null) {
                return 1
            } else {
                val strComp = p1.compareTo(p2, ignoreCase = true)
                if (strComp != 0) return strComp
            }
        }
        return 0
    }

    private data class ParsedVersion(
        val coreNumbers: List<Long>,
        val preRelease: String?,
        val buildMetadata: String?
    )

    private fun cleanPrefix(version: String): String {
        var s = version.trim()
        val prefixes = listOf(
            "release-", "release_", "release/", "rel-", "rel_",
            "ver-", "ver_", "v.", "v-", "v_", "v"
        )
        for (prefix in prefixes) {
            if (s.startsWith(prefix, ignoreCase = true)) {
                s = s.substring(prefix.length).trim()
                break
            }
        }
        return s
    }

    private fun parse(version: String): ParsedVersion {
        val cleaned = cleanPrefix(version)
        val withoutBuild = cleaned.substringBefore('+')
        val build = if (cleaned.contains('+')) cleaned.substringAfter('+') else null

        val core = withoutBuild.substringBefore('-')
        val pre = if (withoutBuild.contains('-')) withoutBuild.substringAfter('-') else null

        val numberParts = core.split(".")
            .mapNotNull { part ->
                val digits = part.takeWhile { it.isDigit() }
                digits.toLongOrNull()
            }

        return ParsedVersion(
            coreNumbers = if (numberParts.isEmpty()) listOf(0L) else numberParts,
            preRelease = pre,
            buildMetadata = build
        )
    }
}
