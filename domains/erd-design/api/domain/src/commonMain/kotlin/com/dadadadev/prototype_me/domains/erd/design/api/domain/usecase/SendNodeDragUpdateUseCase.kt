package com.dadadadev.prototype_me.domains.erd.design.api.domain.usecase

import com.dadadadev.prototype_me.domains.erd.design.api.domain.model.Position

interface SendNodeDragUpdateUseCase {
    suspend operator fun invoke(nodeId: String, position: Position)
}
