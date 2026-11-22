package io.github.raghavg02.fosspulse.domain.repository

import io.github.raghavg02.fosspulse.data.local.dao.AiSummaryDao
import io.github.raghavg02.fosspulse.data.local.entity.AiSummaryEntity
import io.github.raghavg02.fosspulse.domain.ai.LlmModelManager
import io.github.raghavg02.fosspulse.platform.ai.LocalLlmEngine
import kotlinx.coroutines.flow.Flow
import kotlin.time.Clock

interface ChangelogAiRepository {
    fun getSummaryFlow(repoId: String, tagName: String): Flow<AiSummaryEntity?>
    suspend fun getSummary(repoId: String, tagName: String): AiSummaryEntity?
    suspend fun generateAndCacheSummary(repoId: String, tagName: String, rawChangelog: String): Result<AiSummaryEntity>
}

class ChangelogAiRepositoryImpl(
    private val aiSummaryDao: AiSummaryDao,
    private val llmModelManager: LlmModelManager,
    private val localLlmEngine: LocalLlmEngine
) : ChangelogAiRepository {

    override fun getSummaryFlow(repoId: String, tagName: String): Flow<AiSummaryEntity?> {
        val cacheKey = "$repoId:$tagName"
        return aiSummaryDao.getSummaryFlow(cacheKey)
    }

    override suspend fun getSummary(repoId: String, tagName: String): AiSummaryEntity? {
        val cacheKey = "$repoId:$tagName"
        return aiSummaryDao.getSummary(cacheKey)
    }

    override suspend fun generateAndCacheSummary(
        repoId: String,
        tagName: String,
        rawChangelog: String
    ): Result<AiSummaryEntity> {
        val cacheKey = "$repoId:$tagName"
        val existing = aiSummaryDao.getSummary(cacheKey)
        if (existing != null) {
            return Result.success(existing)
        }

        val activeModel = llmModelManager.getActiveModel()
        val result = localLlmEngine.summarize(rawChangelog, activeModel.fileName)

        return result.mapCatching { bullets ->
            val entity = AiSummaryEntity(
                cacheKey = cacheKey,
                repoId = repoId,
                tagName = tagName,
                summaryBullets = bullets,
                modelName = activeModel.name,
                generatedAt = Clock.System.now().toEpochMilliseconds()
            )
            aiSummaryDao.insertSummary(entity)
            entity
        }
    }
}
