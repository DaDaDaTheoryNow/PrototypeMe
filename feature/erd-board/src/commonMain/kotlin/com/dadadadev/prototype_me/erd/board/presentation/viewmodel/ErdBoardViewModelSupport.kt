package com.dadadadev.prototype_me.erd.board.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import com.dadadadev.prototype_me.domains.erd.design.api.domain.model.ErdBoardAction
import com.dadadadev.prototype_me.domains.erd.design.api.domain.model.ErdEntityNode
import com.dadadadev.prototype_me.domains.erd.design.api.domain.model.ErdNodeField
import com.dadadadev.prototype_me.domains.erd.design.api.domain.model.ErdRelationEdge
import com.dadadadev.prototype_me.domains.erd.design.api.domain.model.FieldType
import com.dadadadev.prototype_me.domains.erd.design.api.domain.model.Position
import com.dadadadev.prototype_me.erd.board.presentation.ErdBoardViewModel
import com.dadadadev.prototype_me.erd.board.presentation.viewmodel.undo.ErdUndoAction
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TimeSource
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

// ── Non-suspend helpers (fire-and-forget from outside an intent block) ─────────

internal fun ErdBoardViewModel.requestNodeLock(nodeId: String) {
    viewModelScope.launch {
        useCases.requestNodeLock(nodeId)
    }
}

internal fun ErdBoardViewModel.releaseNodeLock(nodeId: String) {
    viewModelScope.launch {
        useCases.releaseNodeLock(nodeId)
    }
}

internal fun ErdBoardViewModel.syncNodeMove(nodeId: String, position: Position) {
    viewModelScope.launch {
        useCases.moveNode(nodeId, position)
    }
}

internal fun ErdBoardViewModel.syncNodeMoves(positions: Map<String, Position>) {
    if (positions.isEmpty()) return
    viewModelScope.launch {
        useCases.moveNodes(positions)
    }
}

internal fun ErdBoardViewModel.sendNodeDragPreview(nodeId: String, position: Position) {
    if (nodeId !in dragOrigins) return

    val lastSentAt = runtimeState.lastDragPreviewSentAt
    val sameNode = runtimeState.lastDragPreviewNodeId == nodeId
    if (sameNode && lastSentAt != null && lastSentAt.elapsedNow() < 40.milliseconds) {
        return
    }

    runtimeState = runtimeState.markDragPreviewSent(nodeId, TimeSource.Monotonic.markNow())
    viewModelScope.launch {
        useCases.sendNodeDragUpdate(nodeId, position)
    }
}

internal fun ErdBoardViewModel.commitNodeMoveAndReleaseLock(nodeId: String, position: Position) {
    viewModelScope.launch {
        useCases.moveNode(nodeId, position)
        useCases.releaseNodeLock(nodeId)
    }
}

internal fun ErdBoardViewModel.commitNodeMovesAndReleaseLocks(positions: Map<String, Position>) {
    if (positions.isEmpty()) return
    viewModelScope.launch {
        useCases.moveNodes(positions)
        positions.keys.forEach { nodeId ->
            useCases.releaseNodeLock(nodeId)
        }
    }
}

// ── Edge creation (runs inside an intent block) ───────────────────────────────

internal fun ErdBoardViewModel.createEdge(
    sourceNodeId: String,
    sourceFieldId: String?,
    targetNodeId: String,
    targetFieldId: String?,
) = intent {
    val edge = useCases.addEdge(sourceNodeId, sourceFieldId, targetNodeId, targetFieldId)
    runtimeState = runtimeState.pushUndo(ErdUndoAction.EdgeAdded(edge.id))
    reduce {
        state.copy(
            edges = state.edges + (edge.id to edge),
            connectingFromNodeId = null,
            connectingFromFieldId = null,
            selectedEdgeId = edge.id,
            canUndo = runtimeState.canUndo,
        )
    }
}

// ── Raw ErdBoardAction builders (used by undo flows via sendBoardActions) ──────

internal fun buildAddActions(
    nodes: List<ErdEntityNode>,
    edges: List<ErdRelationEdge>,
): List<ErdBoardAction> = buildList(nodes.size + edges.size) {
    nodes.forEach { node ->
        add(ErdBoardAction.AddNode(node = node, actionId = newBoardActionId()))
    }
    edges.forEach { edge ->
        add(ErdBoardAction.AddEdge(edge = edge, actionId = newBoardActionId()))
    }
}

internal fun deleteNodeAction(nodeId: String): ErdBoardAction.DeleteNode =
    ErdBoardAction.DeleteNode(nodeId = nodeId, actionId = newBoardActionId())

internal fun addNodeAction(node: ErdEntityNode): ErdBoardAction.AddNode =
    ErdBoardAction.AddNode(node = node, actionId = newBoardActionId())

internal fun addEdgeAction(edge: ErdRelationEdge): ErdBoardAction.AddEdge =
    ErdBoardAction.AddEdge(edge = edge, actionId = newBoardActionId())

internal fun deleteEdgeAction(edgeId: String): ErdBoardAction.DeleteEdge =
    ErdBoardAction.DeleteEdge(edgeId = edgeId, actionId = newBoardActionId())

internal fun addFieldAction(nodeId: String, field: ErdNodeField): ErdBoardAction.AddField =
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

// ── State-merge helpers ────────────────────────────────────────────────────────

internal fun mergeNodes(
    local: Map<String, ErdEntityNode>,
    remote: Map<String, ErdEntityNode>,
    locallyMovedNodeIds: Set<String>,
): Map<String, ErdEntityNode> = remote.mapValues { (nodeId, remoteNode) ->
    val localNode = local[nodeId]
    if (localNode != null && nodeId in locallyMovedNodeIds) {
        remoteNode.copy(position = localNode.position)
    } else {
        remoteNode
    }
}

@OptIn(ExperimentalUuidApi::class)
internal fun newBoardActionId(): String = Uuid.random().toString()
