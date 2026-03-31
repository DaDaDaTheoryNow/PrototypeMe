package com.dadadadev.prototype_me.domains.erd.design.impl.domain.usecase

import com.dadadadev.prototype_me.domains.erd.design.api.data.repository.ErdBoardRepository
import com.dadadadev.prototype_me.domains.erd.design.api.domain.model.Position
import com.dadadadev.prototype_me.domains.erd.design.api.domain.usecase.SendNodeDragUpdateUseCase

internal class SendNodeDragUpdateUseCaseImpl(
    private val repository: ErdBoardRepository,
) : SendNodeDragUpdateUseCase {
    override suspend fun invoke(nodeId: String, position: Position) {
        repository.sendNodeDragUpdate(nodeId, position)
    }
}
