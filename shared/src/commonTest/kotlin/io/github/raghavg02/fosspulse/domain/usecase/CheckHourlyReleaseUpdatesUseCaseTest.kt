package io.github.raghavg02.fosspulse.domain.usecase

import io.github.raghavg02.fosspulse.data.remote.GitHubApiService
import io.github.raghavg02.fosspulse.domain.model.NetworkResult
import io.github.raghavg02.fosspulse.domain.model.ReleaseAsset
import io.github.raghavg02.fosspulse.domain.model.ReleaseInfo
import io.github.raghavg02.fosspulse.domain.model.TargetPlatform
import io.github.raghavg02.fosspulse.domain.model.TrackedRepo
import io.github.raghavg02.fosspulse.ui.FakeRepoRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class CheckHourlyReleaseUpdatesUseCaseTest {

    private lateinit var fakeRepository: FakeRepoRepository
    private lateinit var fakeApiService: FakeGitHubApiService
    private lateinit var useCase: CheckHourlyReleaseUpdatesUseCase

    private val baseRepo = TrackedRepo(
        id = "imranr98/obtainium",
        owner = "ImranR98",
        repoName = "Obtainium",
        displayName = "Obtainium",
        androidPackageName = "dev.imranr.obtainium",
        targetPlatform = TargetPlatform.ANDROID,
        assetRegex = ".*arm64.*\\.apk$",
        installedVersion = "1.1.2",
        latestRemoteVersion = "1.1.2",
        lastNotifiedTag = "1.1.2",
        etag = "\"old-etag\""
    )

    private val matchedAsset = ReleaseAsset(
        name = "obtainium-arm64.apk",
        downloadUrl = "https://example.com/dl.apk",
        sizeBytes = 15000000L
    )

    @BeforeTest
    fun setUp() {
        fakeRepository = FakeRepoRepository()
        fakeApiService = FakeGitHubApiService()
        useCase = CheckHourlyReleaseUpdatesUseCase(
            repoRepository = fakeRepository,
            gitHubApiService = fakeApiService
        )
    }

    @Test
    fun testNotModifiedSkipsSilently() = runTest {
        fakeRepository.saveRepo(baseRepo)
        fakeApiService.nextResult = NetworkResult.NotModified(etag = "\"old-etag\"")

        val summary = useCase()
        assertEquals(1, summary.checkedCount)
        assertEquals(1, summary.notModifiedCount)
        assertEquals(0, summary.updatedAvailableCount)
        assertEquals(0, summary.alreadyUpdatedCount)
    }

    @Test
    fun testBinaryGateSuppressesNotificationWhenNoBinaryAttached() = runTest {
        fakeRepository.saveRepo(baseRepo)
        // Release has a new tag "1.1.4", but no matched binary (e.g. CI still compiling APK)
        val releaseWithoutBinary = ReleaseInfo(
            tagName = "1.1.4",
            releaseTitle = "v1.1.4",
            rawChangelogMd = "",
            htmlUrl = "https://github.com",
            publishedAtEpochMs = 1700000000000L,
            matchedAsset = null
        )
        fakeApiService.nextResult = NetworkResult.Success(
            data = releaseWithoutBinary,
            etag = "\"new-etag\""
        )

        val summary = useCase()
        assertEquals(1, summary.checkedCount)
        assertEquals(1, summary.binarySuppressedCount)
        assertEquals(0, summary.updatedAvailableCount)

        // Verifies repo's lastNotifiedTag was NOT updated, ensuring next cycle can still notify once binary is uploaded
        val updatedRepo = fakeRepository.getRepoById(baseRepo.id)
        assertEquals("1.1.2", updatedRepo?.lastNotifiedTag)
    }

    @Test
    fun testUpdateAvailableBranchNotifiesAndUpdatesLastNotifiedTag() = runTest {
        fakeRepository.saveRepo(baseRepo.copy(installedVersion = "1.1.2", lastNotifiedTag = "1.1.2"))

        val releaseWithBinary = ReleaseInfo(
            tagName = "1.1.4",
            releaseTitle = "v1.1.4",
            rawChangelogMd = "",
            htmlUrl = "https://github.com",
            publishedAtEpochMs = 1700000000000L,
            matchedAsset = matchedAsset
        )
        fakeApiService.nextResult = NetworkResult.Success(
            data = releaseWithBinary,
            etag = "\"new-etag\""
        )

        val summary = useCase()
        assertEquals(1, summary.updatedAvailableCount)
        assertEquals(0, summary.alreadyUpdatedCount)

        val updatedRepo = fakeRepository.getRepoById(baseRepo.id)
        assertEquals("1.1.4", updatedRepo?.lastNotifiedTag)
        assertEquals("1.1.4", updatedRepo?.latestRemoteVersion)
    }

    @Test
    fun testAlreadyUpdatedWhatsNewBranchNotifies() = runTest {
        // User already has v1.1.4 installed on their device
        fakeRepository.saveRepo(baseRepo.copy(installedVersion = "1.1.4", lastNotifiedTag = "1.1.2"))

        val releaseWithBinary = ReleaseInfo(
            tagName = "1.1.4",
            releaseTitle = "v1.1.4",
            rawChangelogMd = "",
            htmlUrl = "https://github.com",
            publishedAtEpochMs = 1700000000000L,
            matchedAsset = matchedAsset
        )
        fakeApiService.nextResult = NetworkResult.Success(
            data = releaseWithBinary,
            etag = "\"new-etag\""
        )

        val summary = useCase()
        assertEquals(0, summary.updatedAvailableCount)
        assertEquals(1, summary.alreadyUpdatedCount)

        val updatedRepo = fakeRepository.getRepoById(baseRepo.id)
        assertEquals("1.1.4", updatedRepo?.lastNotifiedTag)
    }

    @Test
    fun testDuplicateTagSuppressed() = runTest {
        // Tag 1.1.4 was already notified previously
        fakeRepository.saveRepo(baseRepo.copy(installedVersion = "1.1.2", lastNotifiedTag = "1.1.4"))

        val releaseWithBinary = ReleaseInfo(
            tagName = "1.1.4",
            releaseTitle = "v1.1.4",
            rawChangelogMd = "",
            htmlUrl = "https://github.com",
            publishedAtEpochMs = 1700000000000L,
            matchedAsset = matchedAsset
        )
        fakeApiService.nextResult = NetworkResult.Success(
            data = releaseWithBinary,
            etag = "\"new-etag\""
        )

        val summary = useCase()
        assertEquals(0, summary.updatedAvailableCount)
        assertEquals(0, summary.alreadyUpdatedCount)
    }

    private class FakeGitHubApiService : GitHubApiService {
        var nextResult: NetworkResult<ReleaseInfo>? = null

        override suspend fun getLatestRelease(
            owner: String,
            repo: String,
            assetRegex: String,
            etag: String?,
            includePrereleases: Boolean,
            personalAccessToken: String?
        ): NetworkResult<ReleaseInfo> {
            return nextResult ?: NetworkResult.Error("No mock configured")
        }

        override suspend fun getRateLimit(personalAccessToken: String?): NetworkResult<Int> {
            return NetworkResult.Success(60)
        }
    }
}
