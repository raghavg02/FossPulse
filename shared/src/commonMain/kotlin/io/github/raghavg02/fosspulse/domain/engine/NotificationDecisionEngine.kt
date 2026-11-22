package io.github.raghavg02.fosspulse.domain.engine

import io.github.raghavg02.fosspulse.domain.model.NotificationDecision
import io.github.raghavg02.fosspulse.domain.model.ReleaseInfo
import io.github.raghavg02.fosspulse.domain.model.TrackedRepo

object NotificationDecisionEngine {

    fun evaluateHourlyRelease(
        repo: TrackedRepo,
        latestRelease: ReleaseInfo
    ): NotificationDecision {
        // 1. Asset Gate: If release lacks a matched binary, suppress
        if (latestRelease.matchedAsset == null) {
            return NotificationDecision.SuppressNoMatchingBinary
        }

        // 2. Prevent duplicate alerts for the exact same tag
        if (repo.lastNotifiedTag != null && repo.lastNotifiedTag == latestRelease.tagName) {
            return NotificationDecision.SuppressDuplicate
        }

        val installed = repo.installedVersion
        val remote = latestRelease.tagName

        // If installedVersion < latestTag: Update available
        return if (installed == null || SemVerComparator.compare(installed, remote) < 0) {
            val title = "${repo.displayName} $remote is out"
            val body = "Update available (${installed ?: "Not installed"} → $remote). Tap to download."
            NotificationDecision.NotifyUpdateAvailable(
                repo = repo,
                latestRelease = latestRelease,
                title = title,
                body = body
            )
        } else {
            // installedVersion >= latestTag: Post-update "What's new"
            val title = "${repo.displayName} updated to $remote"
            val body = "You're on the latest version — check out what's new!"
            NotificationDecision.NotifyAlreadyUpdated(
                repo = repo,
                latestRelease = latestRelease,
                title = title,
                body = body
            )
        }
    }

    fun evaluateDailyAudit(
        trackedRepos: List<TrackedRepo>
    ): NotificationDecision.DailyAuditDecision {
        val outdatedRepos = trackedRepos.filter { repo ->
            !repo.isDeleted &&
            repo.installedVersion != null &&
            repo.latestRemoteVersion != null &&
            SemVerComparator.compare(repo.installedVersion, repo.latestRemoteVersion) < 0
        }

        return when (outdatedRepos.size) {
            0 -> NotificationDecision.DailyAuditDecision.DailyAuditSilent
            1 -> {
                val repo = outdatedRepos.first()
                NotificationDecision.DailyAuditDecision.DailyAuditSingleApp(
                    repo = repo,
                    title = "Update available: ${repo.displayName}",
                    body = "${repo.displayName} can be updated from ${repo.installedVersion} to ${repo.latestRemoteVersion}."
                )
            }
            else -> {
                NotificationDecision.DailyAuditDecision.DailyAuditGrouped(
                    outdatedCount = outdatedRepos.size,
                    title = "${outdatedRepos.size} apps have updates available",
                    body = "Tap to review and download the latest releases."
                )
            }
        }
    }
}
