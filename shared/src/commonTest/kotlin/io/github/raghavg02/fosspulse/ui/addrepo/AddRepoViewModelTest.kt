package io.github.raghavg02.fosspulse.ui.addrepo

import io.github.raghavg02.fosspulse.data.remote.GitHubApiService
import io.github.raghavg02.fosspulse.domain.model.NetworkResult
import io.github.raghavg02.fosspulse.domain.model.ReleaseInfo
import io.github.raghavg02.fosspulse.domain.model.TargetPlatform
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
class AddRepoViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeRepository: FakeRepoRepository
    private lateinit var fakeApiService: FakeGitHubApiService
    private lateinit var viewModel: AddRepoViewModel

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeRepoRepository()
        fakeApiService = FakeGitHubApiService()
        viewModel = AddRepoViewModel(fakeRepository, fakeApiService)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testPlatformSelectionUpdatesDefaultRegex() {
        viewModel.onPlatformChanged(TargetPlatform.ANDROID)
        assertTrue(viewModel.uiState.value.assetRegex.contains("apk"))

        viewModel.onPlatformChanged(TargetPlatform.WINDOWS)
        assertTrue(viewModel.uiState.value.assetRegex.contains("exe"))

        viewModel.onPlatformChanged(TargetPlatform.MACOS)
        assertTrue(viewModel.uiState.value.assetRegex.contains("dmg"))

        viewModel.onPlatformChanged(TargetPlatform.IOS)
        assertTrue(viewModel.uiState.value.assetRegex.contains("ipa"))
    }

    @Test
    fun testInputUrlCleaningAndDisplayNameInference() {
        viewModel.onRepoInputChanged("https://github.com/imranr98/obtainium")
        assertEquals("https://github.com/imranr98/obtainium", viewModel.uiState.value.repoInput)
        assertEquals("Obtainium", viewModel.uiState.value.displayName)
    }

    @Test
    fun testValidationFailsOnInvalidRepoFormat() = runTest(testDispatcher) {
        viewModel.onRepoInputChanged("invalid_single_token")
        var successCalled = false
        viewModel.saveRepository { successCalled = true }
        advanceUntilIdle()

        assertFalse(successCalled)
        assertNotNull(viewModel.uiState.value.errorMessage)
        assertTrue(viewModel.uiState.value.errorMessage!!.contains("Invalid repository format"))
    }

    @Test
    fun testValidationFailsOnInvalidRegex() = runTest(testDispatcher) {
        viewModel.onRepoInputChanged("owner/repo")
        viewModel.onRegexChanged("[unclosed_bracket")
        var successCalled = false
        viewModel.saveRepository { successCalled = true }
        advanceUntilIdle()

        assertFalse(successCalled)
        assertNotNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun testSaveRepositorySuccess() = runTest(testDispatcher) {
        viewModel.onRepoInputChanged("imranr98/obtainium")
        viewModel.onPlatformChanged(TargetPlatform.ANDROID)

        fakeApiService.nextResult = NetworkResult.Success(
            data = ReleaseInfo(
                tagName = "v1.1.4",
                releaseTitle = "v1.1.4",
                rawChangelogMd = "",
                htmlUrl = "https://github.com",
                publishedAtEpochMs = 1700000000000L
            ),
            etag = "\"etag-val\""
        )

        var successCalled = false
        viewModel.saveRepository { successCalled = true }
        advanceUntilIdle()

        assertTrue(successCalled)
        assertNull(viewModel.uiState.value.errorMessage)

        val saved = fakeRepository.getRepoById("imranr98/obtainium")
        assertNotNull(saved)
        assertEquals("imranr98", saved.owner)
        assertEquals("obtainium", saved.repoName)
        assertEquals(TargetPlatform.ANDROID, saved.targetPlatform)
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
