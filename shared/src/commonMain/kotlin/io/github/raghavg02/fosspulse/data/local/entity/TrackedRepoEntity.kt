package io.github.raghavg02.fosspulse.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import io.github.raghavg02.fosspulse.domain.model.TargetPlatform
import io.github.raghavg02.fosspulse.domain.model.TrackedRepo

@Entity(tableName = "tracked_repos")
data class TrackedRepoEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String, // canonical "owner/repo" in lowercase

    @ColumnInfo(name = "owner")
    val owner: String,

    @ColumnInfo(name = "repo_name")
    val repoName: String,

    @ColumnInfo(name = "display_name")
    val displayName: String,

    @ColumnInfo(name = "android_package_name")
    val androidPackageName: String? = null,

    @ColumnInfo(name = "target_platform")
    val targetPlatform: String, // "ANDROID", "IOS", "WINDOWS", "MACOS"

    @ColumnInfo(name = "asset_regex")
    val assetRegex: String,

    @ColumnInfo(name = "include_prereleases")
    val includePrereleases: Boolean = false,

    @ColumnInfo(name = "installed_version")
    val installedVersion: String? = null,

    @ColumnInfo(name = "latest_remote_version")
    val latestRemoteVersion: String? = null,

    @ColumnInfo(name = "last_notified_tag")
    val lastNotifiedTag: String? = null,

    @ColumnInfo(name = "etag")
    val etag: String? = null,

    @ColumnInfo(name = "last_checked_at")
    val lastCheckedAt: Long = 0L,

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = 0L,

    @ColumnInfo(name = "is_deleted")
    val isDeleted: Boolean = false
) {
    fun toDomain(): TrackedRepo = TrackedRepo(
        id = id,
        owner = owner,
        repoName = repoName,
        displayName = displayName,
        androidPackageName = androidPackageName,
        targetPlatform = TargetPlatform.fromString(targetPlatform),
        assetRegex = assetRegex,
        includePrereleases = includePrereleases,
        installedVersion = installedVersion,
        latestRemoteVersion = latestRemoteVersion,
        lastNotifiedTag = lastNotifiedTag,
        etag = etag,
        lastCheckedAt = lastCheckedAt,
        updatedAt = updatedAt,
        isDeleted = isDeleted
    )

    companion object {
        fun fromDomain(domain: TrackedRepo): TrackedRepoEntity = TrackedRepoEntity(
            id = domain.id,
            owner = domain.owner,
            repoName = domain.repoName,
            displayName = domain.displayName,
            androidPackageName = domain.androidPackageName,
            targetPlatform = domain.targetPlatform.name,
            assetRegex = domain.assetRegex,
            includePrereleases = domain.includePrereleases,
            installedVersion = domain.installedVersion,
            latestRemoteVersion = domain.latestRemoteVersion,
            lastNotifiedTag = domain.lastNotifiedTag,
            etag = domain.etag,
            lastCheckedAt = domain.lastCheckedAt,
            updatedAt = domain.updatedAt,
            isDeleted = domain.isDeleted
        )
    }
}
