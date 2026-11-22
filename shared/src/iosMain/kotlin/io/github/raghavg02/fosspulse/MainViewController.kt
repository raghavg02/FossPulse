package io.github.raghavg02.fosspulse

import androidx.compose.ui.window.ComposeUIViewController
import io.github.raghavg02.fosspulse.app.di.initKoin

fun MainViewController() = ComposeUIViewController {
    initKoin()
    App()
}
