package io.github.raghavg02.fosspulse.platform.auth

import android.content.Context
import android.content.Intent
import android.net.Uri
import io.github.raghavg02.fosspulse.data.local.appContext
import io.github.raghavg02.fosspulse.platform.storage.SecureStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

actual class GoogleAuthProvider actual constructor(
    private val secureStorage: SecureStorage
) {
    companion object {
        const val DRIVE_APPDATA_SCOPE = "https://www.googleapis.com/auth/drive.appdata"
        private const val TOKEN_KEY = "google_drive_access_token"
        private const val EMAIL_KEY = "google_drive_user_email"
    }

    private val context: Context?
        get() = appContext

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
            val clientId = secureStorage.get("google_oauth_client_id")
            if (clientId.isNullOrBlank()) {
                return@withContext Result.failure(
                    IllegalStateException("Google OAuth Client ID is not configured. Please enter your OAuth Client ID below before connecting.")
                )
            }

            val authUri = Uri.parse("https://accounts.google.com/o/oauth2/v2/auth").buildUpon()
                .appendQueryParameter("client_id", clientId)
                .appendQueryParameter("response_type", "token")
                .appendQueryParameter("redirect_uri", "io.github.raghavg02.fosspulse:/oauth2callback")
                .appendQueryParameter("scope", DRIVE_APPDATA_SCOPE)
                .appendQueryParameter("prompt", "consent")
                .build()

            val intent = Intent(Intent.ACTION_VIEW, authUri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context?.startActivity(intent)

            // If token was already set or passed in
            val token = secureStorage.get(TOKEN_KEY)
            if (!token.isNullOrBlank()) {
                Result.success(GoogleAuthResult(accessToken = token, email = secureStorage.get(EMAIL_KEY)))
            } else {
                // Return a pending auth placeholder or token if available
                Result.failure(IllegalStateException("OAuth browser consent opened. Authorize access to complete Drive sync."))
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
