package io.github.raghavg02.fosspulse.domain.ai

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ChangelogSemanticSummarizerTest {

    @Test
    fun testStructuredChangelogExtraction() {
        val rawMarkdown = """
            # Release 2.0.0
            
            ## Features
            - Added support for Google Drive hidden appDataFolder sync
            - Introduced on-device local LLM changelog summarizer
            
            ## Bug Fixes
            - Fixed ReDoS vulnerability in regex asset matcher
            - Resolved occasional background worker crash on Android 14
            
            ## Breaking Changes
            - Deprecated legacy v1 sync format in favor of schemaVersion 1
        """.trimIndent()

        val summary = ChangelogSemanticSummarizer.generateSummary(rawMarkdown)

        assertTrue(summary.contains("[FEATURES]"))
        assertTrue(summary.contains("[FIXES]"))
        assertTrue(summary.contains("[BREAKING]"))
        assertTrue(summary.lines().size in 3..4)
    }

    @Test
    fun testEmptyChangelogFallback() {
        val emptyChangelog = ""
        val summary = ChangelogSemanticSummarizer.generateSummary(emptyChangelog)

        assertTrue(summary.contains("[FEATURES]"))
        assertTrue(summary.contains("[FIXES]"))
        assertTrue(summary.contains("[BREAKING]"))
        assertTrue(summary.lines().isNotEmpty())
    }
}
