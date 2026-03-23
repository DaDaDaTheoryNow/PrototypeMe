package com.dadadadev.prototype_me.domains.erd.design.api.domain.model

import com.dadadadev.prototype_me.domains.board.core.api.domain.model.BoardEntity
import com.dadadadev.prototype_me.domains.board.core.api.domain.model.BoardPoint
import com.dadadadev.prototype_me.domains.board.core.api.domain.model.BoardSize

enum class ErdNodeType {
    TABLE,
    VIEW,
    MATERIALIZED_VIEW,
}

data class ErdColumn(
    val id: String,
    val name: String,
    val dataType: String,
    val nullable: Boolean = false,
    val isPrimaryKey: Boolean = false,
)

data class ErdNode(
    override val id: String,
    override val position: BoardPoint,
    override val size: BoardSize,
    val tableName: String,
    val schemaName: String? = null,
    val nodeType: ErdNodeType = ErdNodeType.TABLE,
    val columns: List<ErdColumn> = emptyList(),
) : BoardEntity
