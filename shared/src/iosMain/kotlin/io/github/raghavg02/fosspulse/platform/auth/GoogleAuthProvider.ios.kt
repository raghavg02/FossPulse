package io.github.raghavg02.fosspulse.platform.auth

import io.github.raghavg02.fosspulse.platform.storage.SecureStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import platform.Foundation.NSURL
import platform.UIKit.UIApplication

actual class GoogleAuthProvider actual constructor(
    private val secureStorage: SecureStorage
) {
    companion object {
        const val DRIVE_APPDATA_SCOPE = "https://www.googleapis.com/auth/drive.appdata"
        private const val TOKEN_KEY = "google_drive_access_token"
        private const val EMAIL_KEY = "google_drive_user_email"
    }

    actual suspend fun signIn(): Result<GoogleAuthResult> = withContext(Dispatchers.Default) {
        val existingToken = getSavedAccessToken()
        if (!existingToken.isNullOrBlank()) {
            return@withContext Result.success(
                GoogleAuthResult(
                    accessToken = existingToken,
                    email = secureStorage.get(EMAIL_KEY)
                )
            )
        }

        try {
            val clientId = secureStorage.get("google_oauth_client_id")
            if (clientId.isNullOrBlank()) {
                return@withContext Result.failure(
                    IllegalStateException("Google OAuth Client ID is not configured. Please enter your OAuth Client ID below before connecting.")
                )
            }
            val authUrl = "https://accounts.google.com/o/oauth2/v2/auth?" +
                    "client_id=$clientId&" +
                    "response_type=token&" +
                    "redirect_uri=io.github.raghavg02.fosspulse:/oauth2callback&" +
                    "scope=https://www.googleapis.com/auth/drive.appdata&" +
                    "prompt=consent"

            val url = NSURL.URLWithString(authUrl)
            if (url != null) {
                UIApplication.sharedApplication.openURL(url)
            }

            val token = secureStorage.get(TOKEN_KEY)
            if (!token.isNullOrBlank()) {
                Result.success(GoogleAuthResult(accessToken = token, email = secureStorage.get(EMAIL_KEY)))
            } else {
                Result.failure(IllegalStateException("iOS Safari authorization launched. Complete consent to connect Drive."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    actual suspend fun signOut(): Result<Unit> = withContext(Dispatchers.Default) {
        secureStorage.remove(TOKEN_KEY)
        secureStorage.remove(EMAIL_KEY)
        Result.success(Unit)
    }

    actual fun getSavedAccessToken(): String? {
        return secureStorage.get(TOKEN_KEY)
    }

    fun saveToken(token: String, email: String? = null) {
        secureStorage.save(TOKEN_KEY, token)
        if (email != null) {
            secureStorage.save(EMAIL_KEY, email)
        }
    }
}
