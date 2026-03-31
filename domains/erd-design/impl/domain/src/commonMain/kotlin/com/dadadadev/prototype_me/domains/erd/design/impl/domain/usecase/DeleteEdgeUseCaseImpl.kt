package com.dadadadev.prototype_me.domains.erd.design.impl.domain.usecase

import com.dadadadev.prototype_me.domains.erd.design.api.data.repository.ErdBoardRepository
import com.dadadadev.prototype_me.domains.erd.design.api.domain.model.ErdBoardAction
import com.dadadadev.prototype_me.domains.erd.design.api.domain.usecase.DeleteEdgeUseCase
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

internal class DeleteEdgeUseCaseImpl(
    private val repository: ErdBoardRepository,
) : DeleteEdgeUseCase {

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun invoke(edgeId: String) {
        repository.sendAction(
            ErdBoardAction.DeleteEdge(edgeId = edgeId, actionId = Uuid.random().toString()),
        )
    }
}
