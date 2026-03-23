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

    // Tap-to-connect (port dot tap)
    val connectingFromNodeId: String? = null,
    val connectingFromFieldId: String? = null,

    // Drag-to-connect (port dot drag)
    val draggingEdgeFromNodeId: String? = null,
    val draggingEdgeFromFieldId: String? = null,
    val draggingEdgeCurrentPos: Offset? = null,
    val draggingEdgeSnapTargetNodeId: String? = null,
    val draggingEdgeSnapTargetFieldId: String? = null,
    val draggingEdgeSnapTargetIsRight: Boolean? = null,

    // Selection
    val selectedEdgeId: String? = null,
    val nodeMenuNodeId: String? = null,   // floating action menu (tap to select)
    val selectedNodeId: String? = null,   // field editor dialog (long press)

    // Locally authoritative nodes
    // Nodes in this set were recently dragged by us. mergeNodes always keeps
    // local position for them until the server catches up.
    val locallyMovedNodeIds: Set<String> = emptySet(),
)
