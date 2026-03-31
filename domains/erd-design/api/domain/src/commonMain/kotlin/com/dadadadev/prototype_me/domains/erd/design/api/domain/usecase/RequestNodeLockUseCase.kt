package com.dadadadev.prototype_me.domains.erd.design.api.domain.usecase

/**
 * Requests an exclusive edit lock on the given node from the board server.
 *
 * The server may reject the request (e.g., if another user already holds the lock);
 * rejection is reported as a [BoardSyncEffect.LockRejected] side-effect observed
 * via [ObserveSyncEffectsUseCase].
 */
interface RequestNodeLockUseCase {
    suspend operator fun invoke(nodeId: String)
}
