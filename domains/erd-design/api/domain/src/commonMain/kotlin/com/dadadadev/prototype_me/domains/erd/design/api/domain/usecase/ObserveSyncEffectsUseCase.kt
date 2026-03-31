package com.dadadadev.prototype_me.domains.erd.design.api.domain.usecase

import com.dadadadev.prototype_me.domains.board.core.api.domain.model.BoardSyncEffect
import kotlinx.coroutines.flow.Flow

/**
 * Emits [BoardSyncEffect] events such as lock-rejection notices and
 * connection-lost notifications.
 *
 * Consumers should translate these into [ErdBoardSideEffect]s at the
 * Presentation layer.
 */
interface ObserveSyncEffectsUseCase {
    operator fun invoke(): Flow<BoardSyncEffect>
}
