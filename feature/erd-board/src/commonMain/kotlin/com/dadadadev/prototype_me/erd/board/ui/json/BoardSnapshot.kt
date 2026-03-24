package com.dadadadev.prototype_me.erd.board.ui.json

import kotlinx.serialization.Serializable

/**
 * Root document for a serialized ERD board.
 * Used for JSON export / import.
 */
@Serializable
data class BoardSnapshot(
    val version: Int = CURRENT_VERSION,
    val nodes: List<NodeSnapshot>,
    val edges: List<EdgeSnapshot>,
) {
    companion object {
        const val CURRENT_VERSION = 1
    }
}

@Serializable
data class NodeSnapshot(
    val id: String,
    val name: String,
    val x: Float,
    val y: Float,
    val fields: List<FieldSnapshot> = emptyList(),
)

@Serializable
data class FieldSnapshot(
    val id: String,
    val name: String,
    /** One of: TEXT, NUMBER, BOOLEAN, DATE */
    val type: String,
)

@Serializable
data class EdgeSnapshot(
    val id: String,
    val sourceNodeId: String,
    val sourceFieldId: String? = null,
    val targetNodeId: String,
    val targetFieldId: String? = null,
    val label: String? = null,
)
