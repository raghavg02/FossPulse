package io.github.raghavg02.fosspulse.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import io.github.raghavg02.fosspulse.domain.usecase.RunDailyVersionAuditUseCase
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class DailyAuditWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params), KoinComponent {

    private val runDailyVersionAuditUseCase: RunDailyVersionAuditUseCase by inject()

    override suspend fun doWork(): Result {
        return try {
            runDailyVersionAuditUseCase()
            Result.success()
        } catch (_: Exception) {
            Result.retry()
        }
    }
}
