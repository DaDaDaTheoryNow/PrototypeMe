package com.dadadadev.prototype_me.domains.erd.design.impl.domain.usecase

import com.dadadadev.prototype_me.domains.erd.design.api.data.repository.ErdBoardRepository
import com.dadadadev.prototype_me.domains.erd.design.api.domain.model.ErdBoardAction
import com.dadadadev.prototype_me.domains.erd.design.api.domain.model.ErdNodeField
import com.dadadadev.prototype_me.domains.erd.design.api.domain.model.FieldType
import com.dadadadev.prototype_me.domains.erd.design.api.domain.usecase.AddFieldUseCase
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

internal class AddFieldUseCaseImpl(
    private val repository: ErdBoardRepository,
) : AddFieldUseCase {

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun invoke(nodeId: String, name: String, type: FieldType): ErdNodeField {
        val field = ErdNodeField(id = Uuid.random().toString(), name = name, type = type)
        repository.sendAction(
            ErdBoardAction.AddField(
                nodeId = nodeId,
                field = field,
                actionId = Uuid.random().toString(),
            ),
        )
        return field
    }
}
