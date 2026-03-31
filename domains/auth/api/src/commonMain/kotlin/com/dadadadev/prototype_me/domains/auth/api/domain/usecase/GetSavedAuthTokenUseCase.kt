package com.dadadadev.prototype_me.domains.auth.api.domain.usecase

import com.dadadadev.prototype_me.core.common.error.NetworkException
import com.dadadadev.prototype_me.core.common.result.AppResult
import com.dadadadev.prototype_me.domains.auth.api.domain.model.AuthToken

interface GetSavedAuthTokenUseCase {
    suspend operator fun invoke(): AppResult<AuthToken?, NetworkException>
}
