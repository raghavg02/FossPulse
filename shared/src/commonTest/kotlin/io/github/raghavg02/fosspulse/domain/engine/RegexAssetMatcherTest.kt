package io.github.raghavg02.fosspulse.domain.engine

import io.github.raghavg02.fosspulse.domain.model.ReleaseAsset
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class RegexAssetMatcherTest {

    private val sampleAssets = listOf(
        ReleaseAsset(name = "FossPulse-v1.4.0-arm64-v8a.apk", downloadUrl = "https://example.com/arm64.apk", sizeBytes = 25000000L),
        ReleaseAsset(name = "FossPulse-v1.4.0-armeabi-v7a.apk", downloadUrl = "https://example.com/v7a.apk", sizeBytes = 22000000L),
        ReleaseAsset(name = "FossPulse-v1.4.0-x86_64.apk", downloadUrl = "https://example.com/x86.apk", sizeBytes = 27000000L),
        ReleaseAsset(name = "FossPulse-v1.4.0-arm64-v8a.apk.sha256", downloadUrl = "https://example.com/sha256", sizeBytes = 64L),
        ReleaseAsset(name = "Source code.zip", downloadUrl = "https://example.com/src.zip", sizeBytes = 5000000L),
        ReleaseAsset(name = "Source code.tar.gz", downloadUrl = "https://example.com/src.tar.gz", sizeBytes = 4000000L)
    )

    @Test
    fun testMatchesExactArm64ApkAndIgnoresChecksum() {
        val regex = ".*(arm64|universal).*\\.apk$"
        val matched = RegexAssetMatcher.matchAsset(sampleAssets, regex)
        assertNotNull(matched)
        assertEquals("FossPulse-v1.4.0-arm64-v8a.apk", matched.name)
        assertEquals("https://example.com/arm64.apk", matched.downloadUrl)
    }

    @Test
    fun testReturnsNullWhenNoMatchingBinaryAttached() {
        // Only source code archives in this release (e.g., CI still compiling binaries)
        val sourceOnlyAssets = listOf(
            ReleaseAsset(name = "source.zip", downloadUrl = "https://example.com/source.zip", sizeBytes = 1024L),
            ReleaseAsset(name = "source.tar.gz", downloadUrl = "https://example.com/source.tar.gz", sizeBytes = 1024L)
        )
        val regex = ".*(arm64|universal).*\\.apk$"
        val matched = RegexAssetMatcher.matchAsset(sourceOnlyAssets, regex)
        assertNull(matched)
    }

    @Test
    fun testReDoSProtectionRejectsExcessiveLength() {
        val longRegex = "a".repeat(201)
        val result = RegexAssetMatcher.validatePattern(longRegex)
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("maximum length") == true)
    }

    @Test
    fun testRejectsBlankPattern() {
        val result = RegexAssetMatcher.validatePattern("   ")
        assertTrue(result.isFailure)
    }

    @Test
    fun testMatchesWindowsAndMacBinaries() {
        val desktopAssets = listOf(
            ReleaseAsset(name = "app-setup-x64.exe", downloadUrl = "https://example.com/setup.exe", sizeBytes = 60000000L),
            ReleaseAsset(name = "app-x64.msi", downloadUrl = "https://example.com/app.msi", sizeBytes = 60000000L),
            ReleaseAsset(name = "app-universal.dmg", downloadUrl = "https://example.com/app.dmg", sizeBytes = 70000000L)
        )

        val winMatched = RegexAssetMatcher.matchAsset(desktopAssets, ".*(setup|x64).*\\.exe$")
        assertNotNull(winMatched)
        assertEquals("app-setup-x64.exe", winMatched.name)

        val macMatched = RegexAssetMatcher.matchAsset(desktopAssets, ".*\\.dmg$")
        assertNotNull(macMatched)
        assertEquals("app-universal.dmg", macMatched.name)
    }
}
