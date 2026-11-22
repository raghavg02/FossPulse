package io.github.raghavg02.fosspulse.ui.scanner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.raghavg02.fosspulse.data.remote.GitHubApiService
import io.github.raghavg02.fosspulse.domain.model.NetworkResult
import io.github.raghavg02.fosspulse.domain.model.TargetPlatform
import io.github.raghavg02.fosspulse.domain.model.TrackedRepo
import io.github.raghavg02.fosspulse.domain.repository.RepoRepository
import io.github.raghavg02.fosspulse.domain.scanner.ScannedApp
import io.github.raghavg02.fosspulse.platform.scanner.detectDeviceAbi
import io.github.raghavg02.fosspulse.platform.scanner.scanInstalledApps
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class ScannerUiState(
    val isLoading: Boolean = true,
    val deviceAbi: String = detectDeviceAbi(),
    val searchQuery: String = "",
    val apps: List<ScannedApp> = emptyList(),
    val isImporting: Boolean = false,
    val errorMessage: String? = null
) {
    val filteredApps: List<ScannedApp>
        get() = if (searchQuery.isBlank()) {
            apps
        } else {
            val q = searchQuery.trim().lowercase()
            apps.filter {
                it.appName.lowercase().contains(q) ||
                it.packageName.lowercase().contains(q) ||
                it.githubRepoId.lowercase().contains(q)
            }
        }

    val selectedCount: Int
        get() = apps.count { it.isSelected && !it.isAlreadyTracked }
}

class InstalledAppsScannerViewModel(
    private val repoRepository: RepoRepository,
    private val gitHubApiService: GitHubApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScannerUiState())
    val uiState: StateFlow<ScannerUiState> = _uiState.asStateFlow()

    init {
        loadScannedApps()
    }

    fun loadScannedApps() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val rawApps = withContext(Dispatchers.IO) { scanInstalledApps() }
            val trackedRepos = repoRepository.getAllActiveRepos()
            val trackedRepoIds = trackedRepos.map { it.id }.toSet()

            val markedApps = rawApps.map { app ->
                val alreadyTracked = trackedRepoIds.contains(app.githubRepoId)
                app.copy(
                    isAlreadyTracked = alreadyTracked,
                    isSelected = !alreadyTracked
                )
            }

            _uiState.update {
                it.copy(
                    isLoading = false,
                    apps = markedApps
                )
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun toggleSelection(packageName: String) {
        _uiState.update { current ->
            current.copy(
                apps = current.apps.map { app ->
                    if (app.packageName == packageName && !app.isAlreadyTracked) {
                        app.copy(isSelected = !app.isSelected)
                    } else {
                        app
                    }
                }
            )
        }
    }

    fun selectAll() {
        _uiState.update { current ->
            current.copy(
                apps = current.apps.map { app ->
                    if (!app.isAlreadyTracked) app.copy(isSelected = true) else app
                }
            )
        }
    }

    fun deselectAll() {
        _uiState.update { current ->
            current.copy(
                apps = current.apps.map { app ->
                    if (!app.isAlreadyTracked) app.copy(isSelected = false) else app
                }
            )
        }
    }

    fun importSelectedApps(onSuccess: () -> Unit) {
        val selected = _uiState.value.apps.filter { it.isSelected && !it.isAlreadyTracked }
        if (selected.isEmpty()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isImporting = true) }

            val reposToSave = selected.map { app ->
                TrackedRepo(
                    id = app.githubRepoId,
                    owner = app.owner,
                    repoName = app.repoName,
                    displayName = app.appName,
                    androidPackageName = app.packageName,
                    targetPlatform = TargetPlatform.ANDROID,
                    assetRegex = app.suggestedRegex,
                    installedVersion = app.installedVersionName
                )
            }

            repoRepository.saveRepos(reposToSave)

            // Trigger immediate background fetch for these imported repos
            for (repo in reposToSave) {
                when (val result = gitHubApiService.getLatestRelease(
                    owner = repo.owner,
                    repo = repo.repoName,
                    assetRegex = repo.assetRegex
                )) {
                    is NetworkResult.Success -> {
                        repoRepository.updateLatestReleaseCheck(
                            repoId = repo.id,
                            latestTag = result.data.tagName,
                            etag = result.etag,
                            release = result.data
                        )
                    }
                    else -> {}
                }
            }

            _uiState.update { it.copy(isImporting = false) }
            onSuccess()
        }
    }
}
