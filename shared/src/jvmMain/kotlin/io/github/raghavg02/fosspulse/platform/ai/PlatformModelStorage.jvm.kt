package io.github.raghavg02.fosspulse.platform.ai

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

actual class PlatformModelStorage actual constructor() {

    private val modelsDir: File
        get() {
            val userHome = System.getProperty("user.home") ?: "."
            val dir = File(File(userHome, ".fosspulse"), "models")
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
        val file = File(modelsDir, fileName)
        FileOutputStream(file, append).use { fos ->
            fos.write(bytes)
        }
    }
}
