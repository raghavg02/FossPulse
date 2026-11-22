package io.github.raghavg02.fosspulse.data.repository

import io.github.raghavg02.fosspulse.data.local.dao.CachedReleaseDao
import io.github.raghavg02.fosspulse.data.local.dao.TrackedRepoDao
import io.github.raghavg02.fosspulse.data.local.entity.CachedReleaseEntity
import io.github.raghavg02.fosspulse.data.local.entity.TrackedRepoEntity
import io.github.raghavg02.fosspulse.domain.model.ReleaseInfo
import io.github.raghavg02.fosspulse.domain.model.TrackedRepo
import io.github.raghavg02.fosspulse.domain.repository.RepoRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class RepoRepositoryImpl(
    private val trackedRepoDao: TrackedRepoDao,
    private val cachedReleaseDao: CachedReleaseDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : RepoRepository {

    override fun getAllActiveReposFlow(): Flow<List<TrackedRepo>> {
        return trackedRepoDao.getAllActiveReposFlow().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getAllActiveRepos(): List<TrackedRepo> = withContext(ioDispatcher) {
        trackedRepoDao.getAllActiveRepos().map { it.toDomain() }
    }

    override suspend fun getRepoById(id: String): TrackedRepo? = withContext(ioDispatcher) {
        trackedRepoDao.getRepoById(id)?.toDomain()
    }

    override fun getRepoByIdFlow(id: String): Flow<TrackedRepo?> {
        return trackedRepoDao.getRepoByIdFlow(id).map { it?.toDomain() }
    }

    override suspend fun saveRepo(repo: TrackedRepo) = withContext(ioDispatcher) {
        val now = currentTimeEpochMs()
        val toSave = repo.copy(updatedAt = if (repo.updatedAt == 0L) now else repo.updatedAt)
        trackedRepoDao.insertRepo(TrackedRepoEntity.fromDomain(toSave))
    }

    override suspend fun saveRepos(repos: List<TrackedRepo>) = withContext(ioDispatcher) {
        val now = currentTimeEpochMs()
        val entities = repos.map { repo ->
            val toSave = repo.copy(updatedAt = if (repo.updatedAt == 0L) now else repo.updatedAt)
            TrackedRepoEntity.fromDomain(toSave)
        }
        trackedRepoDao.insertRepos(entities)
    }

    override suspend fun updateInstalledVersion(repoId: String, version: String?) = withContext(ioDispatcher) {
        val now = currentTimeEpochMs()
        trackedRepoDao.updateInstalledVersion(repoId, version, now)
    }

    override suspend fun updateLatestReleaseCheck(
        repoId: String,
        latestTag: String,
        etag: String?,
        release: ReleaseInfo?
    ) = withContext(ioDispatcher) {
        val now = currentTimeEpochMs()
        trackedRepoDao.updateLatestReleaseCheck(repoId, latestTag, etag, now)
        if (release != null) {
            cachedReleaseDao.insertRelease(CachedReleaseEntity.fromDomain(repoId, release))
        }
    }

    override suspend fun updateLastNotifiedTag(repoId: String, tag: String) = withContext(ioDispatcher) {
        val now = currentTimeEpochMs()
        trackedRepoDao.updateLastNotifiedTag(repoId, tag, now)
    }

    override suspend fun softDeleteRepo(id: String) = withContext(ioDispatcher) {
        val now = currentTimeEpochMs()
        trackedRepoDao.softDeleteRepo(id, now)
    }

    override suspend fun deleteRepoPermanent(id: String) = withContext(ioDispatcher) {
        trackedRepoDao.deleteRepoPermanent(id)
    }

    override suspend fun getCachedRelease(repoId: String): ReleaseInfo? = withContext(ioDispatcher) {
        cachedReleaseDao.getReleaseByRepoId(repoId)?.toDomain()
    }

    override fun getCachedReleaseFlow(repoId: String): Flow<ReleaseInfo?> {
        return cachedReleaseDao.getReleaseByRepoIdFlow(repoId).map { it?.toDomain() }
    }

    private fun currentTimeEpochMs(): Long = kotlin.time.Clock.System.now().toEpochMilliseconds()
}
