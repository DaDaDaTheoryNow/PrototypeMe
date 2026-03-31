package com.dadadadev.prototype_me.domains.auth.impl.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RegisterGuestResponseDto(
    val status: Int? = null,
    val message: String? = null,
    val data: RegisterGuestTokenDto,
)

@Serializable
data class RegisterGuestTokenDto(
    @SerialName("access_token")
    val accessToken: String,
    @SerialName("token_type")
    val tokenType: String,
)
