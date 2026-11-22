package io.github.raghavg02.fosspulse.domain.usecase

import io.github.raghavg02.fosspulse.domain.model.NotificationDecision
import io.github.raghavg02.fosspulse.domain.model.TargetPlatform
import io.github.raghavg02.fosspulse.domain.model.TrackedRepo
import io.github.raghavg02.fosspulse.ui.FakeRepoRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class RunDailyVersionAuditUseCaseTest {

    private lateinit var fakeRepository: FakeRepoRepository
    private lateinit var useCase: RunDailyVersionAuditUseCase

    private val baseRepo = TrackedRepo(
        id = "imranr98/obtainium",
        owner = "ImranR98",
        repoName = "Obtainium",
        displayName = "Obtainium",
        targetPlatform = TargetPlatform.ANDROID,
        assetRegex = ".*\\.apk$",
        installedVersion = "1.0.0",
        latestRemoteVersion = "1.0.0"
    )

    @BeforeTest
    fun setUp() {
        fakeRepository = FakeRepoRepository()
        useCase = RunDailyVersionAuditUseCase(fakeRepository)
    }

    @Test
    fun testZeroOutdatedAppsEmitsSilent() = runTest {
        fakeRepository.saveRepo(baseRepo.copy(installedVersion = "1.0.0", latestRemoteVersion = "1.0.0"))
        fakeRepository.saveRepo(baseRepo.copy(id = "app/two", installedVersion = "2.0.0", latestRemoteVersion = "2.0.0"))

        val decision = useCase()
        assertIs<NotificationDecision.DailyAuditDecision.DailyAuditSilent>(decision)
    }

    @Test
    fun testSingleOutdatedAppEmitsSingleAppNotification() = runTest {
        fakeRepository.saveRepo(baseRepo.copy(displayName = "Obtainium", installedVersion = "1.0.0", latestRemoteVersion = "1.2.0"))
        fakeRepository.saveRepo(baseRepo.copy(id = "app/two", installedVersion = "2.0.0", latestRemoteVersion = "2.0.0"))

        val decision = useCase()
        val single = assertIs<NotificationDecision.DailyAuditDecision.DailyAuditSingleApp>(decision)
        assertEquals("Update available: Obtainium", single.title)
        assertEquals("Obtainium can be updated from 1.0.0 to 1.2.0.", single.body)
    }

    @Test
    fun testMultipleOutdatedAppsEmitsGroupedNotification() = runTest {
        fakeRepository.saveRepo(baseRepo.copy(id = "app/one", installedVersion = "1.0.0", latestRemoteVersion = "1.2.0"))
        fakeRepository.saveRepo(baseRepo.copy(id = "app/two", installedVersion = "2.0.0", latestRemoteVersion = "2.1.0"))
        fakeRepository.saveRepo(baseRepo.copy(id = "app/three", installedVersion = "3.0.0", latestRemoteVersion = "3.5.0"))

        val decision = useCase()
        val grouped = assertIs<NotificationDecision.DailyAuditDecision.DailyAuditGrouped>(decision)
        assertEquals(3, grouped.outdatedCount)
        assertEquals("3 apps have updates available", grouped.title)
    }
}
