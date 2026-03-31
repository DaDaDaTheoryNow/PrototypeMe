package com.dadadadev.prototype_me.domains.erd.design.impl.domain.usecase

import com.dadadadev.prototype_me.domains.erd.design.api.data.repository.ErdBoardRepository
import com.dadadadev.prototype_me.domains.erd.design.api.domain.model.ErdBoardAction
import com.dadadadev.prototype_me.domains.erd.design.api.domain.model.Position
import com.dadadadev.prototype_me.domains.erd.design.api.domain.usecase.MoveNodeUseCase
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

internal class MoveNodeUseCaseImpl(
    private val repository: ErdBoardRepository,
) : MoveNodeUseCase {

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun invoke(nodeId: String, position: Position) {
        repository.sendAction(
            ErdBoardAction.MoveNode(
                nodeId = nodeId,
                newPosition = position,
                actionId = Uuid.random().toString(),
            ),
        )
    }
}
