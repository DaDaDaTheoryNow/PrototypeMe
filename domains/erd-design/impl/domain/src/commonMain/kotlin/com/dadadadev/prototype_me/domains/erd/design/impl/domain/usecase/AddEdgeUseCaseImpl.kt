package com.dadadadev.prototype_me.domains.erd.design.impl.domain.usecase

import com.dadadadev.prototype_me.domains.erd.design.api.data.repository.ErdBoardRepository
import com.dadadadev.prototype_me.domains.erd.design.api.domain.model.ErdBoardAction
import com.dadadadev.prototype_me.domains.erd.design.api.domain.model.ErdRelationEdge
import com.dadadadev.prototype_me.domains.erd.design.api.domain.usecase.AddEdgeUseCase
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

internal class AddEdgeUseCaseImpl(
    private val repository: ErdBoardRepository,
) : AddEdgeUseCase {

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun invoke(
        sourceNodeId: String,
        sourceFieldId: String?,
        targetNodeId: String,
        targetFieldId: String?,
    ): ErdRelationEdge {
        val edge = ErdRelationEdge(
            id = Uuid.random().toString(),
            sourceNodeId = sourceNodeId,
            sourceFieldId = sourceFieldId,
            targetNodeId = targetNodeId,
            targetFieldId = targetFieldId,
        )
        repository.sendAction(
            ErdBoardAction.AddEdge(edge = edge, actionId = Uuid.random().toString()),
        )
        return edge
    }
}
