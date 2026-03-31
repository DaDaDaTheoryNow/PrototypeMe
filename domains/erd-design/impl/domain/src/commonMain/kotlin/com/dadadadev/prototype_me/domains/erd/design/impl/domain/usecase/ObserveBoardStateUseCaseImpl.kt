package com.dadadadev.prototype_me.domains.erd.design.impl.domain.usecase

import com.dadadadev.prototype_me.domains.erd.design.api.data.repository.ErdBoardRepository
import com.dadadadev.prototype_me.domains.erd.design.api.domain.model.ErdBoardContext
import com.dadadadev.prototype_me.domains.erd.design.api.domain.usecase.ObserveBoardStateUseCase
import kotlinx.coroutines.flow.Flow

internal class ObserveBoardStateUseCaseImpl(
    private val repository: ErdBoardRepository,
) : ObserveBoardStateUseCase {
    override fun invoke(boardId: String): Flow<ErdBoardContext> =
        repository.observeBoardState(boardId)
}
