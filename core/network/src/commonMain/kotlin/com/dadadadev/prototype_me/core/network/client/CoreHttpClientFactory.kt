package com.dadadadev.prototype_me.core.network.client

import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.request.accept
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.KotlinxWebsocketSerializationConverter
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

class CoreHttpClientFactory(
    private val baseUrl: String,
    private val json: Json,
    private val accessTokenProvider: AccessTokenProvider? = null,
    private val bearerToken: String? = null,
    private val timeoutMillis: Long = DEFAULT_TIMEOUT_MILLIS,
) {

    fun create(): HttpClient = HttpClient {
        expectSuccess = false

        install(ContentNegotiation) {
            json(json)
        }

        install(WebSockets) {
            contentConverter = KotlinxWebsocketSerializationConverter(json)
        }

        val staticToken = bearerToken.normalizeToken()

        if (staticToken != null || accessTokenProvider != null) {
            install(createClientPlugin("DynamicBearerTokenPlugin") {
                onRequest { request, _ ->
                    if (request.headers[HttpHeaders.Authorization] == null) {
                        val dynamicToken = accessTokenProvider?.getAccessToken().normalizeToken()
                        val resolvedToken = dynamicToken ?: staticToken
                        if (resolvedToken != null) {
                            request.header(HttpHeaders.Authorization, "Bearer $resolvedToken")
                        }
                    }
                }
            })
        }

        install(HttpTimeout) {
            requestTimeoutMillis = timeoutMillis
            connectTimeoutMillis = timeoutMillis
            socketTimeoutMillis = timeoutMillis
        }

        defaultRequest {
            url(baseUrl)
            accept(ContentType.Application.Json)
            header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
        }
    }

    private fun String?.normalizeToken(): String? {
        val raw = this?.trim()?.takeIf { it.isNotEmpty() } ?: return null
        val withoutScheme = if (raw.startsWith("Bearer ", ignoreCase = true)) {
            raw.substring("Bearer ".length).trim()
        } else {
            raw
        }
        return withoutScheme.takeIf { it.isNotEmpty() }
    }

    private companion object {
        const val DEFAULT_TIMEOUT_MILLIS = 20_000L
    }
}
