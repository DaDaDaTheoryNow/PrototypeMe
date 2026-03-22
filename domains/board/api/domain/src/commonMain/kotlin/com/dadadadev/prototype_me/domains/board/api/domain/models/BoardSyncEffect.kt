package com.dadadadev.prototype_me.domain.models

sealed class BoardSyncEffect {
    /** Lock request was rejected because the node is already held by another user. */
    data class LockRejected(
        val nodeId: String,
        val lockedBy: String
    ) : BoardSyncEffect()

    /** WebSocket/transport connection was lost. */
    data object ConnectionLost : BoardSyncEffect()
}
