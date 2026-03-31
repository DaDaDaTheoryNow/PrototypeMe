package com.dadadadev.prototype_me.feature.home.presentation

import com.dadadadev.prototype_me.core.common.error.NetworkException
import com.dadadadev.prototype_me.core.common.result.AppResult
import com.dadadadev.prototype_me.core.common.result.map
import com.dadadadev.prototype_me.core.mvi.BaseViewModel
import com.dadadadev.prototype_me.feature.home.presentation.contract.HomeIntent
import com.dadadadev.prototype_me.feature.home.presentation.contract.HomeSideEffect
import com.dadadadev.prototype_me.feature.home.presentation.contract.HomeState
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class HomeViewModel(
    private val useCases: HomeUseCases,
) : BaseViewModel<HomeState, HomeSideEffect>(HomeState()) {

    private val sessionUserId = "user_${Uuid.random().toString().take(8)}"

    init {
        bootstrapGuestAuth()
    }

    fun onIntent(homeIntent: HomeIntent) {
        when (homeIntent) {
            HomeIntent.OnCreateBoard -> createBoard()
            HomeIntent.OnOpenJoinMode -> openJoinMode()
            HomeIntent.OnCloseJoinMode -> closeJoinMode()
            is HomeIntent.OnJoinBoardIdChange -> updateJoinBoardId(homeIntent.value)
            HomeIntent.OnSubmitJoinBoard -> submitJoinBoard()
        }
    }

    private fun bootstrapGuestAuth() = intent {
        reduce { state.toAuthenticatingState() }
        when (val result = authorizeGuestIfNeeded()) {
            is AppResult.Success -> reduce { state.toAuthorizedState() }
            is AppResult.Failure -> reduce { state.toUnauthorizedState(result.error.toAuthErrorMessage()) }
        }
    }

    private fun openJoinMode() = intent {
        if (state.isBusy) return@intent
        reduce {
            state.copy(
                isJoinMode = true,
                errorMessage = null,
            )
        }
    }

    private fun closeJoinMode() = intent {
        reduce {
            state.copy(
                isJoinMode = false,
                joinBoardId = "",
                errorMessage = null,
            )
        }
    }

    private fun updateJoinBoardId(value: String) = intent {
        reduce { state.copy(joinBoardId = value) }
    }

    private fun submitJoinBoard() = intent {
        if (state.isBusy) return@intent
        val boardId = state.joinBoardId.trim()
        if (boardId.isBlank()) return@intent

        if (!state.isGuestAuthorized) {
            reduce { state.toAuthenticatingState() }
            when (val authResult = authorizeGuestIfNeeded()) {
                is AppResult.Success -> reduce { state.toAuthorizedState() }
                is AppResult.Failure -> {
                    reduce { state.toUnauthorizedState(authResult.error.toAuthErrorMessage()) }
                    return@intent
                }
            }
        }

        postSideEffect(
            HomeSideEffect.NavigateToBoard(
                boardId = boardId,
                userId = sessionUserId,
            ),
        )
        reduce {
            state.copy(
                isJoinMode = false,
                joinBoardId = "",
                errorMessage = null,
            )
        }
    }

    private fun createBoard() = intent {
        if (state.isBusy) return@intent

        if (!state.isGuestAuthorized) {
            reduce { state.toAuthenticatingState() }
            when (val authResult = authorizeGuestIfNeeded()) {
                is AppResult.Success -> reduce { state.toAuthorizedState() }
                is AppResult.Failure -> {
                    reduce { state.toUnauthorizedState(authResult.error.toAuthErrorMessage()) }
                    return@intent
                }
            }
        }

        reduce { state.copy(isCreatingBoard = true, errorMessage = null) }
        when (val result = createBoardWithAuthRetry(displayName = sessionUserId)) {
            is AppResult.Success -> {
                val approval = result.data
                if (approval.approved) {
                    postSideEffect(
                        HomeSideEffect.NavigateToBoard(
                            boardId = approval.boardId,
                            userId = sessionUserId,
                        ),
                    )
                    reduce { state.copy(isCreatingBoard = false, errorMessage = null) }
                } else {
                    reduce {
                        state.copy(
                            isCreatingBoard = false,
                            errorMessage = "Board creation was not approved by the server.",
                        )
                    }
                }
            }

            is AppResult.Failure -> {
                reduce {
                    state.copy(
                        isCreatingBoard = false,
                        errorMessage = result.error.toBoardErrorMessage(),
                    )
                }
            }
        }
    }

    private suspend fun createBoardWithAuthRetry(
        displayName: String,
    ) = when (val result = useCases.erdBoardRemoteRepository.createBoard(displayName = displayName)) {
        is AppResult.Success -> result
        is AppResult.Failure -> {
            if (result.error != NetworkException.Unauthorized) {
                result
            } else {
                when (val reauthorizeResult = reauthorizeGuest()) {
                    is AppResult.Success -> useCases.erdBoardRemoteRepository.createBoard(displayName = displayName)
                    is AppResult.Failure -> AppResult.Failure(reauthorizeResult.error)
                }
            }
        }
    }

    private suspend fun reauthorizeGuest(): AppResult<Unit, NetworkException> {
        useCases.clearAuthToken()
        return useCases.registerGuest().map { Unit }
    }

    private suspend fun authorizeGuestIfNeeded(): AppResult<Unit, NetworkException> =
        when (val savedTokenResult = useCases.getSavedAuthToken()) {
            is AppResult.Success -> {
                if (savedTokenResult.data != null) {
                    AppResult.Success(Unit)
                } else {
                    useCases.registerGuest().map { Unit }
                }
            }

            is AppResult.Failure -> useCases.registerGuest().map { Unit }
        }
}

private val HomeState.isBusy: Boolean
    get() = isCreatingBoard || isAuthenticating

private fun HomeState.toAuthenticatingState(): HomeState =
    copy(
        isAuthenticating = true,
        errorMessage = null,
    )

private fun HomeState.toAuthorizedState(): HomeState =
    copy(
        isAuthenticating = false,
        isGuestAuthorized = true,
        errorMessage = null,
    )

private fun HomeState.toUnauthorizedState(message: String): HomeState =
    copy(
        isAuthenticating = false,
        isGuestAuthorized = false,
        errorMessage = message,
    )

private fun NetworkException.toBoardErrorMessage(): String = when (this) {
    is NetworkException.HttpError -> when (statusCode) {
        404 -> "Backend endpoint was not found. Check the server route configuration."
        else -> "Server request failed (HTTP $statusCode)."
    }

    NetworkException.NoInternet -> "Cannot reach the backend server."
    NetworkException.RequestTimeout -> "The backend server did not respond in time."
    NetworkException.Unauthorized -> "The backend rejected the request."
    is NetworkException.SerializationError -> "Received an unexpected response from the backend."
    is NetworkException.Unknown -> message
}

private fun NetworkException.toAuthErrorMessage(): String = when (this) {
    is NetworkException.HttpError -> "Guest registration failed (HTTP $statusCode)."
    NetworkException.NoInternet -> "Cannot reach auth backend. Check your internet connection."
    NetworkException.RequestTimeout -> "Auth backend did not respond in time."
    NetworkException.Unauthorized -> "Auth backend rejected guest registration."
    is NetworkException.SerializationError -> "Auth backend returned unexpected response payload."
    is NetworkException.Unknown -> message
}
