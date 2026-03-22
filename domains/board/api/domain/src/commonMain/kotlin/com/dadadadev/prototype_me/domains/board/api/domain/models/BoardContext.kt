package com.dadadadev.prototype_me.domain.models

data class BoardContext(
    val boardId: String,
    val nodes: Map<String, EntityNode>,
    val edges: Map<String, RelationEdge>
)
