package com.dadadadev.prototype_me.domains.erd.design.api.domain.usecase

import com.dadadadev.prototype_me.domains.erd.design.api.domain.model.Position

/**
 * Syncs new canvas positions for multiple nodes in a single batch —
 * used for multi-drag and undo of multi-move operations.
 *
 * [moves] maps each nodeId to its target [Position].
 */
interface MoveNodesUseCase {
    suspend operator fun invoke(moves: Map<String, Position>)
}
