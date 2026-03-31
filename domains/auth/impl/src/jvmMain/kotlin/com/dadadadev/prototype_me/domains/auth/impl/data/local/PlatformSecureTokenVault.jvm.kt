package com.dadadadev.prototype_me.domains.auth.impl.data.local

import java.io.File
import java.nio.file.Files
import java.nio.file.attribute.PosixFilePermission
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

internal actual class PlatformSecureTokenVault actual constructor() {
    private val lock = Any()
    private val storageDir: File = resolveStorageDirectory()
    private val tokenFile = File(storageDir, TOKEN_FILE_NAME)
    private val keyFile = File(storageDir, KEY_FILE_NAME)

    actual suspend fun write(value: String) {
        synchronized(lock) {
            val cipher = Cipher.getInstance(AES_TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, getOrCreateSecretKey())

            val encrypted = cipher.doFinal(value.encodeToByteArray())
            val payload = encode(cipher.iv) + PAYLOAD_DELIMITER + encode(encrypted)

            ensureDirectory()
            tokenFile.writeText(payload)
            enforceOwnerOnlyPermissions(tokenFile)
        }
    }

    actual suspend fun read(): String? {
        synchronized(lock) {
            if (!tokenFile.exists()) return null

            val payload = tokenFile.readText()
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
            if (tokenFile.exists()) {
                tokenFile.delete()
            }
        }
    }

    private fun getOrCreateSecretKey(): SecretKey {
        if (keyFile.exists()) {
            val encoded = keyFile.readText().trim()
            val bytes = decode(encoded)
            return SecretKeySpec(bytes, KEY_ALGORITHM)
        }

        val keyBytes = ByteArray(AES_KEY_SIZE_BYTES)
        SecureRandom().nextBytes(keyBytes)

        ensureDirectory()
        keyFile.writeText(encode(keyBytes))
        enforceOwnerOnlyPermissions(keyFile)

        return SecretKeySpec(keyBytes, KEY_ALGORITHM)
    }

    private fun resolveStorageDirectory(): File {
        val homeDir = System.getProperty("user.home").orEmpty().ifBlank { "." }
        return File(homeDir, STORAGE_DIRECTORY_NAME)
    }

    private fun ensureDirectory() {
        if (!storageDir.exists()) {
            storageDir.mkdirs()
            enforceOwnerOnlyPermissions(storageDir, isDirectory = true)
        }
    }

    private fun enforceOwnerOnlyPermissions(file: File, isDirectory: Boolean = false) {
        runCatching {
            val permissions = if (isDirectory) {
                setOf(
                    PosixFilePermission.OWNER_READ,
                    PosixFilePermission.OWNER_WRITE,
                    PosixFilePermission.OWNER_EXECUTE,
                )
            } else {
                setOf(
                    PosixFilePermission.OWNER_READ,
                    PosixFilePermission.OWNER_WRITE,
                )
            }
            Files.setPosixFilePermissions(
                file.toPath(),
                permissions,
            )
        }
    }

    private fun encode(bytes: ByteArray): String = Base64.getEncoder().encodeToString(bytes)

    private fun decode(value: String): ByteArray = Base64.getDecoder().decode(value)

    private companion object {
        const val STORAGE_DIRECTORY_NAME = ".prototype_me"
        const val TOKEN_FILE_NAME = "auth_token.sec"
        const val KEY_FILE_NAME = "auth_token.key"

        const val KEY_ALGORITHM = "AES"
        const val AES_TRANSFORMATION = "AES/GCM/NoPadding"
        const val AES_KEY_SIZE_BYTES = 32
        const val GCM_TAG_LENGTH_BITS = 128
        const val PAYLOAD_DELIMITER = ':'
    }
}
