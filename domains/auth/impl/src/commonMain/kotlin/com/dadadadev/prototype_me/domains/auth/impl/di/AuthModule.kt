package com.dadadadev.prototype_me.domains.auth.impl.di

import com.dadadadev.prototype_me.domains.auth.api.data.source.AuthLocalSource
import com.dadadadev.prototype_me.domains.auth.api.data.source.AuthRemoteSource
import com.dadadadev.prototype_me.domains.auth.api.domain.repository.AuthRepository
import com.dadadadev.prototype_me.domains.auth.api.domain.usecase.ClearAuthTokenUseCase
import com.dadadadev.prototype_me.domains.auth.api.domain.usecase.GetSavedAuthTokenUseCase
import com.dadadadev.prototype_me.domains.auth.api.domain.usecase.RegisterGuestUseCase
import com.dadadadev.prototype_me.core.network.client.AccessTokenProvider
import com.dadadadev.prototype_me.domains.auth.impl.data.local.SecurePersistedAuthLocalSource
import com.dadadadev.prototype_me.domains.auth.impl.data.remote.AuthHttpClientProvider
import com.dadadadev.prototype_me.domains.auth.impl.data.remote.KtorAuthRemoteSource
import com.dadadadev.prototype_me.domains.auth.impl.domain.provider.AuthAccessTokenProvider
import com.dadadadev.prototype_me.domains.auth.impl.domain.repository.AuthRepositoryImpl
import com.dadadadev.prototype_me.domains.auth.impl.domain.usecase.ClearAuthTokenUseCaseImpl
import com.dadadadev.prototype_me.domains.auth.impl.domain.usecase.GetSavedAuthTokenUseCaseImpl
import com.dadadadev.prototype_me.domains.auth.impl.domain.usecase.RegisterGuestUseCaseImpl
import org.koin.core.qualifier.named
import org.koin.dsl.module

private const val AUTH_HTTP_CLIENT_QUALIFIER = "auth_http_client"

val authModule = module {
    single(named(AUTH_HTTP_CLIENT_QUALIFIER)) { AuthHttpClientProvider().createClient() }

    single<AuthRemoteSource> { KtorAuthRemoteSource(client = get(named(AUTH_HTTP_CLIENT_QUALIFIER))) }
    single<AuthLocalSource> { SecurePersistedAuthLocalSource() }
    single<AccessTokenProvider> { AuthAccessTokenProvider(localSource = get()) }
    single<AuthRepository> { AuthRepositoryImpl(remoteSource = get(), localSource = get()) }

    factory<RegisterGuestUseCase> { RegisterGuestUseCaseImpl(repository = get()) }
    factory<GetSavedAuthTokenUseCase> { GetSavedAuthTokenUseCaseImpl(repository = get()) }
    factory<ClearAuthTokenUseCase> { ClearAuthTokenUseCaseImpl(repository = get()) }
}
