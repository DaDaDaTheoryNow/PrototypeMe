package com.dadadadev.prototype_me.domains.board.core.api.domain.model

interface BoardEdge {
    val id: String
    val sourceId: String
    val targetId: String
    val label: String?
}
