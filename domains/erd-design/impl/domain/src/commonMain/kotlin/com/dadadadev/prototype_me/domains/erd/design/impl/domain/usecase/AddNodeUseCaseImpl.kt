package com.dadadadev.prototype_me.domains.erd.design.impl.domain.usecase

import com.dadadadev.prototype_me.domains.erd.design.api.data.repository.ErdBoardRepository
import com.dadadadev.prototype_me.domains.erd.design.api.domain.model.ErdBoardAction
import com.dadadadev.prototype_me.domains.erd.design.api.domain.model.ErdEntityNode
import com.dadadadev.prototype_me.domains.erd.design.api.domain.model.ErdNodeField
import com.dadadadev.prototype_me.domains.erd.design.api.domain.model.Position
import com.dadadadev.prototype_me.domains.erd.design.api.domain.usecase.AddNodeUseCase
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

internal class AddNodeUseCaseImpl(
    private val repository: ErdBoardRepository,
) : AddNodeUseCase {

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun invoke(
        name: String,
        position: Position,
        initialFields: List<ErdNodeField>,
    ): ErdEntityNode {
        val node = ErdEntityNode(
            id = Uuid.random().toString(),
            name = name,
            position = position,
            fields = initialFields,
        )
        repository.sendAction(
            ErdBoardAction.AddNode(node = node, actionId = Uuid.random().toString()),
        )
        return node
    }
}
