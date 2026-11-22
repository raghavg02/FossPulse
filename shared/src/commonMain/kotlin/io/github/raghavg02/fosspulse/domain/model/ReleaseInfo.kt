package io.github.raghavg02.fosspulse.domain.model

data class ReleaseInfo(
    val tagName: String,
    val releaseTitle: String,
    val rawChangelogMd: String,
    val htmlUrl: String,
    val publishedAtEpochMs: Long,
    val matchedAsset: ReleaseAsset? = null,
    val isPrerelease: Boolean = false
)
