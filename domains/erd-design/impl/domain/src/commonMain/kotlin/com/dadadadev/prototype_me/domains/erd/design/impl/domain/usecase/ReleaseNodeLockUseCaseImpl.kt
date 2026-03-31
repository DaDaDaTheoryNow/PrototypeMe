package com.dadadadev.prototype_me.domains.erd.design.impl.domain.usecase

import com.dadadadev.prototype_me.domains.erd.design.api.data.repository.ErdBoardRepository
import com.dadadadev.prototype_me.domains.erd.design.api.domain.usecase.ReleaseNodeLockUseCase

internal class ReleaseNodeLockUseCaseImpl(
    private val repository: ErdBoardRepository,
) : ReleaseNodeLockUseCase {
    override suspend fun invoke(nodeId: String) {
        repository.releaseLock(nodeId)
    }
}
