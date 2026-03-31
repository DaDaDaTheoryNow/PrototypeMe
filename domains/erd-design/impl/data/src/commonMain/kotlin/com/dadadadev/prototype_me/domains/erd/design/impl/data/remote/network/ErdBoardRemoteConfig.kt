package com.dadadadev.prototype_me.domains.erd.design.impl.data.remote.network

import io.ktor.http.URLBuilder
import io.ktor.http.URLProtocol
import io.ktor.http.Url
import io.ktor.http.appendPathSegments

internal object ErdBoardRemoteConfig {
    const val BASE_URL: String = "http://127.0.0.1:8081/"

    private val baseUrl = Url(BASE_URL)

    fun realtimeWebSocketUrl(boardId: String, sessionToken: String): String =
        URLBuilder().apply {
            protocol = baseUrl.protocol.toWebSocketProtocol()
            host = baseUrl.host
            port = baseUrl.port
            appendPathSegments("v1", "realtime")
            parameters.append("boardId", boardId)
            parameters.append("sessionToken", sessionToken)
        }.buildString()

    private fun URLProtocol.toWebSocketProtocol(): URLProtocol = when (name) {
        URLProtocol.HTTPS.name,
        URLProtocol.WSS.name,
        -> URLProtocol.WSS

        else -> URLProtocol.WS
    }
}
