package io.github.raghavg02.fosspulse.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.github.raghavg02.fosspulse.data.local.entity.AiSummaryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AiSummaryDao {

    @Query("SELECT * FROM ai_summaries WHERE cache_key = :cacheKey LIMIT 1")
    suspend fun getSummary(cacheKey: String): AiSummaryEntity?

    @Query("SELECT * FROM ai_summaries WHERE cache_key = :cacheKey LIMIT 1")
    fun getSummaryFlow(cacheKey: String): Flow<AiSummaryEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSummary(summary: AiSummaryEntity)
}
