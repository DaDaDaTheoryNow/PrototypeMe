package com.dadadadev.prototype_me.domains.auth.impl.data.local

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
internal expect class PlatformSecureTokenVault() {
    suspend fun write(value: String)

    suspend fun read(): String?

    suspend fun clear()
}
