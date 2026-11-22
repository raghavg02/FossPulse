package io.github.raghavg02.fosspulse.domain.model

sealed interface NetworkResult<out T> {
    data class Success<out T>(
        val data: T,
        val etag: String? = null,
        val rateLimitRemaining: Int? = null,
        val rateLimitResetEpochSeconds: Long? = null
    ) : NetworkResult<T>

    data class NotModified(
        val etag: String? = null
    ) : NetworkResult<Nothing>

    data class RateLimited(
        val resetEpochSeconds: Long
    ) : NetworkResult<Nothing>

    data class Error(
        val message: String,
        val statusCode: Int? = null,
        val throwable: Throwable? = null
    ) : NetworkResult<Nothing>
}
