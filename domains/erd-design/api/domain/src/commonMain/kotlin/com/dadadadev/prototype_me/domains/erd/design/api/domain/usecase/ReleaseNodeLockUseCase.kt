package com.dadadadev.prototype_me.domains.erd.design.api.domain.usecase

/**
 * Releases the current user's edit lock on the given node and notifies the board server.
 */
interface ReleaseNodeLockUseCase {
    suspend operator fun invoke(nodeId: String)
}
