package io.github.raghavg02.fosspulse.domain.repository

import io.github.raghavg02.fosspulse.data.local.dao.AiSummaryDao
import io.github.raghavg02.fosspulse.data.local.entity.AiSummaryEntity
import io.github.raghavg02.fosspulse.domain.ai.LlmModelManager
import io.github.raghavg02.fosspulse.platform.ai.LocalLlmEngine
import io.github.raghavg02.fosspulse.platform.ai.PlatformModelStorage
import io.ktor.client.HttpClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class FakeAiSummaryDao : AiSummaryDao {
    val map = mutableMapOf<String, AiSummaryEntity>()

    override suspend fun getSummary(cacheKey: String): AiSummaryEntity? {
        return map[cacheKey]
    }

    override fun getSummaryFlow(cacheKey: String): Flow<AiSummaryEntity?> {
        return flowOf(map[cacheKey])
    }

    override suspend fun insertSummary(summary: AiSummaryEntity) {
        map[summary.cacheKey] = summary
    }
}

class ChangelogAiRepositoryTest {

    @Test
    fun testReturnsExistingSummaryWithoutInference() = runTest {
        val dao = FakeAiSummaryDao()
        val cached = AiSummaryEntity(
            cacheKey = "owner/repo:v1.0.0",
            repoId = "owner/repo",
            tagName = "v1.0.0",
            summaryBullets = "• [FEATURES] Already cached feature",
            modelName = "qwen2.5-0.5b",
            generatedAt = 12345L
        )
        dao.insertSummary(cached)

        val storage = PlatformModelStorage()
        val manager = LlmModelManager(HttpClient(), storage)
        val engine = LocalLlmEngine(storage)
        val repository = ChangelogAiRepositoryImpl(dao, manager, engine)

        val result = repository.generateAndCacheSummary("owner/repo", "v1.0.0", "New markdown")
        assertTrue(result.isSuccess)
        assertEquals("• [FEATURES] Already cached feature", result.getOrNull()?.summaryBullets)
    }
}
