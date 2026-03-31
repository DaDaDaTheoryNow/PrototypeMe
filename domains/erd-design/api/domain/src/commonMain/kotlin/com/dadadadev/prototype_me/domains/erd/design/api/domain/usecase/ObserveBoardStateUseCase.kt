package com.dadadadev.prototype_me.domains.erd.design.api.domain.usecase

import com.dadadadev.prototype_me.domains.erd.design.api.domain.model.ErdBoardContext
import kotlinx.coroutines.flow.Flow

/**
 * Emits a new [ErdBoardContext] snapshot every time the board state changes
 * (node moved, edge added, field updated, etc.).
 *
 * The flow is cold — each collector starts from the current server state.
 * Must be collected after [ConnectToBoardUseCase] has been invoked.
 */
interface ObserveBoardStateUseCase {
    operator fun invoke(boardId: String): Flow<ErdBoardContext>
}
