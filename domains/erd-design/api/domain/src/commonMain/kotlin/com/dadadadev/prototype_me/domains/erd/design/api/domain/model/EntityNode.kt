package com.dadadadev.prototype_me.domains.erd.design.api.domain.model

import com.dadadadev.prototype_me.domains.board.core.api.domain.model.BoardEntity
import com.dadadadev.prototype_me.domains.board.core.api.domain.model.BoardPoint
import com.dadadadev.prototype_me.domains.board.core.api.domain.model.BoardSize

data class EntityNode(
    override val id: String,
    val name: String,
    override val position: BoardPoint,
    override val size: BoardSize = DEFAULT_ENTITY_NODE_SIZE,
    val fields: List<NodeField> = emptyList(),
    /** Non-null when another user is currently dragging this node. */
    val lockedBy: String? = null,
) : BoardEntity {
    private companion object {
        val DEFAULT_ENTITY_NODE_SIZE = BoardSize(width = 240f, height = 160f)
    }
}

