package com.dadadadev.prototype_me.domains.erd.design.impl.domain.usecase

import com.dadadadev.prototype_me.domains.board.core.api.domain.model.BoardSnapshot
import com.dadadadev.prototype_me.domains.erd.design.api.data.codec.ErdBoardJsonCodec
import com.dadadadev.prototype_me.domains.erd.design.api.domain.model.ErdEntityNode
import com.dadadadev.prototype_me.domains.erd.design.api.domain.model.ErdRelationEdge
import com.dadadadev.prototype_me.domains.erd.design.api.domain.usecase.ExportBoardJsonUseCase

internal class ExportBoardJsonUseCaseImpl(
    private val codec: ErdBoardJsonCodec,
) : ExportBoardJsonUseCase {
    override fun invoke(snapshot: BoardSnapshot<ErdEntityNode, ErdRelationEdge>): String =
        codec.encode(snapshot)
}
