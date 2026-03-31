package com.dadadadev.prototype_me.domains.erd.design.api.domain.usecase

import com.dadadadev.prototype_me.domains.erd.design.api.domain.model.Position

/**
 * Syncs the new canvas [position] of a single node to the board server.
 *
 * The caller is expected to update local state optimistically before invoking
 * this use case.
 */
interface MoveNodeUseCase {
    suspend operator fun invoke(nodeId: String, position: Position)
}
