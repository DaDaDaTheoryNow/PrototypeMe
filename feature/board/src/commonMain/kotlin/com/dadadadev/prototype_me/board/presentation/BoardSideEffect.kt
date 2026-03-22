package com.dadadadev.prototype_me.board.presentation

sealed class BoardSideEffect {
    /** Shown as a snackbar/toast when a lock request is rejected */
    data class ShowLockError(val nodeId: String, val lockedBy: String) : BoardSideEffect()

    /** Shown when the transport connection is lost */
    data object ShowConnectionLost : BoardSideEffect()
}
