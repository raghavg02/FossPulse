package io.github.raghavg02.fosspulse.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.raghavg02.fosspulse.data.remote.GitHubApiService
import io.github.raghavg02.fosspulse.domain.model.NetworkResult
import io.github.raghavg02.fosspulse.domain.model.TargetPlatform
import io.github.raghavg02.fosspulse.domain.repository.RepoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DashboardViewModel(
    private val repoRepository: RepoRepository,
    private val gitHubApiService: GitHubApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        observeRepos()
    }

    private fun observeRepos() {
        viewModelScope.launch {
            repoRepository.getAllActiveReposFlow().collect { activeRepos ->
                val items = activeRepos.map { repo ->
                    val release = repoRepository.getCachedRelease(repo.id)
                    TrackedRepoItem(repo = repo, release = release)
                }
                _uiState.update { current ->
                    current.copy(
                        isLoading = false,
                        repos = items,
                        selectedRepoForDetail = if (current.selectedRepoForDetail != null) {
                            items.firstOrNull { it.repo.id == current.selectedRepoForDetail.repo.id }
                        } else {
                            null
                        }
                    )
                }
            }
        }
    }

    fun onEvent(event: DashboardUiEvent) {
        when (event) {
            is DashboardUiEvent.RefreshAll -> refreshAllRepos()
            is DashboardUiEvent.FilterByPlatform -> filterPlatform(event.platform)
            is DashboardUiEvent.SelectRepo -> _uiState.update { it.copy(selectedRepoForDetail = event.item) }
            is DashboardUiEvent.DismissDetail -> _uiState.update { it.copy(selectedRepoForDetail = null) }
            is DashboardUiEvent.ShowAddRepo -> _uiState.update { it.copy(isAddRepoSheetVisible = true) }
            is DashboardUiEvent.DismissAddRepo -> _uiState.update { it.copy(isAddRepoSheetVisible = false) }
            is DashboardUiEvent.DeleteRepo -> deleteRepo(event.repoId)
            is DashboardUiEvent.RefreshSingleRepo -> refreshSingleRepo(event.repoId)
        }
    }

    private fun filterPlatform(platform: TargetPlatform?) {
        _uiState.update { it.copy(selectedPlatformFilter = platform) }
    }

    private fun deleteRepo(repoId: String) {
        viewModelScope.launch {
            repoRepository.softDeleteRepo(repoId)
            if (_uiState.value.selectedRepoForDetail?.repo?.id == repoId) {
                _uiState.update { it.copy(selectedRepoForDetail = null) }
            }
        }
    }

    fun refreshAllRepos() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true, errorMessage = null) }
            val repos = repoRepository.getAllActiveRepos()

            for (repo in repos) {
                checkAndUpdateRepo(repo.owner, repo.repoName, repo.assetRegex, repo.etag, repo.includePrereleases, repo.id)
            }

            _uiState.update { it.copy(isRefreshing = false) }
        }
    }

    private fun refreshSingleRepo(repoId: String) {
        viewModelScope.launch {
            val repo = repoRepository.getRepoById(repoId) ?: return@launch
            checkAndUpdateRepo(repo.owner, repo.repoName, repo.assetRegex, repo.etag, repo.includePrereleases, repo.id)
        }
    }

    private suspend fun checkAndUpdateRepo(
        owner: String,
        repoName: String,
        assetRegex: String,
        etag: String?,
        includePrereleases: Boolean,
        repoId: String
    ) {
        when (val result = gitHubApiService.getLatestRelease(
            owner = owner,
            repo = repoName,
            assetRegex = assetRegex,
            etag = etag,
            includePrereleases = includePrereleases
        )) {
            is NetworkResult.Success -> {
                repoRepository.updateLatestReleaseCheck(
                    repoId = repoId,
                    latestTag = result.data.tagName,
                    etag = result.etag,
                    release = result.data
                )
                if (result.rateLimitRemaining != null) {
                    _uiState.update { it.copy(rateLimitRemaining = result.rateLimitRemaining) }
                }
            }
            is NetworkResult.NotModified -> {
                // Rate limit remains unchanged or decremented minimally
            }
            is NetworkResult.RateLimited -> {
                _uiState.update {
                    it.copy(
                        rateLimitRemaining = 0,
                        errorMessage = "GitHub API rate limit reached. Resets at epoch ${result.resetEpochSeconds}."
                    )
                }
            }
            is NetworkResult.Error -> {
                // Keep existing cached state on error
            }
        }
    }
}
