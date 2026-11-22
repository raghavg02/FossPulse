package io.github.raghavg02.fosspulse.data.remote

import io.github.raghavg02.fosspulse.domain.model.NetworkResult
import io.github.raghavg02.fosspulse.domain.model.ReleaseInfo

interface GitHubApiService {
    suspend fun getLatestRelease(
        owner: String,
        repo: String,
        assetRegex: String,
        etag: String? = null,
        includePrereleases: Boolean = false,
        personalAccessToken: String? = null
    ): NetworkResult<ReleaseInfo>
}
