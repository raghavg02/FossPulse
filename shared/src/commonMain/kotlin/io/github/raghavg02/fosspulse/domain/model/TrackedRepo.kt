package io.github.raghavg02.fosspulse.domain.model

data class TrackedRepo(
    val id: String, // canonical "owner/repo" in lowercase
    val owner: String,
    val repoName: String,
    val displayName: String,
    val androidPackageName: String? = null,
    val targetPlatform: TargetPlatform,
    val assetRegex: String,
    val includePrereleases: Boolean = false,
    val installedVersion: String? = null,
    val latestRemoteVersion: String? = null,
    val lastNotifiedTag: String? = null,
    val etag: String? = null,
    val lastCheckedAt: Long = 0L,
    val updatedAt: Long = 0L,
    val isDeleted: Boolean = false
) {
    companion object {
        fun createCanonicalId(owner: String, repo: String): String {
            return "${owner.trim().lowercase()}/${repo.trim().lowercase()}"
        }
    }
}
