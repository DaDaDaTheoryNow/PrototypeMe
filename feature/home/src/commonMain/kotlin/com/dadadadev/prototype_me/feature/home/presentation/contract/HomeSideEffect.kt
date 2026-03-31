package com.dadadadev.prototype_me.feature.home.presentation.contract

sealed interface HomeSideEffect {
    data class NavigateToBoard(
        val boardId: String,
        val userId: String,
    ) : HomeSideEffect
}
