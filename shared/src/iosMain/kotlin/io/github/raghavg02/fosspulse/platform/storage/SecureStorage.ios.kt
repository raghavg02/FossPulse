package io.github.raghavg02.fosspulse.platform.storage

import platform.Foundation.NSUserDefaults

actual class SecureStorage actual constructor() {

    private val userDefaults = NSUserDefaults.standardUserDefaults
    private val prefix = "fosspulse_sec_"

    actual fun save(key: String, value: String) {
        userDefaults.setObject(value, forKey = prefix + key)
    }

    actual fun get(key: String): String? {
        return userDefaults.stringForKey(prefix + key)
    }

    actual fun remove(key: String) {
        userDefaults.removeObjectForKey(prefix + key)
    }

    actual fun clear() {
        val dict = userDefaults.dictionaryRepresentation()
        for (k in dict.keys) {
            val keyStr = k as? String ?: continue
            if (keyStr.startsWith(prefix)) {
                userDefaults.removeObjectForKey(keyStr)
            }
        }
    }
}
