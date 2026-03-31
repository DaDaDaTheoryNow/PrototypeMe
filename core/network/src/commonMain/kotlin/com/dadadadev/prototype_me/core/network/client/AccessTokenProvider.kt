package com.dadadadev.prototype_me.core.network.client

interface AccessTokenProvider {
    suspend fun getAccessToken(): String?
}
