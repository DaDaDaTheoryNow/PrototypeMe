package com.dadadadev.prototype_me.domains.erd.design.api.domain.model

import com.dadadadev.prototype_me.domains.board.core.api.domain.model.BoardEntity
import com.dadadadev.prototype_me.domains.board.core.api.domain.model.BoardPoint
import com.dadadadev.prototype_me.domains.board.core.api.domain.model.BoardSize

data class EntityNode(
    override val id: String,
    val name: String,
    override val position: BoardPoint,
    val fields: List<NodeField> = emptyList(),
    override val size: BoardSize = measureEntityNodeSize(fields),
    val lockedBy: String? = null,
) : BoardEntity

fun EntityNode.withFields(fields: List<NodeField>): EntityNode =
    copy(fields = fields, size = measureEntityNodeSize(fields))

fun measureEntityNodeSize(fields: List<NodeField>): BoardSize {
    val bodyHeight = if (fields.isNotEmpty()) {
        ENTITY_NODE_DIVIDER_HEIGHT + ENTITY_NODE_FIELD_ROW_HEIGHT * fields.size
    } else {
        0f
    }
    return BoardSize(
        width = ENTITY_NODE_WIDTH,
        height = ENTITY_NODE_HEADER_HEIGHT + bodyHeight,
    )
}

private const val ENTITY_NODE_WIDTH = 160f
private const val ENTITY_NODE_HEADER_HEIGHT = 44f
private const val ENTITY_NODE_FIELD_ROW_HEIGHT = 28f
private const val ENTITY_NODE_DIVIDER_HEIGHT = 1f
