package com.dadadadev.prototype_me.domains.auth.impl.domain.usecase

import com.dadadadev.prototype_me.domains.auth.api.domain.repository.AuthRepository
import com.dadadadev.prototype_me.domains.auth.api.domain.usecase.ClearAuthTokenUseCase

internal class ClearAuthTokenUseCaseImpl(
    private val repository: AuthRepository,
) : ClearAuthTokenUseCase {
    override suspend fun invoke() = repository.clearToken()
}
