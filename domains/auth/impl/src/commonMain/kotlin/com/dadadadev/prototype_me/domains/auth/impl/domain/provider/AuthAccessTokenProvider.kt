package com.dadadadev.prototype_me.domains.auth.impl.domain.provider

import com.dadadadev.prototype_me.core.common.result.AppResult
import com.dadadadev.prototype_me.core.network.client.AccessTokenProvider
import com.dadadadev.prototype_me.domains.auth.api.data.source.AuthLocalSource

internal class AuthAccessTokenProvider(
    private val localSource: AuthLocalSource,
) : AccessTokenProvider {
    override suspend fun getAccessToken(): String? =
        when (val result = localSource.getToken()) {
            is AppResult.Success -> result.data?.accessToken?.trim()?.takeIf { it.isNotEmpty() }
            is AppResult.Failure -> null
        }
}
