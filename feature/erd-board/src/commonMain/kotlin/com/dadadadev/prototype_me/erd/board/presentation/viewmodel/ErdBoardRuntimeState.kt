package com.dadadadev.prototype_me.erd.board.presentation.viewmodel

import com.dadadadev.prototype_me.domains.erd.design.api.domain.model.ErdEntityNode
import com.dadadadev.prototype_me.domains.erd.design.api.domain.model.ErdRelationEdge
import com.dadadadev.prototype_me.erd.board.presentation.viewmodel.undo.ErdUndoAction
import com.dadadadev.prototype_me.erd.board.config.ErdBoardConfig
import kotlin.time.TimeMark

internal data class ErdBoardRuntimeState(
    val locallyMovedNodeIds: Set<String> = emptySet(),
    val undoStack: List<ErdUndoAction> = emptyList(),
    val clipboard: ErdClipboard? = null,
    val lastDragPreviewNodeId: String? = null,
    val lastDragPreviewSentAt: TimeMark? = null,
) {
    val canUndo: Boolean get() = undoStack.isNotEmpty()
}

internal data class ErdClipboard(
    val nodes: List<ErdEntityNode>,
    val internalEdges: List<ErdRelationEdge>,
)

internal fun ErdBoardRuntimeState.pushUndo(action: ErdUndoAction): ErdBoardRuntimeState =
    copy(undoStack = (undoStack + action).takeLast(ErdBoardConfig.MAX_UNDO_STEPS))

internal fun ErdBoardRuntimeState.popUndo(): Pair<ErdUndoAction?, ErdBoardRuntimeState> {
    val action = undoStack.lastOrNull()
    return if (action == null) {
        null to this
    } else {
        action to copy(undoStack = undoStack.dropLast(1))
    }
}

internal fun ErdBoardRuntimeState.storeClipboard(clipboard: ErdClipboard): ErdBoardRuntimeState =
    copy(clipboard = clipboard)

internal fun ErdBoardRuntimeState.markLocallyMoved(nodeId: String): ErdBoardRuntimeState =
    copy(locallyMovedNodeIds = locallyMovedNodeIds + nodeId)

internal fun ErdBoardRuntimeState.markLocallyMoved(nodeIds: Collection<String>): ErdBoardRuntimeState =
    copy(locallyMovedNodeIds = locallyMovedNodeIds + nodeIds)

internal fun ErdBoardRuntimeState.clearLocallyMoved(nodeIds: Collection<String>): ErdBoardRuntimeState =
    copy(locallyMovedNodeIds = locallyMovedNodeIds - nodeIds.toSet())

internal fun ErdBoardRuntimeState.markDragPreviewSent(nodeId: String, sentAt: TimeMark): ErdBoardRuntimeState =
    copy(lastDragPreviewNodeId = nodeId, lastDragPreviewSentAt = sentAt)

internal fun ErdBoardRuntimeState.clearDragPreviewThrottle(): ErdBoardRuntimeState =
    copy(lastDragPreviewNodeId = null, lastDragPreviewSentAt = null)
