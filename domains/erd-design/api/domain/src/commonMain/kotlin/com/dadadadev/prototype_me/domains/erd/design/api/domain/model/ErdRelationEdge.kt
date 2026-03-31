package com.dadadadev.prototype_me.domains.erd.design.api.domain.model

import com.dadadadev.prototype_me.domains.board.core.api.domain.model.BoardEdge

data class ErdRelationEdge(
    override val id: String,
    val sourceNodeId: String,
    val sourceFieldId: String? = null,
    val targetNodeId: String,
    val targetFieldId: String? = null,
    override val label: String? = null,
) : BoardEdge {
    override val sourceId: String = sourceNodeId
    override val targetId: String = targetNodeId
}

