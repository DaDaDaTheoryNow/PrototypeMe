package com.dadadadev.prototype_me.domains.erd.design.impl.domain.usecase

import com.dadadadev.prototype_me.domains.erd.design.api.data.repository.ErdBoardRepository
import com.dadadadev.prototype_me.domains.erd.design.api.domain.model.ErdBoardAction
import com.dadadadev.prototype_me.domains.erd.design.api.domain.usecase.DeleteNodeUseCase
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

internal class DeleteNodeUseCaseImpl(
    private val repository: ErdBoardRepository,
) : DeleteNodeUseCase {

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun invoke(nodeId: String) {
        repository.sendAction(
            ErdBoardAction.DeleteNode(nodeId = nodeId, actionId = Uuid.random().toString()),
        )
    }
}
