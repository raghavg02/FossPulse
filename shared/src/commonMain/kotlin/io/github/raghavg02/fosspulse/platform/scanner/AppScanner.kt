package io.github.raghavg02.fosspulse.platform.scanner

import io.github.raghavg02.fosspulse.domain.scanner.ScannedApp

expect fun scanInstalledApps(): List<ScannedApp>

expect fun detectDeviceAbi(): String
