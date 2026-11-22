package io.github.raghavg02.fosspulse.platform

import android.content.Intent
import android.net.Uri
import io.github.raghavg02.fosspulse.data.local.appContext

actual fun openUrl(url: String) {
    val context = appContext ?: return
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    } catch (_: Exception) {}
}
