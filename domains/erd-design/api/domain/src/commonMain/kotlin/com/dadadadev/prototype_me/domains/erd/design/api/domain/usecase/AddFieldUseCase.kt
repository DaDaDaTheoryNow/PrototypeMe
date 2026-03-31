package com.dadadadev.prototype_me.domains.erd.design.api.domain.usecase

import com.dadadadev.prototype_me.domains.erd.design.api.domain.model.ErdNodeField
import com.dadadadev.prototype_me.domains.erd.design.api.domain.model.FieldType

/**
 * Adds a new field to an ERD entity node and syncs the change to the board server.
 *
 * Returns the fully constructed [ErdNodeField] — including its generated ID —
 * so the caller can apply an optimistic UI update immediately.
 */
interface AddFieldUseCase {
    suspend operator fun invoke(
        nodeId: String,
        name: String,
        type: FieldType,
    ): ErdNodeField
}
