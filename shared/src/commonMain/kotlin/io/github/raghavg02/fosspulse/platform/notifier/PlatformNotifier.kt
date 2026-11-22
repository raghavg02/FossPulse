package io.github.raghavg02.fosspulse.platform.notifier

expect class PlatformNotifier() {
    fun notifyUpdateAvailable(
        appName: String,
        installedVersion: String,
        latestTag: String,
        downloadUrl: String?,
        repoId: String
    )

    fun notifyAlreadyUpdated(
        appName: String,
        latestTag: String,
        repoId: String
    )

    fun notifyDailyAudit(
        title: String,
        body: String
    )
}
