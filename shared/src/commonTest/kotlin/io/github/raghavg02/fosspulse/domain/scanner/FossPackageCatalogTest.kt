package io.github.raghavg02.fosspulse.domain.scanner

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class FossPackageCatalogTest {

    @Test
    fun testFindKnownMappingExactAndCaseInsensitive() {
        val mapping = FossPackageCatalog.findMapping("dev.imranr.obtainium")
        assertNotNull(mapping)
        assertEquals("Obtainium", mapping.displayName)
        assertEquals("imranr98/obtainium", mapping.repoId)

        val upperMapping = FossPackageCatalog.findMapping("DEV.IMRANR.OBTAINIUM")
        assertNotNull(upperMapping)
        assertEquals("imranr98/obtainium", upperMapping.repoId)

        val vlcMapping = FossPackageCatalog.findMapping("org.videolan.vlc")
        assertNotNull(vlcMapping)
        assertEquals("videolan/vlc-android", vlcMapping.repoId)

        val affineMapping = FossPackageCatalog.findMapping("pro.affine.app")
        assertNotNull(affineMapping)
        assertEquals("toeverything/affine", affineMapping.repoId)

        val sealMapping = FossPackageCatalog.findMapping("com.junkfood.seal")
        assertNotNull(sealMapping)
        assertEquals("junkfood02/seal", sealMapping.repoId)
    }

    @Test
    fun testUnknownPackageReturnsNull() {
        val mapping = FossPackageCatalog.findMapping("com.unknown.proprietary.app")
        assertNull(mapping)
    }

    @Test
    fun testDefaultRegexForAbi() {
        val arm64Regex = FossPackageCatalog.defaultRegexForAbi("arm64-v8a")
        assertTrue(arm64Regex.contains("arm64"))

        val v7aRegex = FossPackageCatalog.defaultRegexForAbi("armeabi-v7a")
        assertTrue(v7aRegex.contains("armv7") || v7aRegex.contains("armeabi"))

        val x86Regex = FossPackageCatalog.defaultRegexForAbi("x86_64")
        assertTrue(x86Regex.contains("x86_64"))

        val genericRegex = FossPackageCatalog.defaultRegexForAbi("unknown_abi")
        assertEquals(".*\\.apk$", genericRegex)
    }
}
