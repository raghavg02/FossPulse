package io.github.raghavg02.fosspulse.data.local

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import io.github.raghavg02.fosspulse.data.local.entity.CachedReleaseEntity
import io.github.raghavg02.fosspulse.data.local.entity.TrackedRepoEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class RoomDatabaseTest {

    private lateinit var database: FossPulseDatabase

    @BeforeTest
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder<FossPulseDatabase>()
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(Dispatchers.Unconfined)
            .build()
    }

    @AfterTest
    fun tearDown() {
        database.close()
    }

    @Test
    fun testTrackedRepoInsertAndQuery() = runTest {
        val repo = TrackedRepoEntity(
            id = "imranr98/obtainium",
            owner = "ImranR98",
            repoName = "Obtainium",
            displayName = "Obtainium",
            androidPackageName = "dev.imranr.obtainium",
            targetPlatform = "ANDROID",
            assetRegex = ".*arm64.*\\.apk$",
            installedVersion = "1.1.2",
            latestRemoteVersion = "1.1.4"
        )

        database.trackedRepoDao().insertRepo(repo)

        val retrieved = database.trackedRepoDao().getRepoById("imranr98/obtainium")
        assertNotNull(retrieved)
        assertEquals("Obtainium", retrieved.displayName)
        assertEquals("dev.imranr.obtainium", retrieved.androidPackageName)

        val activeList = database.trackedRepoDao().getAllActiveRepos()
        assertEquals(1, activeList.size)
    }

    @Test
    fun testCascadeDeleteOnCachedRelease() = runTest {
        val repo = TrackedRepoEntity(
            id = "imranr98/obtainium",
            owner = "ImranR98",
            repoName = "Obtainium",
            displayName = "Obtainium",
            targetPlatform = "ANDROID",
            assetRegex = ".*\\.apk$"
        )
        database.trackedRepoDao().insertRepo(repo)

        val release = CachedReleaseEntity(
            repoId = "imranr98/obtainium",
            tagName = "v1.1.4",
            releaseTitle = "v1.1.4",
            rawChangelogMd = "Changelog",
            htmlUrl = "https://github.com",
            publishedAt = 1700000000000L
        )
        database.cachedReleaseDao().insertRelease(release)

        val cached = database.cachedReleaseDao().getReleaseByRepoId("imranr98/obtainium")
        assertNotNull(cached)
        assertEquals("v1.1.4", cached.tagName)

        database.trackedRepoDao().deleteRepoPermanent("imranr98/obtainium")
        val cachedAfterDelete = database.cachedReleaseDao().getReleaseByRepoId("imranr98/obtainium")
        assertNull(cachedAfterDelete)
    }
}
