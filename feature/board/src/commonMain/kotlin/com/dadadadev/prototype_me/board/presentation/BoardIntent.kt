package com.dadadadev.prototype_me.board.presentation

import androidx.compose.ui.geometry.Offset
import com.dadadadev.prototype_me.domain.models.FieldType
import com.dadadadev.prototype_me.domain.models.Position

sealed class BoardIntent {
    // Canvas gestures
    data class OnPanZoom(val centroid: Offset, val pan: Offset, val zoom: Float) : BoardIntent()
    /** Pan-only (no zoom). Used for LMB drag. */
    data class OnPan(val delta: Offset) : BoardIntent()

    // Node drag (move)
    data class OnDragStart(val nodeId: String) : BoardIntent()
    data class OnDragNode(val nodeId: String, val delta: Offset) : BoardIntent()
    data class OnDragEnd(val nodeId: String) : BoardIntent()

    // Add / delete node
    data class OnAddNode(val name: String, val position: Position) : BoardIntent()
    data class OnDeleteNode(val nodeId: String) : BoardIntent()

    // Tap-to-connect (tap port dot to start / finish)
    data class OnNodeFieldTap(val nodeId: String, val fieldId: String) : BoardIntent()
    data object OnCancelConnect : BoardIntent()

    // Drag-to-connect (drag from port dot to target)
    data class OnEdgeDragStart(val nodeId: String, val fieldId: String?) : BoardIntent()
    data class OnEdgeDragMove(
        val screenPos: Offset,
        val snappedTargetNodeId: String? = null,
        val snappedTargetFieldId: String? = null,
        val snappedTargetIsRight: Boolean? = null,
    ) : BoardIntent()
    /** targetNodeId = null -> cancelled or dropped on empty canvas */
    data class OnEdgeDragEnd(val targetNodeId: String?, val targetFieldId: String?) : BoardIntent()

    // Edge interaction
    data class OnSelectEdge(val edgeId: String?) : BoardIntent()
    data class OnDeleteEdge(val edgeId: String) : BoardIntent()

    // Node selection menu
    /** nodeId = null -> close menu */
    data class OnNodeMenu(val nodeId: String?) : BoardIntent()

    // Field editing (long press -> dialog)
    data class OnSelectNode(val nodeId: String?) : BoardIntent()
    data class OnAddField(val nodeId: String, val name: String, val type: FieldType) : BoardIntent()
    data class OnRemoveField(val nodeId: String, val fieldId: String) : BoardIntent()
    data class OnRenameField(
        val nodeId: String, val fieldId: String,
        val newName: String, val newType: FieldType,
    ) : BoardIntent()
}
