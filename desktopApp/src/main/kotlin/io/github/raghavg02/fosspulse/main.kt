package io.github.raghavg02.fosspulse

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import io.github.raghavg02.fosspulse.app.di.initKoin

fun main() {
    initKoin()
    DesktopScheduler.startBackgroundScheduler()

    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "FossPulse",
        ) {
            App()
        }
    }
}
