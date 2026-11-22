package io.github.raghavg02.fosspulse.domain.engine

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SemVerComparatorTest {

    @Test
    fun testVersionNormalizationWithPrefixes() {
        assertEquals(0, SemVerComparator.compare("v1.2.0", "1.2.0"))
        assertEquals(0, SemVerComparator.compare("V1.2.0", "v1.2.0"))
        assertEquals(0, SemVerComparator.compare("release-2.0.1", "2.0.1"))
        assertEquals(0, SemVerComparator.compare("rel_2.0.1", "2.0.1"))
        assertEquals(0, SemVerComparator.compare("ver-1.0", "1.0"))
        assertEquals(0, SemVerComparator.compare("v.1.0.0", "1.0.0"))
        assertTrue(SemVerComparator.compare("release-2.0.1", "2.0.0") > 0)
    }

    @Test
    fun testNumericVersionPrecedence() {
        assertTrue(SemVerComparator.compare("1.1.2", "1.1.4") < 0)
        assertTrue(SemVerComparator.compare("1.2.0", "1.1.9") > 0)
        assertTrue(SemVerComparator.compare("2.0.0", "1.99.99") > 0)
        assertEquals(0, SemVerComparator.compare("1.0", "1.0.0"))
        assertTrue(SemVerComparator.compare("1.0.0.1", "1.0.0") > 0)
    }

    @Test
    fun testPreReleaseHandling() {
        // A release version has higher precedence than a pre-release version: 1.2.0 > 1.2.0-beta.1
        assertTrue(SemVerComparator.compare("1.2.0", "1.2.0-beta.1") > 0)
        assertTrue(SemVerComparator.compare("1.2.0-beta.1", "1.2.0") < 0)

        // Pre-release ordering
        assertTrue(SemVerComparator.compare("1.2.0-alpha.1", "1.2.0-beta.1") < 0)
        assertTrue(SemVerComparator.compare("1.2.0-beta.1", "1.2.0-beta.2") < 0)
        assertTrue(SemVerComparator.compare("1.2.0-rc.1", "1.2.0-beta.5") > 0)
    }

    @Test
    fun testBuildMetadataIgnoredInPrecedence() {
        assertEquals(0, SemVerComparator.compare("1.1.4+45", "1.1.4"))
        assertEquals(0, SemVerComparator.compare("1.1.4+build.1", "1.1.4+build.2"))
    }

    @Test
    fun testNullHandling() {
        assertEquals(0, SemVerComparator.compare(null, null))
        assertTrue(SemVerComparator.compare(null, "1.0.0") < 0)
        assertTrue(SemVerComparator.compare("1.0.0", null) > 0)
    }
}
