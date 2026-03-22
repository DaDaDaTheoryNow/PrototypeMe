package com.dadadadev.prototype_me.board.presentation

import androidx.compose.ui.geometry.Offset
import com.dadadadev.prototype_me.domain.models.FieldType
import com.dadadadev.prototype_me.domain.models.Position
import com.dadadadev.prototype_me.domain.models.RelationType

sealed class BoardIntent {
    // ── Canvas gestures ───────────────────────────────────────────────────────
    data class OnPanZoom(val centroid: Offset, val pan: Offset, val zoom: Float) : BoardIntent()

    // ── Node drag ─────────────────────────────────────────────────────────────
    data class OnDragStart(val nodeId: String) : BoardIntent()
    data class OnDragNode(val nodeId: String, val delta: Offset) : BoardIntent()
    data class OnDragEnd(val nodeId: String) : BoardIntent()

    // ── Add node ──────────────────────────────────────────────────────────────
    data class OnAddNode(val name: String, val position: Position) : BoardIntent()

    // ── Connect: tap a port dot to start/finish a connection ──────────────────
    /** Tap the header port dot → entity-level connection */
    data class OnNodeTap(val nodeId: String) : BoardIntent()

    /** Tap a field port dot → field-level connection */
    data class OnNodeFieldTap(val nodeId: String, val fieldId: String) : BoardIntent()

    /** Cancel the current pending connection (e.g. tapped canvas background) */
    data object OnCancelConnect : BoardIntent()

    // ── Edge interaction ──────────────────────────────────────────────────────
    data class OnSelectEdge(val edgeId: String?) : BoardIntent()
    data class OnDeleteEdge(val edgeId: String) : BoardIntent()
    data class OnChangeEdgeType(val edgeId: String, val type: RelationType) : BoardIntent()

    // ── Field editing ─────────────────────────────────────────────────────────
    data class OnSelectNode(val nodeId: String?) : BoardIntent()
    data class OnAddField(val nodeId: String, val name: String, val type: FieldType) : BoardIntent()
    data class OnRemoveField(val nodeId: String, val fieldId: String) : BoardIntent()
    data class OnRenameField(
        val nodeId: String, val fieldId: String,
        val newName: String, val newType: FieldType,
    ) : BoardIntent()
}
