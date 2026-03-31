package com.dadadadev.prototype_me.domains.auth.impl.data.local

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.io.File
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

internal actual class PlatformSecureTokenVault actual constructor() {
    private val lock = Any()
    private val storageFile: File = resolveStorageFile()
    private val keyStore: KeyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }

    actual suspend fun write(value: String) {
        synchronized(lock) {
            val cipher = Cipher.getInstance(AES_TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, getOrCreateSecretKey())
            val encryptedBytes = cipher.doFinal(value.encodeToByteArray())
            val payload = encode(cipher.iv) + PAYLOAD_DELIMITER + encode(encryptedBytes)

            ensureParentDirectory()
            storageFile.writeText(payload)
            lockDownFilePermissions(storageFile)
        }
    }

    actual suspend fun read(): String? {
        synchronized(lock) {
            if (!storageFile.exists()) return null

            val payload = storageFile.readText()
            val separatorIndex = payload.indexOf(PAYLOAD_DELIMITER)
            if (separatorIndex <= 0) return null

            val iv = decode(payload.substring(0, separatorIndex))
            val encrypted = decode(payload.substring(separatorIndex + 1))

            val cipher = Cipher.getInstance(AES_TRANSFORMATION)
            cipher.init(Cipher.DECRYPT_MODE, getOrCreateSecretKey(), GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv))
            val rawBytes = cipher.doFinal(encrypted)
            return rawBytes.decodeToString()
        }
    }

    actual suspend fun clear() {
        synchronized(lock) {
            if (storageFile.exists()) {
                storageFile.delete()
            }
        }
    }

    private fun getOrCreateSecretKey(): SecretKey {
        val existingEntry = keyStore.getEntry(KEY_ALIAS, null) as? KeyStore.SecretKeyEntry
        if (existingEntry != null) {
            return existingEntry.secretKey
        }

        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            ANDROID_KEYSTORE,
        )
        val keySpec = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(AES_KEY_SIZE_BITS)
            .build()
        keyGenerator.init(keySpec)
        return keyGenerator.generateKey()
    }

    private fun ensureParentDirectory() {
        storageFile.parentFile?.let { parent ->
            if (!parent.exists()) {
                parent.mkdirs()
            }
        }
    }

    private fun resolveStorageFile(): File {
        val homeDir = System.getProperty("user.home").orEmpty().ifBlank { "." }
        val baseDir = File(homeDir)
        return File(baseDir, "$DEFAULT_RELATIVE_STORAGE_DIR/$TOKEN_FILE_NAME")
    }

    private fun encode(bytes: ByteArray): String = Base64.encodeToString(bytes, Base64.NO_WRAP)

    private fun decode(value: String): ByteArray = Base64.decode(value, Base64.NO_WRAP)

    private fun lockDownFilePermissions(file: File) {
        file.setReadable(false, false)
        file.setWritable(false, false)
        file.setExecutable(false, false)
        file.setReadable(true, true)
        file.setWritable(true, true)
    }

    private companion object {
        const val ANDROID_KEYSTORE = "AndroidKeyStore"
        const val AES_TRANSFORMATION = "AES/GCM/NoPadding"
        const val AES_KEY_SIZE_BITS = 256
        const val GCM_TAG_LENGTH_BITS = 128
        const val KEY_ALIAS = "prototype_me_auth_token_vault"
        const val PAYLOAD_DELIMITER = ':'
        const val DEFAULT_RELATIVE_STORAGE_DIR = ".prototype_me"
        const val TOKEN_FILE_NAME = "auth_token.sec"
    }
}
