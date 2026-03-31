package com.dadadadev.prototype_me.domains.auth.impl.data.local

import com.dadadadev.prototype_me.core.common.error.NetworkException
import com.dadadadev.prototype_me.core.common.result.AppResult
import com.dadadadev.prototype_me.domains.auth.api.data.source.AuthLocalSource
import com.dadadadev.prototype_me.domains.auth.api.domain.model.AuthToken
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

internal class SecurePersistedAuthLocalSource(
    private val vault: PlatformSecureTokenVault = PlatformSecureTokenVault(),
) : AuthLocalSource {
    private val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
    }

    override suspend fun saveToken(token: AuthToken): AppResult<Unit, NetworkException> =
        runStorageCall {
            vault.write(json.encodeToString(AuthToken.serializer(), token))
            Unit
        }

    override suspend fun getToken(): AppResult<AuthToken?, NetworkException> =
        runStorageCall {
            val raw = vault.read() ?: return@runStorageCall null
            json.decodeFromString(AuthToken.serializer(), raw)
        }

    override suspend fun clearToken(): AppResult<Unit, NetworkException> =
        runStorageCall {
            vault.clear()
            Unit
        }

    private suspend inline fun <T> runStorageCall(crossinline block: suspend () -> T): AppResult<T, NetworkException> =
        try {
            AppResult.Success(block())
        } catch (serializationException: SerializationException) {
            AppResult.Failure(
                NetworkException.SerializationError(
                    rawBody = null,
                    serializationCause = serializationException,
                ),
            )
        } catch (throwable: Throwable) {
            AppResult.Failure(NetworkException.Unknown(throwable))
        }
}
