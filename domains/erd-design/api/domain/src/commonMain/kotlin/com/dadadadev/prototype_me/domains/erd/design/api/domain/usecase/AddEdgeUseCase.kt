package com.dadadadev.prototype_me.domains.erd.design.api.domain.usecase

import com.dadadadev.prototype_me.domains.erd.design.api.domain.model.ErdRelationEdge

/**
 * Creates a new relation edge between two ERD entity nodes and syncs it to the
 * board server.
 *
 * Returns the fully constructed [ErdRelationEdge] — including its generated ID —
 * so the caller can apply an optimistic UI update immediately.
 *
 * [sourceFieldId] and [targetFieldId] are optional port anchors; pass `null` for
 * a node-level (non-field-specific) connection.
 */
interface AddEdgeUseCase {
    suspend operator fun invoke(
        sourceNodeId: String,
        sourceFieldId: String?,
        targetNodeId: String,
        targetFieldId: String?,
    ): ErdRelationEdge
}
