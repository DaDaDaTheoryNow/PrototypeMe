package com.dadadadev.prototype_me.erd.board.presentation.viewmodel

import com.dadadadev.prototype_me.domains.erd.design.api.domain.model.EntityNode
import com.dadadadev.prototype_me.domains.erd.design.api.domain.model.RelationEdge
import com.dadadadev.prototype_me.erd.board.presentation.viewmodel.undo.ErdUndoAction

internal const val DEFAULT_BOARD_ID = "board_1"
internal const val DEFAULT_CURRENT_USER_ID = "user_1"

internal data class ErdBoardRuntimeState(
    val locallyMovedNodeIds: Set<String> = emptySet(),
    val undoStack: List<ErdUndoAction> = emptyList(),
    val clipboard: ErdClipboard? = null,
) {
    val canUndo: Boolean get() = undoStack.isNotEmpty()
}

internal data class ErdClipboard(
    val nodes: List<EntityNode>,
    val internalEdges: List<RelationEdge>,
)

internal fun ErdBoardRuntimeState.pushUndo(action: ErdUndoAction): ErdBoardRuntimeState =
    copy(undoStack = (undoStack + action).takeLast(MAX_UNDO))

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
