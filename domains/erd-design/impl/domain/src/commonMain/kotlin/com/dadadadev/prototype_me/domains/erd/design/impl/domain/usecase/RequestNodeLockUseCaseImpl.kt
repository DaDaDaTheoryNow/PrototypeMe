package com.dadadadev.prototype_me.domains.erd.design.impl.domain.usecase

import com.dadadadev.prototype_me.domains.erd.design.api.data.repository.ErdBoardRepository
import com.dadadadev.prototype_me.domains.erd.design.api.domain.usecase.RequestNodeLockUseCase

internal class RequestNodeLockUseCaseImpl(
    private val repository: ErdBoardRepository,
) : RequestNodeLockUseCase {
    override suspend fun invoke(nodeId: String) {
        repository.requestLock(nodeId)
    }
}
