package com.dadadadev.prototype_me.domains.erd.design.impl.domain.usecase

import com.dadadadev.prototype_me.domains.erd.design.api.data.repository.ErdBoardRepository
import com.dadadadev.prototype_me.domains.erd.design.api.domain.usecase.ConnectToBoardUseCase

internal class ConnectToBoardUseCaseImpl(
    private val repository: ErdBoardRepository,
) : ConnectToBoardUseCase {
    override suspend fun invoke(boardId: String, currentUserId: String) {
        repository.connect(boardId, currentUserId)
    }
}
