package io.github.raghavg02.fosspulse.platform

actual fun downloadFile(fileName: String, downloadUrl: String) {
    openUrl(downloadUrl)
}
