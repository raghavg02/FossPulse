package io.github.raghavg02.fosspulse.platform.notifier

import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNTimeIntervalNotificationTrigger
import platform.UserNotifications.UNUserNotificationCenter

actual class PlatformNotifier actual constructor() {

    actual fun notifyUpdateAvailable(
        appName: String,
        installedVersion: String,
        latestTag: String,
        downloadUrl: String?,
        repoId: String
    ) {
        val title = "$appName $latestTag is out"
        val body = "Update available ($installedVersion → $latestTag). Tap to download."
        scheduleIosNotification(title, body, repoId)
    }

    actual fun notifyAlreadyUpdated(
        appName: String,
        latestTag: String,
        repoId: String
    ) {
        val title = "$appName updated to $latestTag"
        val body = "You're on the latest version — check out what's new!"
        scheduleIosNotification(title, body, repoId)
    }

    actual fun notifyDailyAudit(title: String, body: String) {
        scheduleIosNotification(title, body, "daily_audit")
    }

    private fun scheduleIosNotification(title: String, body: String, identifier: String) {
        val center = UNUserNotificationCenter.currentNotificationCenter()
        val content = UNMutableNotificationContent().apply {
            setTitle(title)
            setBody(body)
        }
        val trigger = UNTimeIntervalNotificationTrigger.triggerWithTimeInterval(1.0, repeats = false)
        val request = UNNotificationRequest.requestWithIdentifier(identifier, content, trigger)
        center.addNotificationRequest(request, null)
    }
}
