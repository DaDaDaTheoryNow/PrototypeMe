package com.dadadadev.prototype_me.domains.auth.impl.data.remote

import com.dadadadev.prototype_me.core.network.client.CoreHttpClientFactory
import io.ktor.client.HttpClient

internal class AuthHttpClientProvider {
    fun createClient(): HttpClient = CoreHttpClientFactory(
        baseUrl = AuthRemoteConfig.BASE_URL,
        json = AuthContractJson.json,
    ).create()
}
