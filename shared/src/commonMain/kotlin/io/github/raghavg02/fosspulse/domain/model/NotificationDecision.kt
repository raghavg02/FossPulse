package io.github.raghavg02.fosspulse.domain.model

sealed interface NotificationDecision {
    data class NotifyUpdateAvailable(
        val repo: TrackedRepo,
        val latestRelease: ReleaseInfo,
        val title: String,
        val body: String
    ) : NotificationDecision

    data class NotifyAlreadyUpdated(
        val repo: TrackedRepo,
        val latestRelease: ReleaseInfo,
        val title: String,
        val body: String
    ) : NotificationDecision

    data object SuppressDuplicate : NotificationDecision

    data object SuppressNoMatchingBinary : NotificationDecision

    sealed interface DailyAuditDecision : NotificationDecision {
        data object DailyAuditSilent : DailyAuditDecision

        data class DailyAuditSingleApp(
            val repo: TrackedRepo,
            val title: String,
            val body: String
        ) : DailyAuditDecision

        data class DailyAuditGrouped(
            val outdatedCount: Int,
            val title: String,
            val body: String
        ) : DailyAuditDecision
    }
}
