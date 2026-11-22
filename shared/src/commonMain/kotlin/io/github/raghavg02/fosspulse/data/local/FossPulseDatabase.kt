package io.github.raghavg02.fosspulse.data.local

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import io.github.raghavg02.fosspulse.data.local.dao.AiSummaryDao
import io.github.raghavg02.fosspulse.data.local.dao.CachedReleaseDao
import io.github.raghavg02.fosspulse.data.local.dao.TrackedRepoDao
import io.github.raghavg02.fosspulse.data.local.entity.AiSummaryEntity
import io.github.raghavg02.fosspulse.data.local.entity.CachedReleaseEntity
import io.github.raghavg02.fosspulse.data.local.entity.TrackedRepoEntity

@Database(
    entities = [
        TrackedRepoEntity::class,
        CachedReleaseEntity::class,
        AiSummaryEntity::class
    ],
    version = 1,
    exportSchema = false
)
@ConstructedBy(FossPulseDatabaseConstructor::class)
abstract class FossPulseDatabase : RoomDatabase() {
    abstract fun trackedRepoDao(): TrackedRepoDao
    abstract fun cachedReleaseDao(): CachedReleaseDao
    abstract fun aiSummaryDao(): AiSummaryDao
}

// Room compiler generates the actual implementation of RoomDatabaseConstructor
@Suppress("NO_ACTUAL_FOR_EXPECT")
expect object FossPulseDatabaseConstructor : RoomDatabaseConstructor<FossPulseDatabase> {
    override fun initialize(): FossPulseDatabase
}
