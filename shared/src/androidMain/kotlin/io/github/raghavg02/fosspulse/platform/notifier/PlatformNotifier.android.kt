package io.github.raghavg02.fosspulse.platform.notifier

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import io.github.raghavg02.fosspulse.data.local.appContext

actual class PlatformNotifier actual constructor() {

    companion object {
        const val CHANNEL_UPDATES = "fosspulse_release_updates"
        const val CHANNEL_AUDIT = "fosspulse_daily_audit"
    }

    private val context: Context?
        get() = appContext

    private val notificationManager: NotificationManager?
        get() = context?.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = notificationManager ?: return
            val channelUpdates = NotificationChannel(
                CHANNEL_UPDATES,
                "Release Updates",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications when a tracked application has a new release."
                enableVibration(true)
            }

            val channelAudit = NotificationChannel(
                CHANNEL_AUDIT,
                "Daily Version Audits",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Daily 24-hour version status audits."
            }

            manager.createNotificationChannel(channelUpdates)
            manager.createNotificationChannel(channelAudit)
        }
    }

    actual fun notifyUpdateAvailable(
        appName: String,
        installedVersion: String,
        latestTag: String,
        downloadUrl: String?,
        repoId: String
    ) {
        val ctx = context ?: return
        val manager = notificationManager ?: return

        val title = "$appName $latestTag is out"
        val body = "Update available ($installedVersion → $latestTag). Tap to download."

        val launchIntent = ctx.packageManager.getLaunchIntentForPackage(ctx.packageName)?.apply {
            putExtra("repo_id", repoId)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val contentPendingIntent = PendingIntent.getActivity(
            ctx,
            repoId.hashCode(),
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(ctx, CHANNEL_UPDATES)
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(contentPendingIntent)

        if (!downloadUrl.isNullOrBlank()) {
            val downloadIntent = Intent(Intent.ACTION_VIEW, Uri.parse(downloadUrl)).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            val downloadPendingIntent = PendingIntent.getActivity(
                ctx,
                (repoId + "_dl").hashCode(),
                downloadIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            builder.addAction(android.R.drawable.stat_sys_download, "DOWNLOAD", downloadPendingIntent)
        }

        manager.notify(repoId.hashCode(), builder.build())
    }

    actual fun notifyAlreadyUpdated(
        appName: String,
        latestTag: String,
        repoId: String
    ) {
        val ctx = context ?: return
        val manager = notificationManager ?: return

        val title = "$appName updated to $latestTag"
        val body = "You're on the latest version — check out what's new!"

        val launchIntent = ctx.packageManager.getLaunchIntentForPackage(ctx.packageName)?.apply {
            putExtra("repo_id", repoId)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val contentPendingIntent = PendingIntent.getActivity(
            ctx,
            repoId.hashCode(),
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(ctx, CHANNEL_UPDATES)
            .setSmallIcon(android.R.drawable.stat_notify_sync)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(contentPendingIntent)
            .build()

        manager.notify(repoId.hashCode(), notification)
    }

    actual fun notifyDailyAudit(title: String, body: String) {
        val ctx = context ?: return
        val manager = notificationManager ?: return

        val launchIntent = ctx.packageManager.getLaunchIntentForPackage(ctx.packageName)?.apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val contentPendingIntent = PendingIntent.getActivity(
            ctx,
            9999,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(ctx, CHANNEL_AUDIT)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(contentPendingIntent)
            .build()

        manager.notify(9999, notification)
    }
}
