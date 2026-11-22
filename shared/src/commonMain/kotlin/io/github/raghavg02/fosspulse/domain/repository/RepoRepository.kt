package io.github.raghavg02.fosspulse.domain.repository

import io.github.raghavg02.fosspulse.domain.model.ReleaseInfo
import io.github.raghavg02.fosspulse.domain.model.TrackedRepo
import kotlinx.coroutines.flow.Flow

interface RepoRepository {
    fun getAllActiveReposFlow(): Flow<List<TrackedRepo>>
    suspend fun getAllActiveRepos(): List<TrackedRepo>
    suspend fun getRepoById(id: String): TrackedRepo?
    fun getRepoByIdFlow(id: String): Flow<TrackedRepo?>
    suspend fun saveRepo(repo: TrackedRepo)
    suspend fun saveRepos(repos: List<TrackedRepo>)
    suspend fun updateInstalledVersion(repoId: String, version: String?)
    suspend fun updateLatestReleaseCheck(repoId: String, latestTag: String, etag: String?, release: ReleaseInfo?)
    suspend fun updateLastNotifiedTag(repoId: String, tag: String)
    suspend fun softDeleteRepo(id: String)
    suspend fun deleteRepoPermanent(id: String)
    suspend fun getCachedRelease(repoId: String): ReleaseInfo?
    fun getCachedReleaseFlow(repoId: String): Flow<ReleaseInfo?>
}
