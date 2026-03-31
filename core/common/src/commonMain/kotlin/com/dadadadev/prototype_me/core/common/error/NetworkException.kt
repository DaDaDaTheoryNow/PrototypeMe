package com.dadadadev.prototype_me.core.common.error

sealed class NetworkException(
    override val message: String,
    override val cause: Throwable? = null,
) : Exception(message, cause), AppError {

    data object Unauthorized : NetworkException("Unauthorized")

    data object RequestTimeout : NetworkException("Request timeout")

    data object NoInternet : NetworkException("No internet connection")

    data class HttpError(
        val statusCode: Int,
        val responseBody: String?,
    ) : NetworkException(message = "HTTP $statusCode")

    data class SerializationError(
        val rawBody: String?,
        val serializationCause: Throwable,
    ) : NetworkException(
        message = "Failed to deserialize network payload",
        cause = serializationCause,
    )

    data class Unknown(
        val unknownCause: Throwable,
    ) : NetworkException(
        message = unknownCause.message ?: "Unknown network error",
        cause = unknownCause,
    )
}
