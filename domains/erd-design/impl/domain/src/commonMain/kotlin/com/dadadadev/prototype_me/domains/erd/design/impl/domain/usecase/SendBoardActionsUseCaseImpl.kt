package com.dadadadev.prototype_me.domains.erd.design.impl.domain.usecase

import com.dadadadev.prototype_me.domains.erd.design.api.data.repository.ErdBoardRepository
import com.dadadadev.prototype_me.domains.erd.design.api.domain.model.ErdBoardAction
import com.dadadadev.prototype_me.domains.erd.design.api.domain.usecase.SendBoardActionsUseCase

internal class SendBoardActionsUseCaseImpl(
    private val repository: ErdBoardRepository,
) : SendBoardActionsUseCase {
    override suspend fun invoke(actions: List<ErdBoardAction>) {
        repository.sendActions(actions)
    }
}
