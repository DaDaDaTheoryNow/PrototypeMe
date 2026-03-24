package com.dadadadev.prototype_me.erd.board.presentation.viewmodel

import com.dadadadev.prototype_me.domains.erd.design.api.domain.model.Position
import com.dadadadev.prototype_me.erd.board.presentation.ErdBoardViewModel
import com.dadadadev.prototype_me.erd.board.presentation.contract.ErdBoardIntent
import com.dadadadev.prototype_me.erd.board.presentation.contract.ErdBoardSideEffect
import com.dadadadev.prototype_me.erd.board.presentation.viewmodel.undo.ErdUndoAction
import com.dadadadev.prototype_me.erd.board.presentation.viewmodel.undo.applyUndoAction

internal fun ErdBoardViewModel.handleGlobalIntent(boardIntent: ErdBoardIntent) = intent {
    when (boardIntent) {
        ErdBoardIntent.OnEscape -> reduce {
            state.copy(
                connectingFromNodeId = null,
                connectingFromFieldId = null,
                draggingEdgeFromNodeId = null,
                draggingEdgeFromFieldId = null,
                draggingEdgeCurrentPos = null,
                draggingEdgeSnapTargetNodeId = null,
                draggingEdgeSnapTargetFieldId = null,
                draggingEdgeSnapTargetIsRight = null,
                nodeMenuNodeId = null,
                selectedEdgeId = null,
            )
        }

        ErdBoardIntent.OnUndo -> {
            val (action, updatedRuntimeState) = runtimeState.popUndo()
            action ?: return@intent
            runtimeState = updatedRuntimeState
            reduce { state.copy(canUndo = runtimeState.canUndo) }
            applyUndoAction(action)
        }

        is ErdBoardIntent.OnCopy -> {
            val nodes = boardIntent.nodeIds.mapNotNull(state.nodes::get)
            if (nodes.isEmpty()) return@intent

            val internalEdges = state.edges.values.filter { edge ->
                edge.sourceNodeId in boardIntent.nodeIds && edge.targetNodeId in boardIntent.nodeIds
            }
            runtimeState = runtimeState.storeClipboard(ErdClipboard(nodes, internalEdges))
        }

        ErdBoardIntent.OnPaste -> {
            val clipboard = runtimeState.clipboard ?: return@intent
            val nodeIdRemap = clipboard.nodes.associate { node -> node.id to newBoardActionId() }
            val fieldIdRemap = clipboard.nodes
                .flatMap { node -> node.fields.map { field -> field.id to newBoardActionId() } }
                .toMap()

            val newNodes = clipboard.nodes.map { node ->
                node.copy(
                    id = nodeIdRemap.getValue(node.id),
                    position = Position(
                        x = node.position.x + PASTE_OFFSET,
                        y = node.position.y + PASTE_OFFSET,
                    ),
                    fields = node.fields.map { field ->
                        field.copy(id = fieldIdRemap.getValue(field.id))
                    },
                )
            }
            val newEdges = clipboard.internalEdges.map { edge ->
                edge.copy(
                    id = newBoardActionId(),
                    sourceNodeId = nodeIdRemap[edge.sourceNodeId] ?: edge.sourceNodeId,
                    sourceFieldId = edge.sourceFieldId?.let { fieldIdRemap[it] ?: it },
                    targetNodeId = nodeIdRemap[edge.targetNodeId] ?: edge.targetNodeId,
                    targetFieldId = edge.targetFieldId?.let { fieldIdRemap[it] ?: it },
                )
            }
            val newNodeIds = newNodes.map { it.id }
            val newEdgeIds = newEdges.map { it.id }

            runtimeState = runtimeState.pushUndo(ErdUndoAction.BatchAdded(newNodeIds, newEdgeIds))
            reduce {
                state.copy(
                    nodes = state.nodes + newNodes.associateBy { it.id },
                    edges = state.edges + newEdges.associateBy { it.id },
                    nodeMenuNodeId = null,
                    selectedNodeId = null,
                    selectedEdgeId = null,
                    canUndo = runtimeState.canUndo,
                )
            }
            postSideEffect(ErdBoardSideEffect.SelectPastedNodes(newNodeIds.toSet()))
            repository.sendActions(buildAddActions(newNodes, newEdges))
        }

        else -> Unit
    }
}
