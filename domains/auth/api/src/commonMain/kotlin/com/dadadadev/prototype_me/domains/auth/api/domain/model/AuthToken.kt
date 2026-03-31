package com.dadadadev.prototype_me.domains.auth.api.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class AuthToken(
    val accessToken: String,
    val tokenType: String,
)
