package com.dadadadev.prototype_me.domains.erd.design.impl.domain.usecase

import com.dadadadev.prototype_me.domains.erd.design.api.data.repository.ErdBoardRepository
import com.dadadadev.prototype_me.domains.erd.design.api.domain.model.ErdBoardAction
import com.dadadadev.prototype_me.domains.erd.design.api.domain.model.Position
import com.dadadadev.prototype_me.domains.erd.design.api.domain.usecase.MoveNodesUseCase
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

internal class MoveNodesUseCaseImpl(
    private val repository: ErdBoardRepository,
) : MoveNodesUseCase {

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun invoke(moves: Map<String, Position>) {
        repository.sendActions(
            moves.map { (nodeId, position) ->
                ErdBoardAction.MoveNode(
                    nodeId = nodeId,
                    newPosition = position,
                    actionId = Uuid.random().toString(),
                )
            },
        )
    }
}
