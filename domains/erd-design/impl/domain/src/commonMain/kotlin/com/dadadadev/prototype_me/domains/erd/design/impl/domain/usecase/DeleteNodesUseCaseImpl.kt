package com.dadadadev.prototype_me.domains.erd.design.impl.domain.usecase

import com.dadadadev.prototype_me.domains.erd.design.api.data.repository.ErdBoardRepository
import com.dadadadev.prototype_me.domains.erd.design.api.domain.model.ErdBoardAction
import com.dadadadev.prototype_me.domains.erd.design.api.domain.usecase.DeleteNodesUseCase
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

internal class DeleteNodesUseCaseImpl(
    private val repository: ErdBoardRepository,
) : DeleteNodesUseCase {

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun invoke(nodeIds: Collection<String>) {
        repository.sendActions(
            nodeIds.map { nodeId ->
                ErdBoardAction.DeleteNode(nodeId = nodeId, actionId = Uuid.random().toString())
            },
        )
    }
}
