package com.dadadadev.prototype_me.domains.erd.design.api.domain.usecase

/**
 * Deletes a single ERD relation edge by [edgeId] and syncs the removal to the
 * board server.
 */
interface DeleteEdgeUseCase {
    suspend operator fun invoke(edgeId: String)
}
