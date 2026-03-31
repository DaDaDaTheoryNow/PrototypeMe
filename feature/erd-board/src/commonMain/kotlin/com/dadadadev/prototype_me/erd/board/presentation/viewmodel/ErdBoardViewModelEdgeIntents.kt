package com.dadadadev.prototype_me.erd.board.presentation.viewmodel

import com.dadadadev.prototype_me.erd.board.presentation.ErdBoardViewModel
import com.dadadadev.prototype_me.erd.board.presentation.contract.ErdBoardIntent
import com.dadadadev.prototype_me.erd.board.presentation.viewmodel.undo.ErdUndoAction

internal fun ErdBoardViewModel.handleEdgeIntent(boardIntent: ErdBoardIntent) = intent {
    when (boardIntent) {
        is ErdBoardIntent.OnNodeFieldTap -> {
            val nodeId = boardIntent.nodeId
            val fieldId = boardIntent.fieldId
            when (val sourceNodeId = state.connectingFromNodeId) {
                null -> reduce {
                    state.copy(
                        connectingFromNodeId = nodeId,
                        connectingFromFieldId = fieldId,
                        nodeMenuNodeId = null,
                    )
                }

                nodeId -> reduce {
                    state.copy(connectingFromNodeId = null, connectingFromFieldId = null)
                }

                else -> createEdge(sourceNodeId, state.connectingFromFieldId, nodeId, fieldId)
            }
        }

        ErdBoardIntent.OnCancelConnect -> reduce {
            state.copy(connectingFromNodeId = null, connectingFromFieldId = null)
        }

        is ErdBoardIntent.OnEdgeDragStart -> reduce {
            state.copy(
                draggingEdgeFromNodeId = boardIntent.nodeId,
                draggingEdgeFromFieldId = boardIntent.fieldId,
                draggingEdgeCurrentPos = null,
                draggingEdgeSnapTargetNodeId = null,
                draggingEdgeSnapTargetFieldId = null,
                draggingEdgeSnapTargetIsRight = null,
                connectingFromNodeId = null,
                connectingFromFieldId = null,
                nodeMenuNodeId = null,
            )
        }

        is ErdBoardIntent.OnEdgeDragMove -> reduce {
            state.copy(
                draggingEdgeCurrentPos = boardIntent.screenPos,
                draggingEdgeSnapTargetNodeId = boardIntent.snappedTargetNodeId,
                draggingEdgeSnapTargetFieldId = boardIntent.snappedTargetFieldId,
                draggingEdgeSnapTargetIsRight = boardIntent.snappedTargetIsRight,
            )
        }

        is ErdBoardIntent.OnEdgeDragEnd -> {
            val fromNodeId = state.draggingEdgeFromNodeId
            val toNodeId = boardIntent.targetNodeId
            if (fromNodeId != null && toNodeId != null && fromNodeId != toNodeId) {
                createEdge(fromNodeId, state.draggingEdgeFromFieldId, toNodeId, boardIntent.targetFieldId)
            }
            reduce {
                state.copy(
                    draggingEdgeFromNodeId = null,
                    draggingEdgeFromFieldId = null,
                    draggingEdgeCurrentPos = null,
                    draggingEdgeSnapTargetNodeId = null,
                    draggingEdgeSnapTargetFieldId = null,
                    draggingEdgeSnapTargetIsRight = null,
                )
            }
        }

        is ErdBoardIntent.OnSelectEdge -> reduce {
            state.copy(selectedEdgeId = boardIntent.edgeId, nodeMenuNodeId = null)
        }

        is ErdBoardIntent.OnDeleteEdge -> {
            val edge = state.edges[boardIntent.edgeId] ?: return@intent
            runtimeState = runtimeState.pushUndo(ErdUndoAction.EdgeDeleted(edge))
            reduce {
                state.copy(
                    edges = state.edges - boardIntent.edgeId,
                    selectedEdgeId = null,
                    canUndo = runtimeState.canUndo,
                )
            }
            useCases.deleteEdge(boardIntent.edgeId)
        }

        is ErdBoardIntent.OnNodeMenu -> reduce {
            state.copy(nodeMenuNodeId = boardIntent.nodeId, selectedEdgeId = null)
        }

        else -> Unit
    }
}
