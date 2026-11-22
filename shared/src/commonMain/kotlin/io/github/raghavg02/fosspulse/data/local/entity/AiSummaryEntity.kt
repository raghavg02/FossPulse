package io.github.raghavg02.fosspulse.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "ai_summaries",
    foreignKeys = [
        ForeignKey(
            entity = TrackedRepoEntity::class,
            parentColumns = ["id"],
            childColumns = ["repo_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["repo_id"])]
)
data class AiSummaryEntity(
    @PrimaryKey
    @ColumnInfo(name = "cache_key")
    val cacheKey: String, // "{repo_id}:{tag_name}"

    @ColumnInfo(name = "repo_id")
    val repoId: String,

    @ColumnInfo(name = "tag_name")
    val tagName: String,

    @ColumnInfo(name = "summary_bullets")
    val summaryBullets: String, // 3-4 markdown bullets

    @ColumnInfo(name = "model_name")
    val modelName: String,

    @ColumnInfo(name = "generated_at")
    val generatedAt: Long // epoch ms
) {
    companion object {
        fun createCacheKey(repoId: String, tagName: String): String = "$repoId:$tagName"
    }
}
