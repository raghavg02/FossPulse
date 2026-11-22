package io.github.raghavg02.fosspulse.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import io.github.raghavg02.fosspulse.domain.model.ReleaseAsset
import io.github.raghavg02.fosspulse.domain.model.ReleaseInfo

@Entity(
    tableName = "cached_releases",
    foreignKeys = [
        ForeignKey(
            entity = TrackedRepoEntity::class,
            parentColumns = ["id"],
            childColumns = ["repo_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["repo_id"])]
)
data class CachedReleaseEntity(
    @PrimaryKey
    @ColumnInfo(name = "repo_id")
    val repoId: String,

    @ColumnInfo(name = "tag_name")
    val tagName: String,

    @ColumnInfo(name = "release_title")
    val releaseTitle: String,

    @ColumnInfo(name = "raw_changelog_md")
    val rawChangelogMd: String,

    @ColumnInfo(name = "html_url")
    val htmlUrl: String,

    @ColumnInfo(name = "matched_asset_name")
    val matchedAssetName: String? = null,

    @ColumnInfo(name = "matched_asset_url")
    val matchedAssetUrl: String? = null,

    @ColumnInfo(name = "matched_asset_size_bytes")
    val matchedAssetSizeBytes: Long? = null,

    @ColumnInfo(name = "published_at")
    val publishedAt: Long
) {
    fun toDomain(): ReleaseInfo = ReleaseInfo(
        tagName = tagName,
        releaseTitle = releaseTitle,
        rawChangelogMd = rawChangelogMd,
        htmlUrl = htmlUrl,
        publishedAtEpochMs = publishedAt,
        matchedAsset = if (matchedAssetName != null && matchedAssetUrl != null) {
            ReleaseAsset(
                name = matchedAssetName,
                downloadUrl = matchedAssetUrl,
                sizeBytes = matchedAssetSizeBytes ?: 0L
            )
        } else {
            null
        }
    )

    companion object {
        fun fromDomain(repoId: String, domain: ReleaseInfo): CachedReleaseEntity = CachedReleaseEntity(
            repoId = repoId,
            tagName = domain.tagName,
            releaseTitle = domain.releaseTitle,
            rawChangelogMd = domain.rawChangelogMd,
            htmlUrl = domain.htmlUrl,
            matchedAssetName = domain.matchedAsset?.name,
            matchedAssetUrl = domain.matchedAsset?.downloadUrl,
            matchedAssetSizeBytes = domain.matchedAsset?.sizeBytes,
            publishedAt = domain.publishedAtEpochMs
        )
    }
}
