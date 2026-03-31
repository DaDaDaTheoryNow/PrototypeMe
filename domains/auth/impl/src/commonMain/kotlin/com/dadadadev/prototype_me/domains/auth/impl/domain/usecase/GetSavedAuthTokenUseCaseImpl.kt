package com.dadadadev.prototype_me.domains.auth.impl.domain.usecase

import com.dadadadev.prototype_me.domains.auth.api.domain.repository.AuthRepository
import com.dadadadev.prototype_me.domains.auth.api.domain.usecase.GetSavedAuthTokenUseCase

internal class GetSavedAuthTokenUseCaseImpl(
    private val repository: AuthRepository,
) : GetSavedAuthTokenUseCase {
    override suspend fun invoke() = repository.getSavedToken()
}
