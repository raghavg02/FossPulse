package io.github.raghavg02.fosspulse.ui.dashboard

import io.github.raghavg02.fosspulse.data.remote.GitHubApiService
import io.github.raghavg02.fosspulse.domain.model.NetworkResult
import io.github.raghavg02.fosspulse.domain.model.ReleaseAsset
import io.github.raghavg02.fosspulse.domain.model.ReleaseInfo
import io.github.raghavg02.fosspulse.domain.model.TargetPlatform
import io.github.raghavg02.fosspulse.domain.model.TrackedRepo
import io.github.raghavg02.fosspulse.ui.FakeRepoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeRepository: FakeRepoRepository
    private lateinit var fakeApiService: FakeGitHubApiService
    private lateinit var viewModel: DashboardViewModel

    private val sampleRepo = TrackedRepo(
        id = "imranr98/obtainium",
        owner = "ImranR98",
        repoName = "Obtainium",
        displayName = "Obtainium",
        targetPlatform = TargetPlatform.ANDROID,
        assetRegex = ".*arm64.*\\.apk$",
        installedVersion = "1.1.2",
        latestRemoteVersion = "1.1.4"
    )

    private val sampleIosRepo = TrackedRepo(
        id = "owner/iosapp",
        owner = "owner",
        repoName = "iosapp",
        displayName = "iOS App",
        targetPlatform = TargetPlatform.IOS,
        assetRegex = ".*\\.ipa$",
        installedVersion = "2.0.0",
        latestRemoteVersion = "2.0.0"
    )

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeRepoRepository()
        fakeApiService = FakeGitHubApiService()
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testInitialLoadingAndObservingRepos() = runTest(testDispatcher) {
        fakeRepository.saveRepo(sampleRepo)
        fakeRepository.saveRepo(sampleIosRepo)

        viewModel = DashboardViewModel(fakeRepository, fakeApiService)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(2, state.repos.size)
        assertEquals(2, state.filteredRepos.size)
    }

    @Test
    fun testPlatformFilterRow() = runTest(testDispatcher) {
        fakeRepository.saveRepo(sampleRepo)
        fakeRepository.saveRepo(sampleIosRepo)

        viewModel = DashboardViewModel(fakeRepository, fakeApiService)
        advanceUntilIdle()

        // Filter by ANDROID
        viewModel.onEvent(DashboardUiEvent.FilterByPlatform(TargetPlatform.ANDROID))
        advanceUntilIdle()
        assertEquals(TargetPlatform.ANDROID, viewModel.uiState.value.selectedPlatformFilter)
        assertEquals(1, viewModel.uiState.value.filteredRepos.size)
        assertEquals("imranr98/obtainium", viewModel.uiState.value.filteredRepos.first().repo.id)

        // Filter by IOS
        viewModel.onEvent(DashboardUiEvent.FilterByPlatform(TargetPlatform.IOS))
        advanceUntilIdle()
        assertEquals(TargetPlatform.IOS, viewModel.uiState.value.selectedPlatformFilter)
        assertEquals(1, viewModel.uiState.value.filteredRepos.size)
        assertEquals("owner/iosapp", viewModel.uiState.value.filteredRepos.first().repo.id)

        // Filter by ALL (null)
        viewModel.onEvent(DashboardUiEvent.FilterByPlatform(null))
        advanceUntilIdle()
        assertEquals(2, viewModel.uiState.value.filteredRepos.size)
    }

    @Test
    fun testSelectRepoAndDismiss() = runTest(testDispatcher) {
        fakeRepository.saveRepo(sampleRepo)
        viewModel = DashboardViewModel(fakeRepository, fakeApiService)
        advanceUntilIdle()

        val item = viewModel.uiState.value.repos.first()
        viewModel.onEvent(DashboardUiEvent.SelectRepo(item))
        assertEquals(item, viewModel.uiState.value.selectedRepoForDetail)

        viewModel.onEvent(DashboardUiEvent.DismissDetail)
        assertNull(viewModel.uiState.value.selectedRepoForDetail)
    }

    @Test
    fun testDeleteRepo() = runTest(testDispatcher) {
        fakeRepository.saveRepo(sampleRepo)
        viewModel = DashboardViewModel(fakeRepository, fakeApiService)
        advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.repos.size)

        viewModel.onEvent(DashboardUiEvent.DeleteRepo(sampleRepo.id))
        advanceUntilIdle()

        assertEquals(0, viewModel.uiState.value.repos.size)
    }

    @Test
    fun testRefreshAllUpdatesLatestRelease() = runTest(testDispatcher) {
        fakeRepository.saveRepo(sampleRepo)

        val updatedRelease = ReleaseInfo(
            tagName = "v1.2.0",
            releaseTitle = "v1.2.0 Release",
            rawChangelogMd = "New UI",
            htmlUrl = "https://github.com/imranr98/obtainium/releases/tag/v1.2.0",
            publishedAtEpochMs = 1700000000000L,
            matchedAsset = ReleaseAsset("obtainium.apk", "https://example.com/dl", 1000L)
        )
        fakeApiService.nextResult = NetworkResult.Success(
            data = updatedRelease,
            etag = "\"new-etag\"",
            rateLimitRemaining = 55
        )

        viewModel = DashboardViewModel(fakeRepository, fakeApiService)
        advanceUntilIdle()

        viewModel.onEvent(DashboardUiEvent.RefreshAll)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(55, state.rateLimitRemaining)
        val repo = fakeRepository.getRepoById(sampleRepo.id)
        assertEquals("v1.2.0", repo?.latestRemoteVersion)
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
    }
}
