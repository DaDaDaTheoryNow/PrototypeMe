package com.dadadadev.prototype_me.erd.board.presentation.contract

import androidx.compose.ui.geometry.Offset
import com.dadadadev.prototype_me.domains.erd.design.api.domain.model.EntityNode
import com.dadadadev.prototype_me.domains.erd.design.api.domain.model.RelationEdge

/**
 * Public UI contract for the ERD board screen.
 *
 * Keep only values that the composable tree actually renders from.
 * Internal editor mechanics such as undo history, clipboard and local sync
 * tracking live inside the ViewModel runtime state.
 */
data class ErdBoardState(
    val nodes: Map<String, EntityNode> = emptyMap(),
    val edges: Map<String, RelationEdge> = emptyMap(),
    val panOffset: Offset = Offset.Zero,
    val scale: Float = 1f,
    val connectingFromNodeId: String? = null,
    val connectingFromFieldId: String? = null,
    val draggingEdgeFromNodeId: String? = null,
    val draggingEdgeFromFieldId: String? = null,
    val draggingEdgeCurrentPos: Offset? = null,
    val draggingEdgeSnapTargetNodeId: String? = null,
    val draggingEdgeSnapTargetFieldId: String? = null,
    val draggingEdgeSnapTargetIsRight: Boolean? = null,
    val selectedEdgeId: String? = null,
    val nodeMenuNodeId: String? = null,
    val selectedNodeId: String? = null,
    val canUndo: Boolean = false,
)
