package com.dadadadev.prototype_me.erd.board.presentation.viewmodel

import com.dadadadev.prototype_me.domains.erd.design.api.domain.model.EntityNode
import com.dadadadev.prototype_me.domains.erd.design.api.domain.model.FieldType
import com.dadadadev.prototype_me.domains.erd.design.api.domain.model.NodeField
import com.dadadadev.prototype_me.erd.board.presentation.ErdBoardViewModel
import com.dadadadev.prototype_me.erd.board.presentation.contract.ErdBoardIntent
import com.dadadadev.prototype_me.erd.board.presentation.viewmodel.undo.ErdUndoAction

internal fun ErdBoardViewModel.handleNodeIntent(boardIntent: ErdBoardIntent) = intent {
    when (boardIntent) {
        is ErdBoardIntent.OnAddNode -> {
            val nodeId = newBoardActionId()
            val node = EntityNode(
                id = nodeId,
                name = boardIntent.name.ifBlank { "Node" },
                position = boardIntent.position,
                fields = listOf(NodeField(newBoardActionId(), "id", FieldType.NUMBER)),
            )
            runtimeState = runtimeState.pushUndo(ErdUndoAction.NodeAdded(nodeId))
            reduce {
                state.copy(
                    nodes = state.nodes + (nodeId to node),
                    canUndo = runtimeState.canUndo,
                )
            }
            repository.sendAction(addNodeAction(node))
        }

        is ErdBoardIntent.OnDeleteNode -> {
            val nodeId = boardIntent.nodeId
            val node = state.nodes[nodeId] ?: return@intent
            val incidentEdges = state.edges.values.filter { edge ->
                edge.sourceNodeId == nodeId || edge.targetNodeId == nodeId
            }
            val remainingEdges = state.edges.filter { (_, edge) ->
                edge.sourceNodeId != nodeId && edge.targetNodeId != nodeId
            }
            runtimeState = runtimeState
                .pushUndo(ErdUndoAction.NodeDeleted(node, incidentEdges))
                .clearLocallyMoved(listOf(nodeId))
            reduce {
                state.copy(
                    nodes = state.nodes - nodeId,
                    edges = remainingEdges,
                    nodeMenuNodeId = null,
                    selectedEdgeId = state.selectedEdgeId.takeIf(remainingEdges::containsKey),
                    canUndo = runtimeState.canUndo,
                )
            }
            repository.sendAction(deleteNodeAction(nodeId))
        }

        is ErdBoardIntent.OnDeleteNodes -> {
            val nodeIds = boardIntent.nodeIds
            val deletedNodes = nodeIds.mapNotNull(state.nodes::get)
            if (deletedNodes.isEmpty()) return@intent
            val deletedEdges = state.edges.values.filter { edge ->
                edge.sourceNodeId in nodeIds || edge.targetNodeId in nodeIds
            }
            val remainingEdges = state.edges.filter { (_, edge) ->
                edge.sourceNodeId !in nodeIds && edge.targetNodeId !in nodeIds
            }
            runtimeState = runtimeState
                .pushUndo(ErdUndoAction.NodesDeleted(deletedNodes, deletedEdges))
                .clearLocallyMoved(nodeIds)
            reduce {
                state.copy(
                    nodes = state.nodes - nodeIds,
                    edges = remainingEdges,
                    nodeMenuNodeId = null,
                    selectedEdgeId = state.selectedEdgeId.takeIf(remainingEdges::containsKey),
                    canUndo = runtimeState.canUndo,
                )
            }
            repository.sendActions(nodeIds.map(::deleteNodeAction))
        }

        else -> Unit
    }
}
