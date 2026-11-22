package io.github.raghavg02.fosspulse.platform

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import io.github.raghavg02.fosspulse.data.local.appContext

actual fun downloadFile(fileName: String, downloadUrl: String) {
    val context = appContext ?: return
    try {
        val request = DownloadManager.Request(Uri.parse(downloadUrl)).apply {
            setTitle(fileName)
            setDescription("Downloading $fileName via FossPulse")
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
            setAllowedOverMetered(true)
            setAllowedOverRoaming(true)
        }
        val manager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        manager.enqueue(request)
    } catch (_: Exception) {
        openUrl(downloadUrl)
    }
}
