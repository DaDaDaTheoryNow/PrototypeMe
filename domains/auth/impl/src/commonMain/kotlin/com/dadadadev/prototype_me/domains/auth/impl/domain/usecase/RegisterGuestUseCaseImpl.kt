package com.dadadadev.prototype_me.domains.auth.impl.domain.usecase

import com.dadadadev.prototype_me.domains.auth.api.domain.repository.AuthRepository
import com.dadadadev.prototype_me.domains.auth.api.domain.usecase.RegisterGuestUseCase

internal class RegisterGuestUseCaseImpl(
    private val repository: AuthRepository,
) : RegisterGuestUseCase {
    override suspend fun invoke() = repository.registerGuest()
}
