package com.dadadadev.prototype_me.domains.erd.design.impl.domain.usecase

import com.dadadadev.prototype_me.domains.erd.design.api.data.repository.ErdBoardRepository
import com.dadadadev.prototype_me.domains.erd.design.api.domain.model.ErdBoardAction
import com.dadadadev.prototype_me.domains.erd.design.api.domain.usecase.RemoveFieldUseCase
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

internal class RemoveFieldUseCaseImpl(
    private val repository: ErdBoardRepository,
) : RemoveFieldUseCase {

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun invoke(nodeId: String, fieldId: String) {
        repository.sendAction(
            ErdBoardAction.RemoveField(
                nodeId = nodeId,
                fieldId = fieldId,
                actionId = Uuid.random().toString(),
            ),
        )
    }
}
