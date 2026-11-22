package io.github.raghavg02.fosspulse.worker

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object WorkScheduler {

    private const val HOURLY_WORK_NAME = "fosspulse_hourly_watcher"
    private const val DAILY_WORK_NAME = "fosspulse_daily_audit"

    fun scheduleBackgroundWorkers(context: Context) {
        val workManager = WorkManager.getInstance(context)

        val networkConstraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        // 1. Hourly Release Watcher (every 1 hour)
        val hourlyRequest = PeriodicWorkRequestBuilder<HourlyReleaseWorker>(
            1, TimeUnit.HOURS
        )
            .setConstraints(networkConstraints)
            .build()

        workManager.enqueueUniquePeriodicWork(
            HOURLY_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            hourlyRequest
        )

        // 2. Daily Version Audit (every 24 hours)
        val dailyRequest = PeriodicWorkRequestBuilder<DailyAuditWorker>(
            24, TimeUnit.HOURS
        ).build()

        workManager.enqueueUniquePeriodicWork(
            DAILY_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            dailyRequest
        )
    }
}
