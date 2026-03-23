package com.dadadadev.prototype_me.domains.board.core.api.domain.model

data class BoardPoint(
    val x: Float,
    val y: Float,
)

data class BoardSize(
    val width: Float,
    val height: Float,
)

interface BoardEntity {
    val id: String
    val position: BoardPoint
    val size: BoardSize
}

interface BoardEdge {
    val id: String
    val sourceId: String
    val targetId: String
    val label: String?
}

data class DefaultBoardEdge(
    override val id: String,
    override val sourceId: String,
    override val targetId: String,
    override val label: String? = null,
) : BoardEdge

data class BoardSnapshot<T : BoardEntity>(
    val entities: Map<String, T> = emptyMap(),
    val edges: Map<String, BoardEdge> = emptyMap(),
)
