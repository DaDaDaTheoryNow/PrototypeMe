package com.dadadadev.prototype_me.erd.board.presentation.viewmodel

import com.dadadadev.prototype_me.domains.board.core.api.domain.model.BoardSyncEffect
import com.dadadadev.prototype_me.erd.board.layout.remeasured
import com.dadadadev.prototype_me.erd.board.presentation.ErdBoardViewModel
import com.dadadadev.prototype_me.erd.board.presentation.contract.ErdBoardSideEffect

internal fun ErdBoardViewModel.connectBoard() = intent {
    useCases.connectToBoard(boardSession.boardId, boardSession.currentUserId)
}

internal fun ErdBoardViewModel.observeBoardState() = intent {
    useCases.observeBoardState(boardSession.boardId).collect { remote ->
        val remoteNodes = remote.nodes.mapValues { (_, node) -> node.remeasured() }
        val locallyMovedNodeIds = runtimeState.locallyMovedNodeIds
        val mergedNodes = mergeNodes(state.nodes, remoteNodes, locallyMovedNodeIds)
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
    useCases.observeSyncEffects().collect { effect ->
        when (effect) {
            is BoardSyncEffect.LockRejected -> {
                postSideEffect(ErdBoardSideEffect.ShowLockError(effect.nodeId, effect.lockedBy))
            }

            is BoardSyncEffect.ConnectionFailed -> {
                postSideEffect(ErdBoardSideEffect.ShowConnectionError(effect.message))
            }

            BoardSyncEffect.ConnectionLost -> {
                postSideEffect(ErdBoardSideEffect.ShowConnectionLost)
            }
        }
    }
}
