package io.github.raghavg02.fosspulse.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import io.github.raghavg02.fosspulse.domain.usecase.CheckHourlyReleaseUpdatesUseCase
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class HourlyReleaseWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params), KoinComponent {

    private val checkHourlyReleaseUpdatesUseCase: CheckHourlyReleaseUpdatesUseCase by inject()

    override suspend fun doWork(): Result {
        return try {
            checkHourlyReleaseUpdatesUseCase()
            Result.success()
        } catch (_: Exception) {
            Result.retry()
        }
    }
}
