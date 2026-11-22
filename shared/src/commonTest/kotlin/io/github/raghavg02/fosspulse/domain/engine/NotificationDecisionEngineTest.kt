package io.github.raghavg02.fosspulse.domain.engine

import io.github.raghavg02.fosspulse.domain.model.NotificationDecision
import io.github.raghavg02.fosspulse.domain.model.ReleaseAsset
import io.github.raghavg02.fosspulse.domain.model.ReleaseInfo
import io.github.raghavg02.fosspulse.domain.model.TargetPlatform
import io.github.raghavg02.fosspulse.domain.model.TrackedRepo
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class NotificationDecisionEngineTest {

    private val baseRepo = TrackedRepo(
        id = "imranr98/obtainium",
        owner = "ImranR98",
        repoName = "Obtainium",
        displayName = "Obtainium",
        androidPackageName = "dev.imranr.obtainium",
        targetPlatform = TargetPlatform.ANDROID,
        assetRegex = ".*arm64.*\\.apk$",
        installedVersion = "1.1.2",
        latestRemoteVersion = "1.1.4",
        lastNotifiedTag = "1.1.2"
    )

    private val matchedAsset = ReleaseAsset(
        name = "obtainium-arm64.apk",
        downloadUrl = "https://github.com/imranr98/obtainium/releases/download/v1.1.4/obtainium-arm64.apk",
        sizeBytes = 15000000L
    )

    private val latestRelease = ReleaseInfo(
        tagName = "1.1.4",
        releaseTitle = "v1.1.4 Bugfix Release",
        rawChangelogMd = "Fixed crashes",
        htmlUrl = "https://github.com/imranr98/obtainium/releases/tag/v1.1.4",
        publishedAtEpochMs = 1700000000000L,
        matchedAsset = matchedAsset
    )

    @Test
    fun testHourlyUpdateAvailableBranch() {
        val repo = baseRepo.copy(installedVersion = "1.1.2", lastNotifiedTag = "1.1.2")
        val decision = NotificationDecisionEngine.evaluateHourlyRelease(repo, latestRelease)

        assertIs<NotificationDecision.NotifyUpdateAvailable>(decision)
        assertEquals("Obtainium 1.1.4 is out", decision.title)
        assertEquals("Update available (1.1.2 → 1.1.4). Tap to download.", decision.body)
    }

    @Test
    fun testHourlyAlreadyUpdatedWhatsNewBranch() {
        // App is already at v1.1.4 or higher
        val repo = baseRepo.copy(installedVersion = "1.1.4", lastNotifiedTag = "1.1.2")
        val decision = NotificationDecisionEngine.evaluateHourlyRelease(repo, latestRelease)

        assertIs<NotificationDecision.NotifyAlreadyUpdated>(decision)
        assertEquals("Obtainium updated to 1.1.4", decision.title)
        assertEquals("You're on the latest version — check out what's new!", decision.body)
    }

    @Test
    fun testHourlySuppressDuplicateNotification() {
        // Notification for 1.1.4 was already sent previously
        val repo = baseRepo.copy(installedVersion = "1.1.2", lastNotifiedTag = "1.1.4")
        val decision = NotificationDecisionEngine.evaluateHourlyRelease(repo, latestRelease)

        assertIs<NotificationDecision.SuppressDuplicate>(decision)
    }

    @Test
    fun testHourlySuppressWhenNoMatchingBinaryAttached() {
        val releaseWithoutBinary = latestRelease.copy(matchedAsset = null)
        val decision = NotificationDecisionEngine.evaluateHourlyRelease(baseRepo, releaseWithoutBinary)

        assertIs<NotificationDecision.SuppressNoMatchingBinary>(decision)
    }

    @Test
    fun testDailyAuditSilentWhenZeroOutdated() {
        val repos = listOf(
            baseRepo.copy(installedVersion = "1.1.4", latestRemoteVersion = "1.1.4"),
            baseRepo.copy(id = "repo/two", installedVersion = "2.0.0", latestRemoteVersion = "2.0.0")
        )
        val decision = NotificationDecisionEngine.evaluateDailyAudit(repos)
        assertIs<NotificationDecision.DailyAuditDecision.DailyAuditSilent>(decision)
    }

    @Test
    fun testDailyAuditSingleAppOutdated() {
        val repos = listOf(
            baseRepo.copy(displayName = "Obtainium", installedVersion = "1.1.2", latestRemoteVersion = "1.1.4"),
            baseRepo.copy(id = "repo/two", installedVersion = "2.0.0", latestRemoteVersion = "2.0.0")
        )
        val decision = NotificationDecisionEngine.evaluateDailyAudit(repos)
        val single = assertIs<NotificationDecision.DailyAuditDecision.DailyAuditSingleApp>(decision)
        assertEquals("Update available: Obtainium", single.title)
        assertEquals("Obtainium can be updated from 1.1.2 to 1.1.4.", single.body)
    }

    @Test
    fun testDailyAuditGroupedWhenMultipleAppsOutdated() {
        val repos = listOf(
            baseRepo.copy(id = "app/one", installedVersion = "1.0.0", latestRemoteVersion = "1.1.0"),
            baseRepo.copy(id = "app/two", installedVersion = "2.0.0", latestRemoteVersion = "2.1.0"),
            baseRepo.copy(id = "app/three", installedVersion = "3.0.0", latestRemoteVersion = "3.2.0")
        )
        val decision = NotificationDecisionEngine.evaluateDailyAudit(repos)
        val grouped = assertIs<NotificationDecision.DailyAuditDecision.DailyAuditGrouped>(decision)
        assertEquals(3, grouped.outdatedCount)
        assertEquals("3 apps have updates available", grouped.title)
        assertEquals("Tap to review and download the latest releases.", grouped.body)
    }
}
