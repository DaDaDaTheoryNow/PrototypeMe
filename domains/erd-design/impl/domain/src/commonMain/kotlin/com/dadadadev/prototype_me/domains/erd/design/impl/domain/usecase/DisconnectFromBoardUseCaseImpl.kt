package com.dadadadev.prototype_me.domains.erd.design.impl.domain.usecase

import com.dadadadev.prototype_me.domains.erd.design.api.data.repository.ErdBoardRepository
import com.dadadadev.prototype_me.domains.erd.design.api.domain.usecase.DisconnectFromBoardUseCase

internal class DisconnectFromBoardUseCaseImpl(
    private val repository: ErdBoardRepository,
) : DisconnectFromBoardUseCase {
    override suspend fun invoke() {
        repository.disconnect()
    }
}
