package com.dadadadev.prototype_me.domains.erd.design.impl.domain.usecase

import com.dadadadev.prototype_me.domains.erd.design.api.data.repository.ErdBoardRepository
import com.dadadadev.prototype_me.domains.erd.design.api.domain.model.ErdBoardAction
import com.dadadadev.prototype_me.domains.erd.design.api.domain.model.FieldType
import com.dadadadev.prototype_me.domains.erd.design.api.domain.usecase.RenameFieldUseCase
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

internal class RenameFieldUseCaseImpl(
    private val repository: ErdBoardRepository,
) : RenameFieldUseCase {

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun invoke(nodeId: String, fieldId: String, newName: String, newType: FieldType) {
        repository.sendAction(
            ErdBoardAction.RenameField(
                nodeId = nodeId,
                fieldId = fieldId,
                newName = newName,
                newType = newType,
                actionId = Uuid.random().toString(),
            ),
        )
    }
}
