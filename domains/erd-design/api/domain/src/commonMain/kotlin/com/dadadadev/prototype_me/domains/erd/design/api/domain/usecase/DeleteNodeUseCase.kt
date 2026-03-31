package com.dadadadev.prototype_me.domains.erd.design.api.domain.usecase

/**
 * Deletes a single ERD entity node by [nodeId] and syncs the removal to the
 * board server.
 *
 * The caller is responsible for removing any incident edges from local state
 * before dispatching this use case.
 */
interface DeleteNodeUseCase {
    suspend operator fun invoke(nodeId: String)
}
