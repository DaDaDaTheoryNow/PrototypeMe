package com.dadadadev.prototype_me.erd.board.presentation.contract

import androidx.compose.ui.geometry.Offset
import com.dadadadev.prototype_me.domains.board.core.api.domain.model.BoardSnapshot
import com.dadadadev.prototype_me.domains.erd.design.api.domain.model.EntityNode
import com.dadadadev.prototype_me.domains.erd.design.api.domain.model.FieldType
import com.dadadadev.prototype_me.domains.erd.design.api.domain.model.Position
import com.dadadadev.prototype_me.domains.erd.design.api.domain.model.RelationEdge

sealed class ErdBoardIntent {
    // Canvas gestures
    data class OnPanZoom(val centroid: Offset, val pan: Offset, val zoom: Float) : ErdBoardIntent()
    data class OnPan(val delta: Offset) : ErdBoardIntent()
    data class OnSetViewTransform(val scale: Float, val panOffset: Offset) : ErdBoardIntent()

    // Node drag (move in board-space units)
    data class OnDragStart(val nodeId: String) : ErdBoardIntent()
    data class OnDragNode(val nodeId: String, val delta: Offset) : ErdBoardIntent()
    data class OnDragEnd(val nodeId: String) : ErdBoardIntent()

    // Add / delete node
    data class OnAddNode(val name: String, val position: Position) : ErdBoardIntent()
    data class OnDeleteNode(val nodeId: String) : ErdBoardIntent()
    data class OnDeleteNodes(val nodeIds: Set<String>) : ErdBoardIntent()

    // Tap-to-connect (tap port dot to start / finish)
    data class OnNodeFieldTap(val nodeId: String, val fieldId: String) : ErdBoardIntent()
    data object OnCancelConnect : ErdBoardIntent()

    // Drag-to-connect (drag from port dot to target)
    data class OnEdgeDragStart(val nodeId: String, val fieldId: String?) : ErdBoardIntent()
    data class OnEdgeDragMove(
        val screenPos: Offset,
        val snappedTargetNodeId: String? = null,
        val snappedTargetFieldId: String? = null,
        val snappedTargetIsRight: Boolean? = null,
    ) : ErdBoardIntent()
    data class OnEdgeDragEnd(val targetNodeId: String?, val targetFieldId: String?) : ErdBoardIntent()

    // Edge interaction
    data class OnSelectEdge(val edgeId: String?) : ErdBoardIntent()
    data class OnDeleteEdge(val edgeId: String) : ErdBoardIntent()

    // Node selection menu
    data class OnNodeMenu(val nodeId: String?) : ErdBoardIntent()

    // Field editing (long press -> dialog)
    data class OnSelectNode(val nodeId: String?) : ErdBoardIntent()
    data class OnAddField(val nodeId: String, val name: String, val type: FieldType) : ErdBoardIntent()
    data class OnRemoveField(val nodeId: String, val fieldId: String) : ErdBoardIntent()
    data class OnRenameField(
        val nodeId: String,
        val fieldId: String,
        val newName: String,
        val newType: FieldType,
    ) : ErdBoardIntent()

    // Keyboard / global actions
    data object OnEscape : ErdBoardIntent()
    data object OnUndo : ErdBoardIntent()
    data class OnCopy(val nodeIds: Set<String>) : ErdBoardIntent()
    data object OnPaste : ErdBoardIntent()

    /** Ends a multi-node drag and records it as one undoable operation. */
    data class OnMultiDragEnd(val nodeIds: Set<String>) : ErdBoardIntent()

    // JSON import: replaces the entire board with the provided nodes and edges.
    data class OnImportBoard(
        val snapshot: BoardSnapshot<EntityNode, RelationEdge>,
    ) : ErdBoardIntent()
}
