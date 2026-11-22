package io.github.raghavg02.fosspulse.platform.ai

import io.github.raghavg02.fosspulse.data.local.appContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

actual class PlatformModelStorage actual constructor() {

    private val modelsDir: File
        get() {
            val baseDir = appContext?.filesDir ?: File(System.getProperty("java.io.tmpdir") ?: ".")
            val dir = File(baseDir, "models")
            if (!dir.exists()) dir.mkdirs()
            return dir
        }

    actual fun getModelFileSizeBytes(fileName: String): Long {
        val file = File(modelsDir, fileName)
        return if (file.exists()) file.length() else 0L
    }

    actual fun isModelFilePresent(fileName: String): Boolean {
        val file = File(modelsDir, fileName)
        return file.exists() && file.length() > 0
    }

    actual fun deleteModelFile(fileName: String): Boolean {
        val file = File(modelsDir, fileName)
        return if (file.exists()) file.delete() else false
    }

    actual fun getModelFilePath(fileName: String): String {
        return File(modelsDir, fileName).absolutePath
    }

    actual suspend fun writeChunk(fileName: String, bytes: ByteArray, append: Boolean) = withContext(Dispatchers.IO) {
        val dir = modelsDir
        if (!dir.exists()) {
            dir.mkdirs()
        }
        val file = File(dir, fileName)
        FileOutputStream(file, append).use { fos ->
            fos.write(bytes)
            fos.flush()
        }
    }
}
