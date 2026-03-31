package com.dadadadev.prototype_me.domains.erd.design.impl.data.remote.network

import com.dadadadev.prototype_me.core.network.client.CoreHttpClientFactory
import com.dadadadev.prototype_me.core.network.client.AccessTokenProvider
import com.dadadadev.prototype_me.domains.erd.design.impl.data.remote.dto.BoardContractJson
import io.ktor.client.HttpClient

internal class ErdBoardHttpClientProvider(
    private val accessTokenProvider: AccessTokenProvider,
) {
    fun createClient(): HttpClient = CoreHttpClientFactory(
        baseUrl = ErdBoardRemoteConfig.BASE_URL,
        accessTokenProvider = accessTokenProvider,
        json = BoardContractJson.json,
    ).create()
}
