package com.dadadadev.prototype_me.domain.models

data class RelationEdge(
    val id: String,
    val sourceNodeId: String,
    val sourceFieldId: String? = null,   // null = entity-level connection
    val targetNodeId: String,
    val targetFieldId: String? = null,   // null = entity-level connection
    val label: String? = null,           // optional user label on the edge
)
