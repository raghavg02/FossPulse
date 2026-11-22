package io.github.raghavg02.fosspulse.platform.ai

import io.github.raghavg02.fosspulse.domain.ai.ChangelogSemanticSummarizer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

actual class LocalLlmEngine actual constructor(
    private val modelStorage: PlatformModelStorage
) {
    actual suspend fun summarize(rawChangelog: String, modelFileName: String): Result<String> = withContext(Dispatchers.Default) {
        if (!modelStorage.isModelFilePresent(modelFileName)) {
            return@withContext Result.failure(
                IllegalStateException("Model file $modelFileName is not downloaded. Please download it first.")
            )
        }

        try {
            val summary = ChangelogSemanticSummarizer.generateSummary(rawChangelog)
            Result.success(summary)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
