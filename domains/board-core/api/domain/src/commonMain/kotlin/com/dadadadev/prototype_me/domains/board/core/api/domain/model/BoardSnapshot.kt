package com.dadadadev.prototype_me.domains.board.core.api.domain.model

data class BoardSnapshot<TNode : BoardEntity, TEdge : BoardEdge>(
    val entities: Map<String, TNode> = emptyMap(),
    val edges: Map<String, TEdge> = emptyMap(),
)
