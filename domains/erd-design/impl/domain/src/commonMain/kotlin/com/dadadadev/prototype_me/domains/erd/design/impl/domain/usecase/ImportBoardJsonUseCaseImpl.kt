package com.dadadadev.prototype_me.domains.erd.design.impl.domain.usecase

import com.dadadadev.prototype_me.domains.board.core.api.domain.model.BoardSnapshot
import com.dadadadev.prototype_me.domains.erd.design.api.data.codec.ErdBoardJsonCodec
import com.dadadadev.prototype_me.domains.erd.design.api.domain.model.ErdEntityNode
import com.dadadadev.prototype_me.domains.erd.design.api.domain.model.ErdRelationEdge
import com.dadadadev.prototype_me.domains.erd.design.api.domain.usecase.ImportBoardJsonUseCase

internal class ImportBoardJsonUseCaseImpl(
    private val codec: ErdBoardJsonCodec,
) : ImportBoardJsonUseCase {
    override fun invoke(rawJson: String): Result<BoardSnapshot<ErdEntityNode, ErdRelationEdge>> =
        codec.decode(rawJson)
}
