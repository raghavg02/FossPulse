package io.github.raghavg02.fosspulse.data.remote

import io.github.raghavg02.fosspulse.data.remote.dto.GitHubReleaseDto
import io.github.raghavg02.fosspulse.data.remote.dto.GitHubTagDto
import io.github.raghavg02.fosspulse.domain.engine.RegexAssetMatcher
import io.github.raghavg02.fosspulse.domain.model.NetworkResult
import io.github.raghavg02.fosspulse.domain.model.ReleaseInfo
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

class GitHubApiServiceImpl(
    private val client: HttpClient = createDefaultHttpClient()
) : GitHubApiService {

    companion object {
        const val BASE_URL = "https://api.github.com"
        const val HEADER_API_VERSION = "2022-11-28"
        const val USER_AGENT = "FossPulse-KMP"

        fun createDefaultHttpClient(): HttpClient {
            return HttpClient {
                install(ContentNegotiation) {
                    json(
                        Json {
                            ignoreUnknownKeys = true
                            isLenient = true
                            coerceInputValues = true
                        }
                    )
                }
            }
        }
    }

    override suspend fun getLatestRelease(
        owner: String,
        repo: String,
        assetRegex: String,
        etag: String?,
        includePrereleases: Boolean,
        personalAccessToken: String?
    ): NetworkResult<ReleaseInfo> {
        val endpoint = if (includePrereleases) {
            "$BASE_URL/repos/$owner/$repo/releases?per_page=5"
        } else {
            "$BASE_URL/repos/$owner/$repo/releases/latest"
        }

        return try {
            val response: HttpResponse = client.get(endpoint) {
                header("Accept", "application/vnd.github+json")
                header("X-GitHub-Api-Version", HEADER_API_VERSION)
                header("User-Agent", USER_AGENT)
                if (!etag.isNullOrBlank()) {
                    header("If-None-Match", etag)
                }
                if (!personalAccessToken.isNullOrBlank()) {
                    header("Authorization", "Bearer $personalAccessToken")
                }
            }

            val remainingLimit = response.headers["x-ratelimit-remaining"]?.toIntOrNull()
            val resetEpoch = response.headers["x-ratelimit-reset"]?.toLongOrNull()
            val responseEtag = response.headers["ETag"] ?: response.headers["etag"]

            when (response.status) {
                HttpStatusCode.NotModified -> {
                    NetworkResult.NotModified(etag = responseEtag ?: etag)
                }
                HttpStatusCode.OK -> {
                    val releaseDto = if (includePrereleases) {
                        val list = response.body<List<GitHubReleaseDto>>()
                        list.firstOrNull() ?: return fallbackToTags(owner, repo, personalAccessToken)
                    } else {
                        response.body<GitHubReleaseDto>()
                    }

                    val domainAssets = releaseDto.assets.map { it.toDomain() }
                    val matchedAsset = RegexAssetMatcher.matchAsset(domainAssets, assetRegex)
                    val releaseInfo = releaseDto.toDomain(matchedAsset = matchedAsset)

                    NetworkResult.Success(
                        data = releaseInfo,
                        etag = responseEtag,
                        rateLimitRemaining = remainingLimit,
                        rateLimitResetEpochSeconds = resetEpoch
                    )
                }
                HttpStatusCode.Forbidden, HttpStatusCode.TooManyRequests -> {
                    if (remainingLimit == 0 && resetEpoch != null) {
                        NetworkResult.RateLimited(resetEpochSeconds = resetEpoch)
                    } else {
                        NetworkResult.Error(
                            message = response.bodyAsText(),
                            statusCode = response.status.value
                        )
                    }
                }
                HttpStatusCode.NotFound -> {
                    // Fallback to /tags when repo has no releases
                    fallbackToTags(owner, repo, personalAccessToken)
                }
                else -> {
                    NetworkResult.Error(
                        message = "GitHub API returned status ${response.status.value}: ${response.bodyAsText()}",
                        statusCode = response.status.value
                    )
                }
            }
        } catch (e: Exception) {
            NetworkResult.Error(
                message = e.message ?: "Failed to connect to GitHub API",
                throwable = e
            )
        }
    }

    private suspend fun fallbackToTags(
        owner: String,
        repo: String,
        personalAccessToken: String?
    ): NetworkResult<ReleaseInfo> {
        return try {
            val tagsResponse: HttpResponse = client.get("$BASE_URL/repos/$owner/$repo/tags?per_page=5") {
                header("Accept", "application/vnd.github+json")
                header("X-GitHub-Api-Version", HEADER_API_VERSION)
                header("User-Agent", USER_AGENT)
                if (!personalAccessToken.isNullOrBlank()) {
                    header("Authorization", "Bearer $personalAccessToken")
                }
            }

            if (tagsResponse.status == HttpStatusCode.OK) {
                val tags = tagsResponse.body<List<GitHubTagDto>>()
                val latestTag = tags.firstOrNull()
                if (latestTag != null) {
                    val releaseInfo = ReleaseInfo(
                        tagName = latestTag.name,
                        releaseTitle = latestTag.name,
                        rawChangelogMd = "",
                        htmlUrl = "https://github.com/$owner/$repo/releases/tag/${latestTag.name}",
                        publishedAtEpochMs = 0L,
                        matchedAsset = null,
                        isPrerelease = false
                    )
                    NetworkResult.Success(
                        data = releaseInfo,
                        etag = tagsResponse.headers["ETag"],
                        rateLimitRemaining = tagsResponse.headers["x-ratelimit-remaining"]?.toIntOrNull(),
                        rateLimitResetEpochSeconds = tagsResponse.headers["x-ratelimit-reset"]?.toLongOrNull()
                    )
                } else {
                    NetworkResult.Error(
                        message = "Repository $owner/$repo has no published releases or git tags",
                        statusCode = 404
                    )
                }
            } else {
                NetworkResult.Error(
                    message = "Fallback to tags failed with status ${tagsResponse.status.value}",
                    statusCode = tagsResponse.status.value
                )
            }
        } catch (e: Exception) {
            NetworkResult.Error(
                message = "Failed to fetch fallback tags: ${e.message}",
                throwable = e
            )
        }
    }

    override suspend fun getRateLimit(personalAccessToken: String?): NetworkResult<Int> {
        return try {
            val response: HttpResponse = client.get("$BASE_URL/rate_limit") {
                header("Accept", "application/vnd.github+json")
                header("X-GitHub-Api-Version", HEADER_API_VERSION)
                header("User-Agent", USER_AGENT)
                if (!personalAccessToken.isNullOrBlank()) {
                    header("Authorization", "Bearer $personalAccessToken")
                }
            }

            if (response.status == HttpStatusCode.OK) {
                val limit = response.headers["x-ratelimit-limit"]?.toIntOrNull() ?: 60
                NetworkResult.Success(data = limit)
            } else {
                NetworkResult.Error(
                    message = "Rate limit check failed: ${response.status.value}",
                    statusCode = response.status.value
                )
            }
        } catch (e: Exception) {
            NetworkResult.Error(
                message = e.message ?: "Failed to query rate limit",
                throwable = e
            )
        }
    }
}
