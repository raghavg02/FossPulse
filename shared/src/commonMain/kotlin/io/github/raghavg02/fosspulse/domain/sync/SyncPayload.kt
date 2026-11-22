package io.github.raghavg02.fosspulse.domain.sync

import kotlinx.serialization.Serializable

@Serializable
data class SyncPayload(
    val schemaVersion: Int = 1,
    val syncedAt: Long,
    val repos: List<SyncRepoItem>
)

@Serializable
data class SyncRepoItem(
    val id: String,
    val owner: String,
    val repoName: String,
    val displayName: String,
    val androidPackageName: String? = null,
    val targetPlatform: String,
    val assetRegex: String,
    val includePrereleases: Boolean = false,
    val updatedAt: Long,
    val isDeleted: Boolean = false
)
