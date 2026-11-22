package io.github.raghavg02.fosspulse.domain.ai

import io.github.raghavg02.fosspulse.platform.ai.PlatformModelStorage
import io.ktor.client.HttpClient
import io.ktor.client.plugins.onDownload
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.contentLength
import io.ktor.http.isSuccess
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.readAvailable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

data class LlmModelInfo(
    val name: String,
    val fileName: String,
    val downloadUrl: String,
    val estimatedSizeBytes: Long
)

sealed interface LlmModelStatus {
    data object NotDownloaded : LlmModelStatus
    data class Downloading(val progress: Float) : LlmModelStatus
    data class Downloaded(val sizeBytes: Long) : LlmModelStatus
    data class Error(val message: String) : LlmModelStatus
}

class LlmModelManager(
    private val httpClient: HttpClient,
    private val storage: PlatformModelStorage = PlatformModelStorage()
) {
    companion object {
        val DEFAULT_MODEL = LlmModelInfo(
            name = "Qwen2.5 1.5B Instruct (Q4_K_M)",
            fileName = "qwen2.5-1.5b-instruct-q4_k_m.gguf",
            downloadUrl = "https://huggingface.co/Qwen/Qwen2.5-1.5B-Instruct-GGUF/resolve/main/qwen2.5-1.5b-instruct-q4_k_m.gguf",
            estimatedSizeBytes = 986_000_000L // ~940 MiB
        )

        val SMALL_MODEL = LlmModelInfo(
            name = "Qwen2.5 0.5B Instruct (Q4_K_M)",
            fileName = "qwen2.5-0.5b-instruct-q4_k_m.gguf",
            downloadUrl = "https://huggingface.co/Qwen/Qwen2.5-0.5B-Instruct-GGUF/resolve/main/qwen2.5-0.5b-instruct-q4_k_m.gguf",
            estimatedSizeBytes = 397_747_200L // ~379 MiB
        )
    }

    private var activeModel: LlmModelInfo = DEFAULT_MODEL

    private val _modelStatus = MutableStateFlow<LlmModelStatus>(LlmModelStatus.NotDownloaded)
    val modelStatus: StateFlow<LlmModelStatus> = _modelStatus.asStateFlow()

    init {
        refreshStatus()
    }

    fun getActiveModel(): LlmModelInfo = activeModel

    fun refreshStatus() {
        if (storage.isModelFilePresent(activeModel.fileName)) {
            val size = storage.getModelFileSizeBytes(activeModel.fileName)
            _modelStatus.value = LlmModelStatus.Downloaded(size)
        } else {
            _modelStatus.value = LlmModelStatus.NotDownloaded
        }
    }

    fun isModelDownloaded(): Boolean {
        return storage.isModelFilePresent(activeModel.fileName)
    }

    fun getModelSizeBytes(): Long {
        return storage.getModelFileSizeBytes(activeModel.fileName)
    }

    fun getFormattedModelSize(): String {
        val bytes = if (isModelDownloaded()) getModelSizeBytes() else activeModel.estimatedSizeBytes
        val mb = kotlin.math.round((bytes / (1024.0 * 1024.0)) * 10) / 10.0
        return "$mb MB"
    }

    fun deleteModel(): Boolean {
        val deleted = storage.deleteModelFile(activeModel.fileName)
        refreshStatus()
        return deleted
    }

    suspend fun downloadModel(): Result<Unit> = withContext(Dispatchers.Default) {
        if (isModelDownloaded()) {
            refreshStatus()
            return@withContext Result.success(Unit)
        }

        try {
            _modelStatus.value = LlmModelStatus.Downloading(0.0f)

            val response = httpClient.get(activeModel.downloadUrl)
            if (!response.status.isSuccess()) {
                val error = "Failed to download model: HTTP ${response.status.value}"
                _modelStatus.value = LlmModelStatus.Error(error)
                return@withContext Result.failure(IllegalStateException(error))
            }

            val totalBytes = response.contentLength() ?: activeModel.estimatedSizeBytes
            val channel: ByteReadChannel = response.bodyAsChannel()
            var downloadedBytes = 0L
            val buffer = ByteArray(256 * 1024)
            var append = false

            while (!channel.isClosedForRead) {
                val bytesRead = channel.readAvailable(buffer, 0, buffer.size)
                if (bytesRead <= 0) break

                val chunk = if (bytesRead == buffer.size) buffer else buffer.copyOf(bytesRead)
                storage.writeChunk(activeModel.fileName, chunk, append = append)
                append = true

                downloadedBytes += bytesRead
                val progress = if (totalBytes > 0) (downloadedBytes.toFloat() / totalBytes.toFloat()).coerceIn(0f, 1f) else 0.5f
                _modelStatus.value = LlmModelStatus.Downloading(progress)
            }

            refreshStatus()
            Result.success(Unit)
        } catch (t: Throwable) {
            val message = t.message ?: "Download failed"
            _modelStatus.value = LlmModelStatus.Error(message)
            Result.failure(t)
        }
    }
}
