package io.github.raghavg02.fosspulse.domain.engine

import io.github.raghavg02.fosspulse.domain.model.ReleaseAsset

object RegexAssetMatcher {

    private const val MAX_REGEX_LENGTH = 200

    private val IGNORED_EXTENSIONS = listOf(
        ".sha256", ".sha512", ".sha1", ".md5",
        ".asc", ".sig", ".txt", ".json",
        ".sbom", ".spdx", ".pem", ".cert"
    )

    fun validatePattern(pattern: String): Result<Regex> {
        if (pattern.isBlank()) {
            return Result.failure(IllegalArgumentException("Regex pattern cannot be blank"))
        }
        if (pattern.length > MAX_REGEX_LENGTH) {
            return Result.failure(
                IllegalArgumentException("Regex pattern exceeds maximum length of $MAX_REGEX_LENGTH characters (ReDoS protection)")
            )
        }
        return runCatching {
            Regex(pattern, RegexOption.IGNORE_CASE)
        }
    }

    fun matchAsset(assets: List<ReleaseAsset>, assetRegex: String): ReleaseAsset? {
        val compiledRegex = validatePattern(assetRegex).getOrNull() ?: return null

        val isTargetingSignature = IGNORED_EXTENSIONS.any { ext -> assetRegex.contains(ext, ignoreCase = true) }

        return assets.firstOrNull { asset ->
            val filename = asset.name
            if (!isTargetingSignature && IGNORED_EXTENSIONS.any { filename.endsWith(it, ignoreCase = true) }) {
                false
            } else {
                compiledRegex.containsMatchIn(filename)
            }
        }
    }
}
