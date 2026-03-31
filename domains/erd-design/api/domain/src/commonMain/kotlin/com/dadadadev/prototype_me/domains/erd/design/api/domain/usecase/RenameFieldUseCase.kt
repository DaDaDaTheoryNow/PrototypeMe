package com.dadadadev.prototype_me.domains.erd.design.api.domain.usecase

import com.dadadadev.prototype_me.domains.erd.design.api.domain.model.FieldType

/**
 * Updates the name and/or type of an existing field within an ERD entity node
 * and syncs the change to the board server.
 */
interface RenameFieldUseCase {
    suspend operator fun invoke(
        nodeId: String,
        fieldId: String,
        newName: String,
        newType: FieldType,
    )
}
