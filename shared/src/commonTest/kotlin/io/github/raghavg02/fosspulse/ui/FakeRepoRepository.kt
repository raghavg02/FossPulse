package io.github.raghavg02.fosspulse.ui

import io.github.raghavg02.fosspulse.domain.model.ReleaseInfo
import io.github.raghavg02.fosspulse.domain.model.TrackedRepo
import io.github.raghavg02.fosspulse.domain.repository.RepoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeRepoRepository : RepoRepository {
    private val reposFlow = MutableStateFlow<Map<String, TrackedRepo>>(emptyMap())
    private val releasesFlow = MutableStateFlow<Map<String, ReleaseInfo>>(emptyMap())

    override fun getAllActiveReposFlow(): Flow<List<TrackedRepo>> {
        return reposFlow.map { it.values.filter { !it.isDeleted }.toList() }
    }

    override suspend fun getAllActiveRepos(): List<TrackedRepo> {
        return reposFlow.value.values.filter { !it.isDeleted }.toList()
    }

    override suspend fun getRepoById(id: String): TrackedRepo? {
        return reposFlow.value[id]
    }

    override fun getRepoByIdFlow(id: String): Flow<TrackedRepo?> {
        return reposFlow.map { it[id] }
    }

    override suspend fun saveRepo(repo: TrackedRepo) {
        reposFlow.value = reposFlow.value + (repo.id to repo)
    }

    override suspend fun saveRepos(repos: List<TrackedRepo>) {
        reposFlow.value = reposFlow.value + repos.associateBy { it.id }
    }

    override suspend fun updateInstalledVersion(repoId: String, version: String?) {
        val existing = reposFlow.value[repoId] ?: return
        reposFlow.value = reposFlow.value + (repoId to existing.copy(installedVersion = version))
    }

    override suspend fun updateLatestReleaseCheck(repoId: String, latestTag: String, etag: String?, release: ReleaseInfo?) {
        val existing = reposFlow.value[repoId] ?: return
        reposFlow.value = reposFlow.value + (repoId to existing.copy(latestRemoteVersion = latestTag, etag = etag))
        if (release != null) {
            releasesFlow.value = releasesFlow.value + (repoId to release)
        }
    }

    override suspend fun updateLastNotifiedTag(repoId: String, tag: String) {
        val existing = reposFlow.value[repoId] ?: return
        reposFlow.value = reposFlow.value + (repoId to existing.copy(lastNotifiedTag = tag))
    }

    override suspend fun softDeleteRepo(id: String) {
        val existing = reposFlow.value[id] ?: return
        reposFlow.value = reposFlow.value + (id to existing.copy(isDeleted = true))
    }

    override suspend fun deleteRepoPermanent(id: String) {
        reposFlow.value = reposFlow.value - id
        releasesFlow.value = releasesFlow.value - id
    }

    override suspend fun getCachedRelease(repoId: String): ReleaseInfo? {
        return releasesFlow.value[repoId]
    }

    override fun getCachedReleaseFlow(repoId: String): Flow<ReleaseInfo?> {
        return releasesFlow.map { it[repoId] }
    }
}
