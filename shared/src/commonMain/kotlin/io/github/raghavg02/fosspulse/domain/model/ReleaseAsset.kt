package io.github.raghavg02.fosspulse.domain.model

data class ReleaseAsset(
    val name: String,
    val downloadUrl: String,
    val sizeBytes: Long,
    val contentType: String? = null
)
