package io.github.raghavg02.fosspulse

import io.github.raghavg02.fosspulse.domain.usecase.CheckHourlyReleaseUpdatesUseCase
import io.github.raghavg02.fosspulse.domain.usecase.RunDailyVersionAuditUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.time.Duration.Companion.hours

object DesktopScheduler : KoinComponent {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val hourlyUseCase: CheckHourlyReleaseUpdatesUseCase by inject()
    private val dailyAuditUseCase: RunDailyVersionAuditUseCase by inject()

    fun startBackgroundScheduler() {
        // Hourly Release Watcher (runs once per hour)
        scope.launch {
            while (isActive) {
                try {
                    hourlyUseCase()
                } catch (_: Exception) {}
                delay(1.hours)
            }
        }

        // Daily Version Audit (runs once every 24 hours)
        scope.launch {
            while (isActive) {
                try {
                    dailyAuditUseCase()
                } catch (_: Exception) {}
                delay(24.hours)
            }
        }
    }
}
