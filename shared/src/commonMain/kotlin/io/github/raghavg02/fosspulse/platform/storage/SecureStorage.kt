package io.github.raghavg02.fosspulse.platform.storage

expect class SecureStorage() {
    fun save(key: String, value: String)
    fun get(key: String): String?
    fun remove(key: String)
    fun clear()
}
