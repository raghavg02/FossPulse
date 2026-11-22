package io.github.raghavg02.fosspulse.domain.usecase

import io.github.raghavg02.fosspulse.data.remote.GoogleDriveApiService
import io.github.raghavg02.fosspulse.domain.model.TargetPlatform
import io.github.raghavg02.fosspulse.domain.model.TrackedRepo
import io.github.raghavg02.fosspulse.domain.repository.RepoRepository
import io.github.raghavg02.fosspulse.domain.sync.SyncPayload
import io.github.raghavg02.fosspulse.domain.sync.SyncRepoItem
import kotlin.time.Clock

class DriveSyncUseCase(
    private val repoRepository: RepoRepository,
    private val googleDriveApiService: GoogleDriveApiService
) {

    suspend fun execute(accessToken: String): Result<SyncPayload> {
        return try {
            val existingFileId = googleDriveApiService.findSyncFile(accessToken)
            val remotePayload = if (existingFileId != null) {
                googleDriveApiService.downloadSyncFile(accessToken, existingFileId)
            } else {
                null
            }

            val localRepos = repoRepository.getAllActiveRepos()
            val localMap = localRepos.associateBy { it.id }.toMutableMap()
            val remoteRepoMap = (remotePayload?.repos ?: emptyList()).associateBy { it.id }.toMutableMap()

            // 1. Reconcile remote into local
            for ((remoteId, remoteItem) in remoteRepoMap) {
                val local = localMap[remoteId]
                if (local == null) {
                    if (!remoteItem.isDeleted) {
                        val platform = try {
                            TargetPlatform.valueOf(remoteItem.targetPlatform)
                        } catch (_: Exception) {
                            TargetPlatform.ANDROID
                        }
                        val newRepo = TrackedRepo(
                            id = remoteItem.id,
                            owner = remoteItem.owner,
                            repoName = remoteItem.repoName,
                            displayName = remoteItem.displayName,
                            androidPackageName = remoteItem.androidPackageName,
                            targetPlatform = platform,
                            assetRegex = remoteItem.assetRegex,
                            includePrereleases = remoteItem.includePrereleases,
                            installedVersion = null,
                            latestRemoteVersion = null,
                            etag = null,
                            lastNotifiedTag = null,
                            updatedAt = remoteItem.updatedAt,
                            isDeleted = false
                        )
                        repoRepository.saveRepo(newRepo)
                        localMap[remoteId] = newRepo
                    }
                } else {
                    if (remoteItem.isDeleted) {
                        if (remoteItem.updatedAt >= local.updatedAt) {
                            repoRepository.softDeleteRepo(local.id)
                            localMap.remove(local.id)
                        }
                    } else if (remoteItem.updatedAt > local.updatedAt) {
                        val platform = try {
                            TargetPlatform.valueOf(remoteItem.targetPlatform)
                        } catch (_: Exception) {
                            local.targetPlatform
                        }
                        val updatedLocal = local.copy(
                            displayName = remoteItem.displayName,
                            owner = remoteItem.owner,
                            repoName = remoteItem.repoName,
                            androidPackageName = remoteItem.androidPackageName,
                            targetPlatform = platform,
                            assetRegex = remoteItem.assetRegex,
                            includePrereleases = remoteItem.includePrereleases,
                            updatedAt = remoteItem.updatedAt
                            // installedVersion, etag, and lastNotifiedTag remain local
                        )
                        repoRepository.saveRepo(updatedLocal)
                        localMap[remoteId] = updatedLocal
                    }
                }
            }

            // 2. Reconcile local into remote payload
            for ((localId, local) in localMap) {
                val remoteItem = remoteRepoMap[localId]
                if (remoteItem == null || local.updatedAt > remoteItem.updatedAt) {
                    remoteRepoMap[localId] = SyncRepoItem(
                        id = local.id,
                        owner = local.owner,
                        repoName = local.repoName,
                        displayName = local.displayName,
                        androidPackageName = local.androidPackageName,
                        targetPlatform = local.targetPlatform.name,
                        assetRegex = local.assetRegex,
                        includePrereleases = local.includePrereleases,
                        updatedAt = local.updatedAt,
                        isDeleted = false
                    )
                }
            }

            val now = Clock.System.now().toEpochMilliseconds()
            val mergedPayload = SyncPayload(
                schemaVersion = 1,
                syncedAt = now,
                repos = remoteRepoMap.values.toList()
            )

            googleDriveApiService.uploadSyncFile(accessToken, mergedPayload, existingFileId)
            Result.success(mergedPayload)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
