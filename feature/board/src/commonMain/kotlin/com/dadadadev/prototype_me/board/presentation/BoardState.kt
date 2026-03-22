package com.dadadadev.prototype_me.board.presentation

import androidx.compose.ui.geometry.Offset
import com.dadadadev.prototype_me.domain.models.EntityNode
import com.dadadadev.prototype_me.domain.models.RelationEdge

data class BoardState(
    val nodes: Map<String, EntityNode> = emptyMap(),
    val edges: Map<String, RelationEdge> = emptyMap(),
    val panOffset: Offset = Offset.Zero,
    val scale: Float = 1f,
    val currentUserId: String = "user_1",

    // ── Connection in progress (null = idle) ──────────────────────────────────
    val connectingFromNodeId: String? = null,
    val connectingFromFieldId: String? = null,   // null = entity-level

    // ── Selection ─────────────────────────────────────────────────────────────
    val selectedNodeId: String? = null,          // field editor open
    val selectedEdgeId: String? = null,          // edge toolbar open
)
