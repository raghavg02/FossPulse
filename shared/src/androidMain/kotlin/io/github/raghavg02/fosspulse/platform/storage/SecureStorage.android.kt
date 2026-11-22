package io.github.raghavg02.fosspulse.platform.storage

import android.content.Context
import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import io.github.raghavg02.fosspulse.data.local.appContext
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

actual class SecureStorage actual constructor() {

    private val prefsName = "fosspulse_secure_prefs"
    private val keyAlias = "FossPulseMasterKey"
    private val androidKeyStore = "AndroidKeyStore"
    private val transformation = "AES/GCM/NoPadding"
    private val gcmTagLength = 128
    private val ivLength = 12

    private val prefs: SharedPreferences?
        get() = appContext?.getSharedPreferences(prefsName, Context.MODE_PRIVATE)

    init {
        getOrCreateSecretKey()
    }

    private fun getOrCreateSecretKey(): SecretKey? {
        return try {
            val keyStore = KeyStore.getInstance(androidKeyStore).apply { load(null) }
            if (!keyStore.containsAlias(keyAlias)) {
                val keyGenerator = KeyGenerator.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES,
                    androidKeyStore
                )
                val spec = KeyGenParameterSpec.Builder(
                    keyAlias,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setRandomizedEncryptionRequired(true)
                    .build()
                keyGenerator.init(spec)
                keyGenerator.generateKey()
            } else {
                val keyEntry = keyStore.getEntry(keyAlias, null) as? KeyStore.SecretKeyEntry
                keyEntry?.secretKey
            }
        } catch (_: Exception) {
            null
        }
    }

    actual fun save(key: String, value: String) {
        val secretKey = getOrCreateSecretKey()
        val p = prefs ?: return
        if (secretKey == null) {
            p.edit().putString(key, value).apply()
            return
        }
        try {
            val cipher = Cipher.getInstance(transformation)
            cipher.init(Cipher.ENCRYPT_MODE, secretKey)
            val iv = cipher.iv
            val cipherText = cipher.doFinal(value.toByteArray(Charsets.UTF_8))
            val combined = ByteArray(iv.size + cipherText.size)
            System.arraycopy(iv, 0, combined, 0, iv.size)
            System.arraycopy(cipherText, 0, combined, iv.size, cipherText.size)
            val encoded = Base64.encodeToString(combined, Base64.NO_WRAP)
            p.edit().putString(key, encoded).apply()
        } catch (_: Exception) {
            p.edit().putString(key, value).apply()
        }
    }

    actual fun get(key: String): String? {
        val p = prefs ?: return null
        val raw = p.getString(key, null) ?: return null
        val secretKey = getOrCreateSecretKey() ?: return raw
        return try {
            val combined = Base64.decode(raw, Base64.NO_WRAP)
            if (combined.size < ivLength) return raw
            val iv = combined.copyOfRange(0, ivLength)
            val cipherText = combined.copyOfRange(ivLength, combined.size)
            val cipher = Cipher.getInstance(transformation)
            val spec = GCMParameterSpec(gcmTagLength, iv)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)
            val plain = cipher.doFinal(cipherText)
            String(plain, Charsets.UTF_8)
        } catch (_: Exception) {
            raw
        }
    }

    actual fun remove(key: String) {
        prefs?.edit()?.remove(key)?.apply()
    }

    actual fun clear() {
        prefs?.edit()?.clear()?.apply()
    }
}
