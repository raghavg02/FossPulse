package io.github.raghavg02.fosspulse.data.remote.dto

import io.github.raghavg02.fosspulse.domain.model.ReleaseAsset
import io.github.raghavg02.fosspulse.domain.model.ReleaseInfo
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GitHubReleaseDto(
    @SerialName("tag_name")
    val tagName: String,
    @SerialName("name")
    val name: String? = null,
    @SerialName("body")
    val body: String? = null,
    @SerialName("html_url")
    val htmlUrl: String,
    @SerialName("published_at")
    val publishedAt: String? = null,
    @SerialName("prerelease")
    val prerelease: Boolean = false,
    @SerialName("assets")
    val assets: List<GitHubAssetDto> = emptyList()
) {
    fun toDomain(matchedAsset: ReleaseAsset? = null): ReleaseInfo {
        val epochMs = try {
            if (publishedAt != null) {
                kotlin.time.Instant.parse(publishedAt).toEpochMilliseconds()
            } else {
                0L
            }
        } catch (_: Exception) {
            0L
        }
        return ReleaseInfo(
            tagName = tagName,
            releaseTitle = name ?: tagName,
            rawChangelogMd = body ?: "",
            htmlUrl = htmlUrl,
            publishedAtEpochMs = epochMs,
            matchedAsset = matchedAsset,
            isPrerelease = prerelease
        )
    }
}

@Serializable
data class GitHubAssetDto(
    @SerialName("name")
    val name: String,
    @SerialName("browser_download_url")
    val browserDownloadUrl: String,
    @SerialName("size")
    val size: Long,
    @SerialName("content_type")
    val contentType: String? = null
) {
    fun toDomain(): ReleaseAsset = ReleaseAsset(
        name = name,
        downloadUrl = browserDownloadUrl,
        sizeBytes = size,
        contentType = contentType
    )
}

@Serializable
data class GitHubTagDto(
    @SerialName("name")
    val name: String,
    @SerialName("commit")
    val commit: GitHubTagCommitDto? = null
)

@Serializable
data class GitHubTagCommitDto(
    @SerialName("sha")
    val sha: String,
    @SerialName("url")
    val url: String
)
