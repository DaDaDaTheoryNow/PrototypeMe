package com.dadadadev.prototype_me.domains.board.core.impl.domain.usecase

class ClampBoardScaleUseCase(
    private val min: Float = 0.25f,
    private val max: Float = 4f,
) {
    operator fun invoke(scale: Float): Float = scale.coerceIn(min, max)
}
