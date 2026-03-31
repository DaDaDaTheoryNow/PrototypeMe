package com.dadadadev.prototype_me.domains.auth.api.domain.usecase

import com.dadadadev.prototype_me.core.common.error.NetworkException
import com.dadadadev.prototype_me.core.common.result.AppResult

interface ClearAuthTokenUseCase {
    suspend operator fun invoke(): AppResult<Unit, NetworkException>
}
