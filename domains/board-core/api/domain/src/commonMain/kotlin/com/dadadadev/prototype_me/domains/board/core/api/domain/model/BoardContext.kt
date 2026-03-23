package com.dadadadev.prototype_me.domains.board.core.api.domain.model

data class BoardContext<TNode : BoardEntity, TEdge : BoardEdge>(
    val boardId: String,
    val nodes: Map<String, TNode>,
    val edges: Map<String, TEdge>,
)

fun <TNode : BoardEntity, TEdge : BoardEdge> BoardContext<TNode, TEdge>.toBoardSnapshot(): BoardSnapshot<TNode> =
    BoardSnapshot(
        entities = nodes,
        edges = edges,
    )
