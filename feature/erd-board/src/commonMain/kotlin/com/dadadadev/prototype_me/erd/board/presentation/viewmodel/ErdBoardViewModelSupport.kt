package com.dadadadev.prototype_me.erd.board.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import com.dadadadev.prototype_me.domains.erd.design.api.domain.model.EntityNode
import com.dadadadev.prototype_me.domains.erd.design.api.domain.model.ErdBoardAction
import com.dadadadev.prototype_me.domains.erd.design.api.domain.model.FieldType
import com.dadadadev.prototype_me.domains.erd.design.api.domain.model.NodeField
import com.dadadadev.prototype_me.domains.erd.design.api.domain.model.Position
import com.dadadadev.prototype_me.domains.erd.design.api.domain.model.RelationEdge
import com.dadadadev.prototype_me.erd.board.presentation.ErdBoardViewModel
import com.dadadadev.prototype_me.erd.board.presentation.viewmodel.undo.ErdUndoAction
import kotlinx.coroutines.launch
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

internal const val BOARD_MIN_SCALE = 0.2f
internal const val BOARD_MAX_SCALE = 5f
internal const val MAX_UNDO = 50
internal const val PASTE_OFFSET = 40f

internal fun ErdBoardViewModel.requestNodeLock(nodeId: String) {
    viewModelScope.launch {
        repository.requestLock(nodeId)
    }
}

internal fun ErdBoardViewModel.releaseNodeLock(nodeId: String) {
    viewModelScope.launch {
        repository.releaseLock(nodeId)
    }
}

internal fun ErdBoardViewModel.syncNodeMove(nodeId: String, position: Position) {
    viewModelScope.launch {
        repository.sendAction(moveNodeAction(nodeId, position))
    }
}

internal fun ErdBoardViewModel.syncNodeMoves(positions: Map<String, Position>) {
    if (positions.isEmpty()) return
    viewModelScope.launch {
        repository.sendActions(positions.map { (nodeId, position) -> moveNodeAction(nodeId, position) })
    }
}

internal fun ErdBoardViewModel.createEdge(
    sourceNodeId: String,
    sourceFieldId: String?,
    targetNodeId: String,
    targetFieldId: String?,
) = intent {
    val edgeId = newBoardActionId()
    val edge = RelationEdge(
        id = edgeId,
        sourceNodeId = sourceNodeId,
        sourceFieldId = sourceFieldId,
        targetNodeId = targetNodeId,
        targetFieldId = targetFieldId,
    )
    runtimeState = runtimeState.pushUndo(ErdUndoAction.EdgeAdded(edgeId))
    reduce {
        state.copy(
            edges = state.edges + (edgeId to edge),
            connectingFromNodeId = null,
            connectingFromFieldId = null,
            selectedEdgeId = edgeId,
            canUndo = runtimeState.canUndo,
        )
    }
    repository.sendAction(addEdgeAction(edge))
}

internal fun buildAddActions(
    nodes: List<EntityNode>,
    edges: List<RelationEdge>,
): List<ErdBoardAction> = buildList(nodes.size + edges.size) {
    nodes.forEach { node -> add(addNodeAction(node)) }
    edges.forEach { edge -> add(addEdgeAction(edge)) }
}

internal fun deleteNodeAction(nodeId: String): ErdBoardAction.DeleteNode =
    ErdBoardAction.DeleteNode(nodeId = nodeId, actionId = newBoardActionId())

internal fun addNodeAction(node: EntityNode): ErdBoardAction.AddNode =
    ErdBoardAction.AddNode(node = node, actionId = newBoardActionId())

internal fun addEdgeAction(edge: RelationEdge): ErdBoardAction.AddEdge =
    ErdBoardAction.AddEdge(edge = edge, actionId = newBoardActionId())

internal fun deleteEdgeAction(edgeId: String): ErdBoardAction.DeleteEdge =
    ErdBoardAction.DeleteEdge(edgeId = edgeId, actionId = newBoardActionId())

internal fun moveNodeAction(nodeId: String, position: Position): ErdBoardAction.MoveNode =
    ErdBoardAction.MoveNode(nodeId = nodeId, newPosition = position, actionId = newBoardActionId())

internal fun addFieldAction(nodeId: String, field: NodeField): ErdBoardAction.AddField =
    ErdBoardAction.AddField(nodeId = nodeId, field = field, actionId = newBoardActionId())

internal fun removeFieldAction(nodeId: String, fieldId: String): ErdBoardAction.RemoveField =
    ErdBoardAction.RemoveField(nodeId = nodeId, fieldId = fieldId, actionId = newBoardActionId())

internal fun renameFieldAction(
    nodeId: String,
    fieldId: String,
    newName: String,
    newType: FieldType,
): ErdBoardAction.RenameField =
    ErdBoardAction.RenameField(
        nodeId = nodeId,
        fieldId = fieldId,
        newName = newName,
        newType = newType,
        actionId = newBoardActionId(),
    )

internal fun mergeNodes(
    local: Map<String, EntityNode>,
    remote: Map<String, EntityNode>,
    locallyMovedNodeIds: Set<String>,
): Map<String, EntityNode> = remote.mapValues { (nodeId, remoteNode) ->
    val localNode = local[nodeId]
    if (localNode != null && nodeId in locallyMovedNodeIds) {
        remoteNode.copy(position = localNode.position)
    } else {
        remoteNode
    }
}

@OptIn(ExperimentalUuidApi::class)
internal fun newBoardActionId(): String = Uuid.random().toString()
