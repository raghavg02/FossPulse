package io.github.raghavg02.fosspulse.platform.storage

import java.io.File
import java.util.Base64
import java.util.Properties

actual class SecureStorage actual constructor() {

    private val storageDir: File = File(System.getProperty("user.home"), ".fosspulse").apply {
        if (!exists()) mkdirs()
    }
    private val credentialsFile: File = File(storageDir, ".credentials")
    private val properties = Properties()

    init {
        loadProperties()
    }

    private fun loadProperties() {
        if (credentialsFile.exists()) {
            try {
                credentialsFile.inputStream().use { properties.load(it) }
            } catch (_: Exception) {}
        }
    }

    private fun saveProperties() {
        try {
            credentialsFile.outputStream().use { properties.store(it, "FossPulse Secure Storage") }
        } catch (_: Exception) {}
    }

    actual fun save(key: String, value: String) {
        val encoded = Base64.getEncoder().encodeToString(value.toByteArray(Charsets.UTF_8))
        properties.setProperty(key, encoded)
        saveProperties()
    }

    actual fun get(key: String): String? {
        val encoded = properties.getProperty(key) ?: return null
        return try {
            val decoded = Base64.getDecoder().decode(encoded)
            String(decoded, Charsets.UTF_8)
        } catch (_: Exception) {
            encoded
        }
    }

    actual fun remove(key: String) {
        properties.remove(key)
        saveProperties()
    }

    actual fun clear() {
        properties.clear()
        saveProperties()
    }
}
