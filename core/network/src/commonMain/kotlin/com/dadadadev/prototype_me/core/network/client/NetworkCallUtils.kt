package com.dadadadev.prototype_me.core.network.client

import com.dadadadev.prototype_me.core.common.error.NetworkException
import com.dadadadev.prototype_me.core.common.result.AppResult
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.ResponseException
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.JsonConvertException
import io.ktor.util.network.UnresolvedAddressException
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerializationException

suspend inline fun <T> safeNetworkCall(
    crossinline block: suspend () -> T,
): AppResult<T, NetworkException> =
    try {
        AppResult.Success(block())
    } catch (exception: CancellationException) {
        throw exception
    } catch (exception: NetworkException) {
        AppResult.Failure(exception)
    } catch (exception: HttpRequestTimeoutException) {
        AppResult.Failure(NetworkException.RequestTimeout)
    } catch (exception: UnresolvedAddressException) {
        AppResult.Failure(NetworkException.NoInternet)
    } catch (exception: JsonConvertException) {
        AppResult.Failure(NetworkException.SerializationError(rawBody = null, serializationCause = exception))
    } catch (exception: SerializationException) {
        AppResult.Failure(NetworkException.SerializationError(rawBody = null, serializationCause = exception))
    } catch (exception: ResponseException) {
        val statusCode = exception.response.status.value
        val body = runCatching { exception.response.bodyAsText() }.getOrNull()

        if (statusCode == HttpStatusCode.Unauthorized.value) {
            AppResult.Failure(NetworkException.Unauthorized)
        } else {
            AppResult.Failure(NetworkException.HttpError(statusCode = statusCode, responseBody = body))
        }
    } catch (exception: Throwable) {
        AppResult.Failure(NetworkException.Unknown(exception))
    }

suspend inline fun <reified T> HttpResponse.decodeBodyOrThrow(): T =
    try {
        body()
    } catch (exception: Throwable) {
        val raw = runCatching { bodyAsText() }.getOrNull()
        throw NetworkException.SerializationError(rawBody = raw, serializationCause = exception)
    }
