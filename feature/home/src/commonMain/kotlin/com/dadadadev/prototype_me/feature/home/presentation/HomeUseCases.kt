package com.dadadadev.prototype_me.feature.home.presentation

import com.dadadadev.prototype_me.domains.auth.api.domain.usecase.GetSavedAuthTokenUseCase
import com.dadadadev.prototype_me.domains.auth.api.domain.usecase.RegisterGuestUseCase
import com.dadadadev.prototype_me.domains.auth.api.domain.usecase.ClearAuthTokenUseCase
import com.dadadadev.prototype_me.domains.erd.design.api.data.repository.ErdBoardRemoteRepository

data class HomeUseCases(
    val getSavedAuthToken: GetSavedAuthTokenUseCase,
    val registerGuest: RegisterGuestUseCase,
    val clearAuthToken: ClearAuthTokenUseCase,
    val erdBoardRemoteRepository: ErdBoardRemoteRepository,
)
