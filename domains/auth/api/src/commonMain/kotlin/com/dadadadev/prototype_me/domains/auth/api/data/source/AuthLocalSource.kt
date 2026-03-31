package com.dadadadev.prototype_me.domains.auth.api.data.source

import com.dadadadev.prototype_me.core.common.error.NetworkException
import com.dadadadev.prototype_me.core.common.result.AppResult
import com.dadadadev.prototype_me.domains.auth.api.domain.model.AuthToken

interface AuthLocalSource {
    suspend fun saveToken(token: AuthToken): AppResult<Unit, NetworkException>

    suspend fun getToken(): AppResult<AuthToken?, NetworkException>

    suspend fun clearToken(): AppResult<Unit, NetworkException>
}
