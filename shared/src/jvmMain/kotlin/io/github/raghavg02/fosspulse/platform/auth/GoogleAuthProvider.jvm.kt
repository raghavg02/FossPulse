package io.github.raghavg02.fosspulse.platform.auth

import io.github.raghavg02.fosspulse.platform.storage.SecureStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.awt.Desktop
import java.net.URI

actual class GoogleAuthProvider actual constructor(
    private val secureStorage: SecureStorage
) {
    companion object {
        const val DRIVE_APPDATA_SCOPE = "https://www.googleapis.com/auth/drive.appdata"
        private const val TOKEN_KEY = "google_drive_access_token"
        private const val EMAIL_KEY = "google_drive_user_email"
    }

    actual suspend fun signIn(): Result<GoogleAuthResult> = withContext(Dispatchers.IO) {
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
            val clientId = secureStorage.get("google_oauth_client_id") ?: "fosspulse-desktop.apps.googleusercontent.com"
            val authUrl = "https://accounts.google.com/o/oauth2/v2/auth?" +
                    "client_id=$clientId&" +
                    "response_type=token&" +
                    "redirect_uri=http://localhost:8080/oauth2callback&" +
                    "scope=https://www.googleapis.com/auth/drive.appdata&" +
                    "prompt=consent"

            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(URI(authUrl))
            }

            val token = secureStorage.get(TOKEN_KEY)
            if (!token.isNullOrBlank()) {
                Result.success(GoogleAuthResult(accessToken = token, email = secureStorage.get(EMAIL_KEY)))
            } else {
                Result.failure(IllegalStateException("Browser authorization launched. Complete login to finish Drive sync."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    actual suspend fun signOut(): Result<Unit> = withContext(Dispatchers.IO) {
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
