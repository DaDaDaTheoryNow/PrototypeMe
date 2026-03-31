package com.dadadadev.prototype_me.domains.erd.design.impl.domain.usecase

import com.dadadadev.prototype_me.domains.board.core.api.domain.model.BoardSyncEffect
import com.dadadadev.prototype_me.domains.erd.design.api.data.repository.ErdBoardRepository
import com.dadadadev.prototype_me.domains.erd.design.api.domain.usecase.ObserveSyncEffectsUseCase
import kotlinx.coroutines.flow.Flow

internal class ObserveSyncEffectsUseCaseImpl(
    private val repository: ErdBoardRepository,
) : ObserveSyncEffectsUseCase {
    override fun invoke(): Flow<BoardSyncEffect> =
        repository.observeSideEffects()
}
