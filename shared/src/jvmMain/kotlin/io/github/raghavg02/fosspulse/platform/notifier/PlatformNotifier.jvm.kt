package io.github.raghavg02.fosspulse.platform.notifier

import java.awt.SystemTray
import java.awt.Toolkit
import java.awt.TrayIcon

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
        sendDesktopMessage(title, body)
    }

    actual fun notifyAlreadyUpdated(
        appName: String,
        latestTag: String,
        repoId: String
    ) {
        val title = "$appName updated to $latestTag"
        val body = "You're on the latest version — check out what's new!"
        sendDesktopMessage(title, body)
    }

    actual fun notifyDailyAudit(title: String, body: String) {
        sendDesktopMessage(title, body)
    }

    private fun sendDesktopMessage(title: String, message: String) {
        if (SystemTray.isSupported()) {
            try {
                val tray = SystemTray.getSystemTray()
                val image = Toolkit.getDefaultToolkit().createImage("")
                val trayIcon = TrayIcon(image, "FossPulse")
                trayIcon.isImageAutoSize = true
                tray.add(trayIcon)
                trayIcon.displayMessage(title, message, TrayIcon.MessageType.INFO)
            } catch (_: Exception) {
                println("[FossPulse Notifier] $title: $message")
            }
        } else {
            println("[FossPulse Notifier] $title: $message")
        }
    }
}
