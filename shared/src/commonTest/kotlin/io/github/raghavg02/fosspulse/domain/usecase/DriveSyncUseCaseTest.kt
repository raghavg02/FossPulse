package io.github.raghavg02.fosspulse.domain.usecase

import io.github.raghavg02.fosspulse.data.remote.GoogleDriveApiService
import io.github.raghavg02.fosspulse.domain.model.TargetPlatform
import io.github.raghavg02.fosspulse.domain.model.TrackedRepo
import io.github.raghavg02.fosspulse.domain.sync.SyncPayload
import io.github.raghavg02.fosspulse.domain.sync.SyncRepoItem
import io.github.raghavg02.fosspulse.ui.FakeRepoRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class FakeGoogleDriveApiService : GoogleDriveApiService {
    var fileId: String? = null
    var storedPayload: SyncPayload? = null

    override suspend fun findSyncFile(accessToken: String): String? {
        return fileId
    }

    override suspend fun downloadSyncFile(accessToken: String, fileId: String): SyncPayload? {
        return storedPayload
    }

    override suspend fun uploadSyncFile(accessToken: String, payload: SyncPayload, existingFileId: String?): String {
        storedPayload = payload
        val id = existingFileId ?: "drive_file_123"
        fileId = id
        return id
    }
}

class DriveSyncUseCaseTest {

    @Test
    fun testMergeRemoteIntoEmptyLocal() = runTest {
        val repoRepository = FakeRepoRepository()
        val driveApi = FakeGoogleDriveApiService()
        val useCase = DriveSyncUseCase(repoRepository, driveApi)

        driveApi.fileId = "remote_sync_file"
        driveApi.storedPayload = SyncPayload(
            syncedAt = 1000L,
            repos = listOf(
                SyncRepoItem(
                    id = "imranr98/obtainium",
                    owner = "ImranR98",
                    repoName = "Obtainium",
                    displayName = "Obtainium",
                    targetPlatform = "ANDROID",
                    assetRegex = ".*\\.apk$",
                    updatedAt = 1000L
                )
            )
        )

        val result = useCase.execute("mock_access_token")
        assertTrue(result.isSuccess)

        val localRepo = repoRepository.getRepoById("imranr98/obtainium")
        assertNotNull(localRepo)
        assertEquals("Obtainium", localRepo.displayName)
        assertNull(localRepo.installedVersion) // local installed version remains null
        assertNull(localRepo.etag)
    }

    @Test
    fun testPreserveLocalInstalledVersionDuringUpdate() = runTest {
        val repoRepository = FakeRepoRepository()
        val driveApi = FakeGoogleDriveApiService()
        val useCase = DriveSyncUseCase(repoRepository, driveApi)

        // Local repo has installedVersion = "v1.2.3" and etag = "etag_local"
        val existingLocal = TrackedRepo(
            id = "user/app",
            owner = "user",
            repoName = "app",
            displayName = "Old Name",
            targetPlatform = TargetPlatform.ANDROID,
            assetRegex = ".*\\.apk$",
            installedVersion = "v1.2.3",
            etag = "etag_local",
            updatedAt = 500L
        )
        repoRepository.saveRepo(existingLocal)

        // Remote has newer update with updated display name
        driveApi.fileId = "remote_sync_file"
        driveApi.storedPayload = SyncPayload(
            syncedAt = 1500L,
            repos = listOf(
                SyncRepoItem(
                    id = "user/app",
                    owner = "user",
                    repoName = "app",
                    displayName = "New Name",
                    targetPlatform = "ANDROID",
                    assetRegex = ".*\\.apk$",
                    updatedAt = 1500L
                )
            )
        )

        val result = useCase.execute("mock_access_token")
        assertTrue(result.isSuccess)

        val updatedLocal = repoRepository.getRepoById("user/app")
        assertNotNull(updatedLocal)
        assertEquals("New Name", updatedLocal.displayName)
        assertEquals("v1.2.3", updatedLocal.installedVersion) // PRESERVED!
        assertEquals("etag_local", updatedLocal.etag) // PRESERVED!
    }

    @Test
    fun testRemoteDeletedRepoSoftDeletesLocal() = runTest {
        val repoRepository = FakeRepoRepository()
        val driveApi = FakeGoogleDriveApiService()
        val useCase = DriveSyncUseCase(repoRepository, driveApi)

        val existingLocal = TrackedRepo(
            id = "user/deleted-app",
            owner = "user",
            repoName = "deleted-app",
            displayName = "Deleted App",
            targetPlatform = TargetPlatform.ANDROID,
            assetRegex = ".*\\.apk$",
            updatedAt = 500L
        )
        repoRepository.saveRepo(existingLocal)

        driveApi.fileId = "remote_sync_file"
        driveApi.storedPayload = SyncPayload(
            syncedAt = 1500L,
            repos = listOf(
                SyncRepoItem(
                    id = "user/deleted-app",
                    owner = "user",
                    repoName = "deleted-app",
                    displayName = "Deleted App",
                    targetPlatform = "ANDROID",
                    assetRegex = ".*\\.apk$",
                    updatedAt = 1500L,
                    isDeleted = true
                )
            )
        )

        val result = useCase.execute("mock_access_token")
        assertTrue(result.isSuccess)

        val active = repoRepository.getAllActiveRepos()
        assertTrue(active.none { it.id == "user/deleted-app" })
    }
}
