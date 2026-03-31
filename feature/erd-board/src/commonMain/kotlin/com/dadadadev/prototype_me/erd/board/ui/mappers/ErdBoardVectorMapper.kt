package com.dadadadev.prototype_me.erd.board.ui.mappers

import androidx.compose.ui.geometry.Offset
import com.dadadadev.prototype_me.erd.board.presentation.contract.ErdBoardVector

internal fun Offset.toBoardVector(): ErdBoardVector =
    ErdBoardVector(x = x, y = y)

internal fun ErdBoardVector.toOffset(): Offset =
    Offset(x = x, y = y)

internal fun ErdBoardVector?.toOffset(): Offset? =
    this?.toOffset()
