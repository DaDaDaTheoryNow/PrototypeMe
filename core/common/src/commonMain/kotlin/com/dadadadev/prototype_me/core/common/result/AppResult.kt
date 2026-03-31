package com.dadadadev.prototype_me.core.common.result

sealed interface AppResult<out D, out E> {
    data class Success<D>(val data: D) : AppResult<D, Nothing>
    data class Failure<E>(val error: E) : AppResult<Nothing, E>
}

typealias EmptyResult<E> = AppResult<Unit, E>

inline fun <T, E, R> AppResult<T, E>.map(transform: (T) -> R): AppResult<R, E> =
    when (this) {
        is AppResult.Success -> AppResult.Success(transform(data))
        is AppResult.Failure -> this
    }

inline fun <T, E, R> AppResult<T, E>.mapError(transform: (E) -> R): AppResult<T, R> =
    when (this) {
        is AppResult.Success -> this
        is AppResult.Failure -> AppResult.Failure(transform(error))
    }

inline fun <T, E> AppResult<T, E>.onSuccess(action: (T) -> Unit): AppResult<T, E> =
    apply {
        if (this is AppResult.Success) {
            action(data)
        }
    }

inline fun <T, E> AppResult<T, E>.onFailure(action: (E) -> Unit): AppResult<T, E> =
    apply {
        if (this is AppResult.Failure) {
            action(error)
        }
    }

inline fun <T, E, R> AppResult<T, E>.fold(
    onSuccess: (T) -> R,
    onFailure: (E) -> R,
): R = when (this) {
    is AppResult.Success -> onSuccess(data)
    is AppResult.Failure -> onFailure(error)
}
