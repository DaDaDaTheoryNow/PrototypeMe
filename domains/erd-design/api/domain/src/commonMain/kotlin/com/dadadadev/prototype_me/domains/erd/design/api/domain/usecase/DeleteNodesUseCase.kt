package com.dadadadev.prototype_me.domains.erd.design.api.domain.usecase

/**
 * Deletes a collection of ERD entity nodes and syncs all removals to the board
 * server in a single batch, minimising round-trips.
 */
interface DeleteNodesUseCase {
    suspend operator fun invoke(nodeIds: Collection<String>)
}
