package com.dadadadev.prototype_me.domain.models

data class EntityNode(
    val id: String,
    val name: String,
    val position: Position,
    val fields: List<NodeField> = emptyList(),
    /** Non-null when another user is currently dragging this node. */
    val lockedBy: String? = null
)
