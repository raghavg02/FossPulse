package io.github.raghavg02.fosspulse.platform.auth

import io.github.raghavg02.fosspulse.platform.storage.SecureStorage

data class GoogleAuthResult(
    val accessToken: String,
    val email: String? = null
)

expect class GoogleAuthProvider(secureStorage: SecureStorage = SecureStorage()) {
    suspend fun signIn(): Result<GoogleAuthResult>
    suspend fun signOut(): Result<Unit>
    fun getSavedAccessToken(): String?
}
