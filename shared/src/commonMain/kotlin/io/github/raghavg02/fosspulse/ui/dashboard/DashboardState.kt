package io.github.raghavg02.fosspulse.ui.dashboard

import io.github.raghavg02.fosspulse.domain.engine.SemVerComparator
import io.github.raghavg02.fosspulse.domain.model.ReleaseInfo
import io.github.raghavg02.fosspulse.domain.model.TargetPlatform
import io.github.raghavg02.fosspulse.domain.model.TrackedRepo

data class TrackedRepoItem(
    val repo: TrackedRepo,
    val release: ReleaseInfo?
) {
    val isUpdateAvailable: Boolean
        get() {
            val installed = repo.installedVersion ?: return false
            val remote = repo.latestRemoteVersion ?: return false
            return SemVerComparator.compare(installed, remote) < 0
        }

    val isUpToDate: Boolean
        get() {
            val installed = repo.installedVersion ?: return false
            val remote = repo.latestRemoteVersion ?: return false
            return SemVerComparator.compare(installed, remote) >= 0
        }
}

data class DashboardUiState(
    val isLoading: Boolean = true,
    val repos: List<TrackedRepoItem> = emptyList(),
    val selectedPlatformFilter: TargetPlatform? = null, // null = ALL
    val isRefreshing: Boolean = false,
    val rateLimitRemaining: Int? = null,
    val selectedRepoForDetail: TrackedRepoItem? = null,
    val isAddRepoSheetVisible: Boolean = false,
    val isScannerVisible: Boolean = false,
    val errorMessage: String? = null
) {
    val filteredRepos: List<TrackedRepoItem>
        get() = if (selectedPlatformFilter == null) {
            repos
        } else {
            repos.filter { it.repo.targetPlatform == selectedPlatformFilter }
        }
}

sealed interface DashboardUiEvent {
    data object RefreshAll : DashboardUiEvent
    data class FilterByPlatform(val platform: TargetPlatform?) : DashboardUiEvent
    data class SelectRepo(val item: TrackedRepoItem) : DashboardUiEvent
    data object DismissDetail : DashboardUiEvent
    data object ShowAddRepo : DashboardUiEvent
    data object DismissAddRepo : DashboardUiEvent
    data object ShowScanner : DashboardUiEvent
    data object DismissScanner : DashboardUiEvent
    data class DeleteRepo(val repoId: String) : DashboardUiEvent
    data class RefreshSingleRepo(val repoId: String) : DashboardUiEvent
}
