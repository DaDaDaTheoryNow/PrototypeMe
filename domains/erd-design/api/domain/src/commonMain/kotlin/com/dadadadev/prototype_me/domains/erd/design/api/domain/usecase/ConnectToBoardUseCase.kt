package com.dadadadev.prototype_me.domains.erd.design.api.domain.usecase

/**
 * Establishes the realtime connection to the board session identified by
 * [boardId] and authenticates the current user as [currentUserId].
 *
 * Must be called before dispatching any mutation use case or collecting the
 * board-state / sync-effects flows.
 */
interface ConnectToBoardUseCase {
    suspend operator fun invoke(boardId: String, currentUserId: String)
}
