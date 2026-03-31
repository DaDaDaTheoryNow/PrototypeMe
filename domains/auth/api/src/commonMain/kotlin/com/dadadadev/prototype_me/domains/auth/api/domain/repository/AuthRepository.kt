package com.dadadadev.prototype_me.domains.auth.api.domain.repository

import com.dadadadev.prototype_me.core.common.error.NetworkException
import com.dadadadev.prototype_me.core.common.result.AppResult
import com.dadadadev.prototype_me.domains.auth.api.domain.model.AuthToken

interface AuthRepository {
    suspend fun registerGuest(): AppResult<AuthToken, NetworkException>

    suspend fun getSavedToken(): AppResult<AuthToken?, NetworkException>

    suspend fun clearToken(): AppResult<Unit, NetworkException>
}
