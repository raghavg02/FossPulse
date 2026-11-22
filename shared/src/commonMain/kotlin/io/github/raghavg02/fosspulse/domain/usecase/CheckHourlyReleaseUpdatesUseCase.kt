package io.github.raghavg02.fosspulse.domain.usecase

import io.github.raghavg02.fosspulse.data.remote.GitHubApiService
import io.github.raghavg02.fosspulse.domain.engine.NotificationDecisionEngine
import io.github.raghavg02.fosspulse.domain.model.NetworkResult
import io.github.raghavg02.fosspulse.domain.model.NotificationDecision
import io.github.raghavg02.fosspulse.domain.repository.RepoRepository
import io.github.raghavg02.fosspulse.platform.notifier.PlatformNotifier

data class HourlyCheckSummary(
    val checkedCount: Int,
    val updatedAvailableCount: Int,
    val alreadyUpdatedCount: Int,
    val binarySuppressedCount: Int,
    val notModifiedCount: Int
)

class CheckHourlyReleaseUpdatesUseCase(
    private val repoRepository: RepoRepository,
    private val gitHubApiService: GitHubApiService,
    private val notifier: PlatformNotifier = PlatformNotifier()
) {
    suspend operator fun invoke(): HourlyCheckSummary {
        val repos = repoRepository.getAllActiveRepos()
        var updateAvailableCount = 0
        var alreadyUpdatedCount = 0
        var binarySuppressedCount = 0
        var notModifiedCount = 0

        for (repo in repos) {
            when (val result = gitHubApiService.getLatestRelease(
                owner = repo.owner,
                repo = repo.repoName,
                assetRegex = repo.assetRegex,
                etag = repo.etag,
                includePrereleases = repo.includePrereleases
            )) {
                is NetworkResult.Success -> {
                    val release = result.data
                    // Update latest remote version and cached release
                    repoRepository.updateLatestReleaseCheck(
                        repoId = repo.id,
                        latestTag = release.tagName,
                        etag = result.etag,
                        release = release
                    )

                    // Evaluate notification decision
                    when (val decision = NotificationDecisionEngine.evaluateHourlyRelease(repo, release)) {
                        is NotificationDecision.NotifyUpdateAvailable -> {
                            notifier.notifyUpdateAvailable(
                                appName = repo.displayName,
                                installedVersion = repo.installedVersion ?: "Unknown",
                                latestTag = release.tagName,
                                downloadUrl = release.matchedAsset?.downloadUrl,
                                repoId = repo.id
                            )
                            repoRepository.updateLastNotifiedTag(repo.id, release.tagName)
                            updateAvailableCount++
                        }
                        is NotificationDecision.NotifyAlreadyUpdated -> {
                            notifier.notifyAlreadyUpdated(
                                appName = repo.displayName,
                                latestTag = release.tagName,
                                repoId = repo.id
                            )
                            repoRepository.updateLastNotifiedTag(repo.id, release.tagName)
                            alreadyUpdatedCount++
                        }
                        is NotificationDecision.SuppressNoMatchingBinary -> {
                            binarySuppressedCount++
                        }
                        is NotificationDecision.SuppressDuplicate -> {
                            // Already notified for this tag
                        }
                        else -> {}
                    }
                }
                is NetworkResult.NotModified -> {
                    notModifiedCount++
                }
                else -> {
                    // Network errors or rate limit - keep existing DB state
                }
            }
        }

        return HourlyCheckSummary(
            checkedCount = repos.size,
            updatedAvailableCount = updateAvailableCount,
            alreadyUpdatedCount = alreadyUpdatedCount,
            binarySuppressedCount = binarySuppressedCount,
            notModifiedCount = notModifiedCount
        )
    }
}
