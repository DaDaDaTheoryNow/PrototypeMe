package com.dadadadev.prototype_me.erd.board.presentation.viewmodel

import com.dadadadev.prototype_me.domains.board.core.api.domain.model.BoardSyncEffect
import com.dadadadev.prototype_me.erd.board.presentation.ErdBoardViewModel
import com.dadadadev.prototype_me.erd.board.presentation.contract.ErdBoardSideEffect

internal fun ErdBoardViewModel.connectBoard() = intent {
    repository.connect(DEFAULT_BOARD_ID, DEFAULT_CURRENT_USER_ID)
}

internal fun ErdBoardViewModel.observeBoardState() = intent {
    repository.observeBoardState(DEFAULT_BOARD_ID).collect { remote ->
        val locallyMovedNodeIds = runtimeState.locallyMovedNodeIds
        val mergedNodes = mergeNodes(state.nodes, remote.nodes, locallyMovedNodeIds)
        val caughtUpNodeIds = locallyMovedNodeIds.filter { nodeId ->
            val localNode = state.nodes[nodeId]
            val remoteNode = remote.nodes[nodeId]
            localNode != null && remoteNode != null && localNode.position == remoteNode.position
        }
        if (caughtUpNodeIds.isNotEmpty()) {
            runtimeState = runtimeState.clearLocallyMoved(caughtUpNodeIds)
        }

        reduce {
            state.copy(
                nodes = mergedNodes,
                edges = remote.edges,
            )
        }
    }
}

internal fun ErdBoardViewModel.observeBoardSideEffects() = intent {
    repository.observeSideEffects().collect { effect ->
        when (effect) {
            is BoardSyncEffect.LockRejected -> {
                postSideEffect(ErdBoardSideEffect.ShowLockError(effect.nodeId, effect.lockedBy))
            }

            BoardSyncEffect.ConnectionLost -> {
                postSideEffect(ErdBoardSideEffect.ShowConnectionLost)
            }
        }
    }
}
