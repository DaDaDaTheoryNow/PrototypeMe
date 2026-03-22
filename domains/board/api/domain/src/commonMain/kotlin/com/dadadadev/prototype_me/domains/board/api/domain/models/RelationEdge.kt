package com.dadadadev.prototype_me.domain.models

enum class RelationType { ONE_TO_ONE, ONE_TO_MANY, MANY_TO_MANY, INHERITS }

data class RelationEdge(
    val id: String,
    val sourceNodeId: String,
    val sourceFieldId: String? = null,   // null = entity-level connection
    val targetNodeId: String,
    val targetFieldId: String? = null,
    val type: RelationType = RelationType.ONE_TO_MANY,
)
