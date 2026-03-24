package com.dadadadev.prototype_me.erd.board.presentation.viewmodel

import androidx.compose.ui.geometry.Offset
import com.dadadadev.prototype_me.domains.erd.design.api.domain.model.Position
import com.dadadadev.prototype_me.erd.board.presentation.ErdBoardViewModel
import com.dadadadev.prototype_me.erd.board.presentation.contract.ErdBoardIntent
import com.dadadadev.prototype_me.erd.board.presentation.viewmodel.undo.ErdUndoAction

internal fun ErdBoardViewModel.handleCanvasIntent(boardIntent: ErdBoardIntent) = intent {
    when (boardIntent) {
        is ErdBoardIntent.OnPanZoom -> reduce {
            val newScale = (state.scale * boardIntent.zoom).coerceIn(BOARD_MIN_SCALE, BOARD_MAX_SCALE)
            val factor = newScale / state.scale
            state.copy(
                scale = newScale,
                panOffset = Offset(
                    x = boardIntent.centroid.x - factor * (boardIntent.centroid.x - state.panOffset.x) + boardIntent.pan.x,
                    y = boardIntent.centroid.y - factor * (boardIntent.centroid.y - state.panOffset.y) + boardIntent.pan.y,
                ),
            )
        }

        is ErdBoardIntent.OnPan -> reduce {
            state.copy(panOffset = state.panOffset + boardIntent.delta)
        }

        is ErdBoardIntent.OnSetViewTransform -> reduce {
            state.copy(
                scale = boardIntent.scale.coerceIn(BOARD_MIN_SCALE, BOARD_MAX_SCALE),
                panOffset = boardIntent.panOffset,
            )
        }

        is ErdBoardIntent.OnDragStart -> {
            val node = state.nodes[boardIntent.nodeId] ?: return@intent
            dragOrigins[boardIntent.nodeId] = node.position
            runtimeState = runtimeState.markLocallyMoved(boardIntent.nodeId)
            requestNodeLock(boardIntent.nodeId)
        }

        is ErdBoardIntent.OnDragNode -> {
            val node = state.nodes[boardIntent.nodeId] ?: return@intent
            val newPosition = Position(
                x = node.position.x + boardIntent.delta.x,
                y = node.position.y + boardIntent.delta.y,
            )
            reduce { state.copy(nodes = state.nodes + (boardIntent.nodeId to node.copy(position = newPosition))) }
        }

        is ErdBoardIntent.OnDragEnd -> {
            val nodeId = boardIntent.nodeId
            val origin = dragOrigins.remove(nodeId)
            val currentPosition = state.nodes[nodeId]?.position
            if (origin != null && currentPosition != null && origin != currentPosition) {
                runtimeState = runtimeState.pushUndo(ErdUndoAction.NodeMoved(nodeId, origin))
                reduce { state.copy(canUndo = runtimeState.canUndo) }
                syncNodeMove(nodeId, currentPosition)
            }
            releaseNodeLock(nodeId)
        }

        is ErdBoardIntent.OnMultiDragEnd -> {
            val movedPairs = boardIntent.nodeIds.mapNotNull { nodeId ->
                val origin = dragOrigins.remove(nodeId) ?: return@mapNotNull null
                val currentPosition = state.nodes[nodeId]?.position ?: return@mapNotNull null
                if (origin == currentPosition) null else nodeId to origin
            }
            if (movedPairs.isNotEmpty()) {
                runtimeState = runtimeState.pushUndo(ErdUndoAction.NodesMoved(movedPairs.toMap()))
                reduce { state.copy(canUndo = runtimeState.canUndo) }
                syncNodeMoves(
                    movedPairs.mapNotNull { (nodeId, _) ->
                        state.nodes[nodeId]?.position?.let { position -> nodeId to position }
                    }.toMap(),
                )
            }
            boardIntent.nodeIds.forEach(::releaseNodeLock)
        }

        else -> Unit
    }
}
