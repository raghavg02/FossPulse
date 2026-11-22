package io.github.raghavg02.fosspulse.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.github.raghavg02.fosspulse.data.local.entity.CachedReleaseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CachedReleaseDao {

    @Query("SELECT * FROM cached_releases WHERE repo_id = :repoId LIMIT 1")
    suspend fun getReleaseByRepoId(repoId: String): CachedReleaseEntity?

    @Query("SELECT * FROM cached_releases WHERE repo_id = :repoId LIMIT 1")
    fun getReleaseByRepoIdFlow(repoId: String): Flow<CachedReleaseEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRelease(release: CachedReleaseEntity)

    @Query("DELETE FROM cached_releases WHERE repo_id = :repoId")
    suspend fun deleteReleaseByRepoId(repoId: String)
}
