package com.dadadadev.prototype_me.domains.erd.design.api.domain.usecase

import com.dadadadev.prototype_me.domains.erd.design.api.domain.model.ErdEntityNode
import com.dadadadev.prototype_me.domains.erd.design.api.domain.model.ErdNodeField
import com.dadadadev.prototype_me.domains.erd.design.api.domain.model.Position

/**
 * Creates a new ERD entity node and syncs it to the board server.
 *
 * Returns the fully constructed [ErdEntityNode] — including its generated ID —
 * so the caller can apply an optimistic UI update immediately.
 */
interface AddNodeUseCase {
    suspend operator fun invoke(
        name: String,
        position: Position,
        initialFields: List<ErdNodeField> = emptyList(),
    ): ErdEntityNode
}
