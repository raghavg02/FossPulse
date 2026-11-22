package io.github.raghavg02.fosspulse.platform.scanner

import io.github.raghavg02.fosspulse.domain.scanner.ScannedApp

actual fun detectDeviceAbi(): String = "x86_64"

actual fun scanInstalledApps(): List<ScannedApp> = emptyList()
