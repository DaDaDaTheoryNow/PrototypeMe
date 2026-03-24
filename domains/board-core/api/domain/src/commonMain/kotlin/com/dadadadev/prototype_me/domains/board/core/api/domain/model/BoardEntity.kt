package com.dadadadev.prototype_me.domains.board.core.api.domain.model

interface BoardEntity {
    val id: String
    val position: BoardPoint
    val size: BoardSize
}
