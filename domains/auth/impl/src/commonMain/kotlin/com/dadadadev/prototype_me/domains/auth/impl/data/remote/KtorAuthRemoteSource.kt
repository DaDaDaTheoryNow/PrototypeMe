package com.dadadadev.prototype_me.domains.auth.impl.data.remote

import com.dadadadev.prototype_me.core.common.error.NetworkException
import com.dadadadev.prototype_me.core.common.result.AppResult
import com.dadadadev.prototype_me.core.network.client.safeNetworkCall
import com.dadadadev.prototype_me.domains.auth.api.data.source.AuthRemoteSource
import com.dadadadev.prototype_me.domains.auth.api.domain.model.AuthToken
import com.dadadadev.prototype_me.domains.auth.impl.data.dto.RegisterGuestResponseDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post

internal class KtorAuthRemoteSource(
    private val client: HttpClient,
) : AuthRemoteSource {
    override suspend fun registerGuest(): AppResult<AuthToken, NetworkException> = safeNetworkCall {
        val response: RegisterGuestResponseDto = client.post("/auth/register-guest").body()
        AuthToken(
            accessToken = response.data.accessToken,
            tokenType = response.data.tokenType,
        )
    }
}
