package io.github.raghavg02.fosspulse.platform

import platform.Foundation.NSURL
import platform.UIKit.UIApplication

actual fun openUrl(url: String) {
    val nsUrl = NSURL(string = url) ?: return
    UIApplication.sharedApplication.openURL(nsUrl)
}
