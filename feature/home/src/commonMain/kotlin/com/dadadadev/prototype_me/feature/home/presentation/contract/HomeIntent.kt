package com.dadadadev.prototype_me.feature.home.presentation.contract

sealed interface HomeIntent {
    data object OnCreateBoard : HomeIntent
    data object OnOpenJoinMode : HomeIntent
    data object OnCloseJoinMode : HomeIntent
    data class OnJoinBoardIdChange(val value: String) : HomeIntent
    data object OnSubmitJoinBoard : HomeIntent
}
