package io.github.raghavg02.fosspulse.data.remote

import io.github.raghavg02.fosspulse.domain.model.NetworkResult
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull

class GitHubApiClientTest {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    private val sampleReleaseJson = """
        {
          "tag_name": "v1.2.0",
          "name": "Version 1.2.0 Stable",
          "body": "## What's Changed\n* Added Neo-Brutalism UI",
          "html_url": "https://github.com/imranr98/obtainium/releases/tag/v1.2.0",
          "published_at": "2024-03-01T12:00:00Z",
          "prerelease": false,
          "assets": [
            {
              "name": "obtainium-arm64-v8a.apk",
              "browser_download_url": "https://github.com/imranr98/obtainium/releases/download/v1.2.0/obtainium-arm64-v8a.apk",
              "size": 18000000,
              "content_type": "application/vnd.android.package-archive"
            }
          ]
        }
    """.trimIndent()

    private val sampleTagsJson = """
        [
          {
            "name": "v1.1.0",
            "commit": {
              "sha": "abcdef123456",
              "url": "https://api.github.com/repos/owner/repo/commits/abcdef123456"
            }
          }
        ]
    """.trimIndent()

    @Test
    fun testGetLatestReleaseReturns200WithEtagAndMatchedAsset() = runTest {
        val mockEngine = MockEngine { request ->
            val headers = headersOf(
                HttpHeaders.ContentType to listOf("application/json"),
                "ETag" to listOf("\"etag-12345\""),
                "x-ratelimit-remaining" to listOf("58"),
                "x-ratelimit-reset" to listOf("1700003600")
            )
            respond(sampleReleaseJson, HttpStatusCode.OK, headers)
        }

        val httpClient = HttpClient(mockEngine) {
            install(ContentNegotiation) { json(json) }
        }
        val apiService = GitHubApiServiceImpl(httpClient)

        val result = apiService.getLatestRelease(
            owner = "imranr98",
            repo = "obtainium",
            assetRegex = ".*arm64.*\\.apk$"
        )

        val success = assertIs<NetworkResult.Success<*>>(result)
        assertEquals("\"etag-12345\"", success.etag)
        assertEquals(58, success.rateLimitRemaining)

        val release = success.data as io.github.raghavg02.fosspulse.domain.model.ReleaseInfo
        assertEquals("v1.2.0", release.tagName)
        assertEquals("Version 1.2.0 Stable", release.releaseTitle)
        assertNotNull(release.matchedAsset)
        assertEquals("obtainium-arm64-v8a.apk", release.matchedAsset?.name)
    }

    @Test
    fun testGetLatestReleaseReturns304NotModified() = runTest {
        var ifNoneMatchReceived: String? = null
        val mockEngine = MockEngine { request ->
            ifNoneMatchReceived = request.headers["If-None-Match"]
            respond(
                content = "",
                status = HttpStatusCode.NotModified,
                headers = headersOf("ETag" to listOf("\"etag-12345\""))
            )
        }

        val httpClient = HttpClient(mockEngine) {
            install(ContentNegotiation) { json(json) }
        }
        val apiService = GitHubApiServiceImpl(httpClient)

        val result = apiService.getLatestRelease(
            owner = "imranr98",
            repo = "obtainium",
            assetRegex = ".*\\.apk$",
            etag = "\"etag-12345\""
        )

        assertEquals("\"etag-12345\"", ifNoneMatchReceived)
        assertIs<NetworkResult.NotModified>(result)
    }

    @Test
    fun testGetLatestReleaseFallsBackToTagsOn404() = runTest {
        val mockEngine = MockEngine { request ->
            if (request.url.encodedPath.contains("/releases/latest")) {
                respond("Not Found", HttpStatusCode.NotFound)
            } else if (request.url.encodedPath.contains("/tags")) {
                val headers = headersOf(
                    HttpHeaders.ContentType to listOf("application/json"),
                    "ETag" to listOf("\"tag-etag\"")
                )
                respond(sampleTagsJson, HttpStatusCode.OK, headers)
            } else {
                respond("Error", HttpStatusCode.BadRequest)
            }
        }

        val httpClient = HttpClient(mockEngine) {
            install(ContentNegotiation) { json(json) }
        }
        val apiService = GitHubApiServiceImpl(httpClient)

        val result = apiService.getLatestRelease(
            owner = "owner",
            repo = "repo",
            assetRegex = ".*\\.apk$"
        )

        val success = assertIs<NetworkResult.Success<*>>(result)
        val release = success.data as io.github.raghavg02.fosspulse.domain.model.ReleaseInfo
        assertEquals("v1.1.0", release.tagName)
        assertEquals("https://github.com/owner/repo/releases/tag/v1.1.0", release.htmlUrl)
    }

    @Test
    fun testRateLimitExceededReturnsRateLimitedResult() = runTest {
        val mockEngine = MockEngine {
            val headers = headersOf(
                "x-ratelimit-remaining" to listOf("0"),
                "x-ratelimit-reset" to listOf("1700009999")
            )
            respond("API rate limit exceeded", HttpStatusCode.Forbidden, headers)
        }

        val httpClient = HttpClient(mockEngine) {
            install(ContentNegotiation) { json(json) }
        }
        val apiService = GitHubApiServiceImpl(httpClient)

        val result = apiService.getLatestRelease(
            owner = "owner",
            repo = "repo",
            assetRegex = ".*"
        )

        val rateLimited = assertIs<NetworkResult.RateLimited>(result)
        assertEquals(1700009999L, rateLimited.resetEpochSeconds)
    }
}
