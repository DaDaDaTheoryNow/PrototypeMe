package com.dadadadev.prototype_me.erd.board.presentation.viewmodel.undo

import com.dadadadev.prototype_me.domains.erd.design.api.domain.model.withFields
import com.dadadadev.prototype_me.erd.board.presentation.ErdBoardViewModel
import com.dadadadev.prototype_me.erd.board.presentation.viewmodel.addEdgeAction
import com.dadadadev.prototype_me.erd.board.presentation.viewmodel.addFieldAction
import com.dadadadev.prototype_me.erd.board.presentation.viewmodel.buildAddActions
import com.dadadadev.prototype_me.erd.board.presentation.viewmodel.clearLocallyMoved
import com.dadadadev.prototype_me.erd.board.presentation.viewmodel.deleteEdgeAction
import com.dadadadev.prototype_me.erd.board.presentation.viewmodel.deleteNodeAction
import com.dadadadev.prototype_me.erd.board.presentation.viewmodel.markLocallyMoved
import com.dadadadev.prototype_me.erd.board.presentation.viewmodel.syncNodeMove
import com.dadadadev.prototype_me.erd.board.presentation.viewmodel.syncNodeMoves
import com.dadadadev.prototype_me.erd.board.presentation.viewmodel.removeFieldAction
import com.dadadadev.prototype_me.erd.board.presentation.viewmodel.renameFieldAction

internal fun ErdBoardViewModel.applyUndoAction(action: ErdUndoAction) = intent {
    when (action) {
        is ErdUndoAction.NodeAdded -> {
            val remainingEdges = state.edges.filter { (_, edge) ->
                edge.sourceNodeId != action.nodeId && edge.targetNodeId != action.nodeId
            }
            runtimeState = runtimeState.clearLocallyMoved(listOf(action.nodeId))
            reduce {
                state.copy(
                    nodes = state.nodes - action.nodeId,
                    edges = remainingEdges,
                    nodeMenuNodeId = null,
                    selectedEdgeId = state.selectedEdgeId.takeIf(remainingEdges::containsKey),
                )
            }
            repository.sendAction(deleteNodeAction(action.nodeId))
        }

        is ErdUndoAction.NodeDeleted -> {
            reduce {
                state.copy(
                    nodes = state.nodes + (action.node.id to action.node),
                    edges = state.edges + action.edges.associateBy { it.id },
                )
            }
            repository.sendActions(buildAddActions(listOf(action.node), action.edges))
        }

        is ErdUndoAction.NodeMoved -> {
            val node = state.nodes[action.nodeId] ?: return@intent
            val restoredNode = node.copy(position = action.previousPosition)
            runtimeState = runtimeState.markLocallyMoved(action.nodeId)
            reduce {
                state.copy(
                    nodes = state.nodes + (action.nodeId to restoredNode),
                )
            }
            syncNodeMove(action.nodeId, action.previousPosition)
        }

        is ErdUndoAction.NodesMoved -> {
            val restoredNodes = action.previousPositions.entries.fold(state.nodes) { acc, (nodeId, previousPosition) ->
                val node = acc[nodeId] ?: return@fold acc
                acc + (nodeId to node.copy(position = previousPosition))
            }
            runtimeState = runtimeState.markLocallyMoved(action.previousPositions.keys)
            reduce {
                state.copy(
                    nodes = restoredNodes,
                )
            }
            syncNodeMoves(action.previousPositions)
        }

        is ErdUndoAction.BatchAdded -> {
            val nodeIds = action.nodeIds.toSet()
            val remainingEdges = state.edges.filter { (edgeId, _) -> edgeId !in action.edgeIds }
            runtimeState = runtimeState.clearLocallyMoved(nodeIds)
            reduce {
                state.copy(
                    nodes = state.nodes - nodeIds,
                    edges = remainingEdges,
                    nodeMenuNodeId = null,
                    selectedEdgeId = state.selectedEdgeId.takeIf(remainingEdges::containsKey),
                )
            }
            repository.sendActions(action.nodeIds.map(::deleteNodeAction))
        }

        is ErdUndoAction.NodesDeleted -> {
            reduce {
                state.copy(
                    nodes = state.nodes + action.nodes.associateBy { it.id },
                    edges = state.edges + action.edges.associateBy { it.id },
                )
            }
            repository.sendActions(buildAddActions(action.nodes, action.edges))
        }

        is ErdUndoAction.EdgeAdded -> {
            reduce {
                state.copy(
                    edges = state.edges - action.edgeId,
                    selectedEdgeId = if (state.selectedEdgeId == action.edgeId) null else state.selectedEdgeId,
                )
            }
            repository.sendAction(deleteEdgeAction(action.edgeId))
        }

        is ErdUndoAction.EdgeDeleted -> {
            reduce { state.copy(edges = state.edges + (action.edge.id to action.edge)) }
            repository.sendAction(addEdgeAction(action.edge))
        }

        is ErdUndoAction.FieldAdded -> {
            val node = state.nodes[action.nodeId] ?: return@intent
            val updatedNode = node.withFields(node.fields.filter { it.id != action.fieldId })
            reduce { state.copy(nodes = state.nodes + (action.nodeId to updatedNode)) }
            repository.sendAction(removeFieldAction(action.nodeId, action.fieldId))
        }

        is ErdUndoAction.FieldRemoved -> {
            val node = state.nodes[action.nodeId] ?: return@intent
            val updatedNode = node.withFields(node.fields + action.field)
            reduce { state.copy(nodes = state.nodes + (action.nodeId to updatedNode)) }
            repository.sendAction(addFieldAction(action.nodeId, action.field))
        }

        is ErdUndoAction.FieldRenamed -> {
            val node = state.nodes[action.nodeId] ?: return@intent
            val updatedFields = node.fields.map { field ->
                if (field.id == action.fieldId) {
                    field.copy(name = action.previousName, type = action.previousType)
                } else {
                    field
                }
            }
            reduce { state.copy(nodes = state.nodes + (action.nodeId to node.withFields(updatedFields))) }
            repository.sendAction(
                renameFieldAction(
                    nodeId = action.nodeId,
                    fieldId = action.fieldId,
                    newName = action.previousName,
                    newType = action.previousType,
                ),
            )
        }
    }
}
