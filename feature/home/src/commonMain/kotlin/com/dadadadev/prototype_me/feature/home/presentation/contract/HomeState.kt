package com.dadadadev.prototype_me.feature.home.presentation.contract

data class HomeState(
    val isAuthenticating: Boolean = true,
    val isGuestAuthorized: Boolean = false,
    val isCreatingBoard: Boolean = false,
    val isJoinMode: Boolean = false,
    val joinBoardId: String = "",
    val errorMessage: String? = null,
)
