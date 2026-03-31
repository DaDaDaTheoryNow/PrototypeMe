package com.dadadadev.prototype_me.domains.erd.design.api.domain.model

import com.dadadadev.prototype_me.domains.board.core.api.domain.model.BoardEntity
import com.dadadadev.prototype_me.domains.board.core.api.domain.model.BoardPoint
import com.dadadadev.prototype_me.domains.board.core.api.domain.model.BoardSize

data class ErdEntityNode(
    override val id: String,
    val name: String,
    override val position: BoardPoint,
    val fields: List<ErdNodeField> = emptyList(),
    override val size: BoardSize = BoardSize(0f, 0f),
    val lockedBy: String? = null,
) : BoardEntity
