package com.dadadadev.prototype_me.feature.home.di

import com.dadadadev.prototype_me.domains.auth.api.domain.usecase.GetSavedAuthTokenUseCase
import com.dadadadev.prototype_me.domains.auth.api.domain.usecase.RegisterGuestUseCase
import com.dadadadev.prototype_me.domains.auth.api.domain.usecase.ClearAuthTokenUseCase
import com.dadadadev.prototype_me.domains.erd.design.api.data.repository.ErdBoardRemoteRepository
import com.dadadadev.prototype_me.feature.home.presentation.HomeUseCases
import com.dadadadev.prototype_me.feature.home.presentation.HomeViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val homeFeatureModule = module {
    factory {
        HomeUseCases(
            getSavedAuthToken = get<GetSavedAuthTokenUseCase>(),
            registerGuest = get<RegisterGuestUseCase>(),
            clearAuthToken = get<ClearAuthTokenUseCase>(),
            erdBoardRemoteRepository = get<ErdBoardRemoteRepository>(),
        )
    }

    viewModel {
        HomeViewModel(useCases = get())
    }
}
