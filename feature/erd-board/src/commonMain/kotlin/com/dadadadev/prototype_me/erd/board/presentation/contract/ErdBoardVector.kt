package com.dadadadev.prototype_me.erd.board.presentation.contract

/**
 * Platform-agnostic 2D vector used by the ERD board presentation contract.
 *
 * Keeps Presentation and MVI contracts independent from Compose-specific
 * geometry primitives.
 */
data class ErdBoardVector(
    val x: Float,
    val y: Float,
) {
    operator fun plus(other: ErdBoardVector): ErdBoardVector =
        ErdBoardVector(x = x + other.x, y = y + other.y)

    companion object {
        val Zero: ErdBoardVector = ErdBoardVector(0f, 0f)
    }
}
