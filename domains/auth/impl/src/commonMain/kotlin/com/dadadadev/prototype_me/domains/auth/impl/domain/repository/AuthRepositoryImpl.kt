package com.dadadadev.prototype_me.domains.auth.impl.domain.repository

import com.dadadadev.prototype_me.core.common.error.NetworkException
import com.dadadadev.prototype_me.core.common.result.AppResult
import com.dadadadev.prototype_me.domains.auth.api.data.source.AuthLocalSource
import com.dadadadev.prototype_me.domains.auth.api.data.source.AuthRemoteSource
import com.dadadadev.prototype_me.domains.auth.api.domain.model.AuthToken
import com.dadadadev.prototype_me.domains.auth.api.domain.repository.AuthRepository

internal class AuthRepositoryImpl(
    private val remoteSource: AuthRemoteSource,
    private val localSource: AuthLocalSource,
) : AuthRepository {
    override suspend fun registerGuest(): AppResult<AuthToken, NetworkException> =
        when (val remoteResult = remoteSource.registerGuest()) {
            is AppResult.Success -> {
                when (val localResult = localSource.saveToken(remoteResult.data)) {
                    is AppResult.Success -> remoteResult
                    is AppResult.Failure -> AppResult.Failure(localResult.error)
                }
            }

            is AppResult.Failure -> remoteResult
        }

    override suspend fun getSavedToken(): AppResult<AuthToken?, NetworkException> = localSource.getToken()

    override suspend fun clearToken(): AppResult<Unit, NetworkException> = localSource.clearToken()
}
