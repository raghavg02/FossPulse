package io.github.raghavg02.fosspulse.platform.ai

expect class LocalLlmEngine(modelStorage: PlatformModelStorage = PlatformModelStorage()) {
    suspend fun summarize(rawChangelog: String, modelFileName: String): Result<String>
}
