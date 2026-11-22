package io.github.raghavg02.fosspulse.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import io.github.raghavg02.fosspulse.data.local.entity.TrackedRepoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TrackedRepoDao {

    @Query("SELECT * FROM tracked_repos WHERE is_deleted = 0 ORDER BY display_name COLLATE NOCASE ASC")
    fun getAllActiveReposFlow(): Flow<List<TrackedRepoEntity>>

    @Query("SELECT * FROM tracked_repos WHERE is_deleted = 0 ORDER BY display_name COLLATE NOCASE ASC")
    suspend fun getAllActiveRepos(): List<TrackedRepoEntity>

    @Query("SELECT * FROM tracked_repos WHERE id = :id LIMIT 1")
    suspend fun getRepoById(id: String): TrackedRepoEntity?

    @Query("SELECT * FROM tracked_repos WHERE id = :id LIMIT 1")
    fun getRepoByIdFlow(id: String): Flow<TrackedRepoEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRepo(repo: TrackedRepoEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRepos(repos: List<TrackedRepoEntity>)

    @Update
    suspend fun updateRepo(repo: TrackedRepoEntity)

    @Query("UPDATE tracked_repos SET installed_version = :version, updated_at = :updatedAt WHERE id = :repoId")
    suspend fun updateInstalledVersion(repoId: String, version: String?, updatedAt: Long)

    @Query("UPDATE tracked_repos SET latest_remote_version = :latestTag, etag = :etag, last_checked_at = :checkedAt, updated_at = :checkedAt WHERE id = :repoId")
    suspend fun updateLatestReleaseCheck(repoId: String, latestTag: String, etag: String?, checkedAt: Long)

    @Query("UPDATE tracked_repos SET last_notified_tag = :tag, updated_at = :updatedAt WHERE id = :repoId")
    suspend fun updateLastNotifiedTag(repoId: String, tag: String, updatedAt: Long)

    @Query("UPDATE tracked_repos SET is_deleted = 1, updated_at = :updatedAt WHERE id = :id")
    suspend fun softDeleteRepo(id: String, updatedAt: Long)

    @Query("DELETE FROM tracked_repos WHERE id = :id")
    suspend fun deleteRepoPermanent(id: String)
}
