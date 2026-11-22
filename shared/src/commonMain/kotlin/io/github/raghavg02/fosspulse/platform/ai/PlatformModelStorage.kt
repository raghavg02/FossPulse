package io.github.raghavg02.fosspulse.platform.ai

expect class PlatformModelStorage() {
    fun getModelFileSizeBytes(fileName: String): Long
    fun isModelFilePresent(fileName: String): Boolean
    fun deleteModelFile(fileName: String): Boolean
    fun getModelFilePath(fileName: String): String
    suspend fun writeChunk(fileName: String, bytes: ByteArray, append: Boolean)
}
