package com.dadadadev.prototype_me.domains.board.core.api.domain.model

sealed interface BoardSyncEffect {
    data class LockRejected(
        val nodeId: String,
        val lockedBy: String,
    ) : BoardSyncEffect

    data object ConnectionLost : BoardSyncEffect
}
