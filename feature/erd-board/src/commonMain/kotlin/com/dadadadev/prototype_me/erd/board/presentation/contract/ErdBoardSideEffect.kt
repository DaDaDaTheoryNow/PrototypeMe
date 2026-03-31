package com.dadadadev.prototype_me.erd.board.presentation.contract

sealed class ErdBoardSideEffect {
    /** Shown as a snackbar/toast when a lock request is rejected */
    data class ShowLockError(val nodeId: String, val lockedBy: String) : ErdBoardSideEffect()

    /** Shown when initial board load or join fails with a concrete error */
    data class ShowConnectionError(val message: String) : ErdBoardSideEffect()

    /** Shown when the transport connection is lost */
    data object ShowConnectionLost : ErdBoardSideEffect()

    /** Switches local multi-selection to the nodes created by paste */
    data class SelectPastedNodes(val nodeIds: Set<String>) : ErdBoardSideEffect()
}

