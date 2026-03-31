package com.dadadadev.prototype_me.domains.erd.design.api.domain.usecase

/**
 * Removes a field from an ERD entity node and syncs the removal to the board server.
 *
 * The caller is responsible for updating local node state before or after invoking
 * this use case.
 */
interface RemoveFieldUseCase {
    suspend operator fun invoke(nodeId: String, fieldId: String)
}
