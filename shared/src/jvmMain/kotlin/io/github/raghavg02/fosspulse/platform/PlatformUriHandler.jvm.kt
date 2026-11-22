package io.github.raghavg02.fosspulse.platform

import java.awt.Desktop
import java.net.URI

actual fun openUrl(url: String) {
    if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
        Desktop.getDesktop().browse(URI(url))
    }
}
