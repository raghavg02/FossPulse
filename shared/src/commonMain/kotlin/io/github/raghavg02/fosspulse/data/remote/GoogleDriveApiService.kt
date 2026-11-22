package io.github.raghavg02.fosspulse.data.remote

import io.github.raghavg02.fosspulse.domain.sync.SyncPayload
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class DriveFileListResponse(
    val files: List<DriveFileItem> = emptyList()
)

@Serializable
data class DriveFileItem(
    val id: String,
    val name: String
)

interface GoogleDriveApiService {
    suspend fun findSyncFile(accessToken: String): String?
    suspend fun downloadSyncFile(accessToken: String, fileId: String): SyncPayload?
    suspend fun uploadSyncFile(accessToken: String, payload: SyncPayload, existingFileId: String? = null): String
}

class GoogleDriveApiServiceImpl(
    private val httpClient: HttpClient,
    private val json: Json = Json { ignoreUnknownKeys = true; prettyPrint = true }
) : GoogleDriveApiService {

    companion object {
        private const val SYNC_FILENAME = "fosspulse_sync.json"
        private const val DRIVE_API_URL = "https://www.googleapis.com/drive/v3/files"
        private const val UPLOAD_API_URL = "https://www.googleapis.com/upload/drive/v3/files"
    }

    override suspend fun findSyncFile(accessToken: String): String? {
        return try {
            val response = httpClient.get(DRIVE_API_URL) {
                header(HttpHeaders.Authorization, "Bearer $accessToken")
                parameter("spaces", "appDataFolder")
                parameter("q", "name='$SYNC_FILENAME' and trashed=false")
                parameter("fields", "files(id, name)")
            }
            if (response.status.isSuccess()) {
                val list = response.body<DriveFileListResponse>()
                list.files.firstOrNull()?.id
            } else {
                null
            }
        } catch (_: Exception) {
            null
        }
    }

    override suspend fun downloadSyncFile(accessToken: String, fileId: String): SyncPayload? {
        return try {
            val response = httpClient.get("$DRIVE_API_URL/$fileId") {
                header(HttpHeaders.Authorization, "Bearer $accessToken")
                parameter("alt", "media")
            }
            if (response.status.isSuccess()) {
                val text = response.bodyAsText()
                json.decodeFromString<SyncPayload>(text)
            } else {
                null
            }
        } catch (_: Exception) {
            null
        }
    }

    override suspend fun uploadSyncFile(
        accessToken: String,
        payload: SyncPayload,
        existingFileId: String?
    ): String {
        val payloadJson = json.encodeToString(SyncPayload.serializer(), payload)

        return if (existingFileId != null) {
            // Update existing file content
            val response = httpClient.patch("$UPLOAD_API_URL/$existingFileId") {
                header(HttpHeaders.Authorization, "Bearer $accessToken")
                parameter("uploadType", "media")
                contentType(ContentType.Application.Json)
                setBody(payloadJson)
            }
            if (response.status.isSuccess()) {
                val updated = response.body<DriveFileItem>()
                updated.id
            } else {
                throw IllegalStateException("Failed to update sync file: ${response.status}")
            }
        } else {
            // Create file metadata in appDataFolder first
            val metaResponse = httpClient.post(DRIVE_API_URL) {
                header(HttpHeaders.Authorization, "Bearer $accessToken")
                contentType(ContentType.Application.Json)
                setBody(
                    """
                    {
                        "name": "$SYNC_FILENAME",
                        "parents": ["appDataFolder"]
                    }
                    """.trimIndent()
                )
            }
            val created = metaResponse.body<DriveFileItem>()

            // Then upload media
            httpClient.patch("$UPLOAD_API_URL/${created.id}") {
                header(HttpHeaders.Authorization, "Bearer $accessToken")
                parameter("uploadType", "media")
                contentType(ContentType.Application.Json)
                setBody(payloadJson)
            }
            created.id
        }
    }
}
