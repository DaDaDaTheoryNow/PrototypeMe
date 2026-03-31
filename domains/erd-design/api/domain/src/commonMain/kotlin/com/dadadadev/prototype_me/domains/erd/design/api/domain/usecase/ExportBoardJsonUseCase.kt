package com.dadadadev.prototype_me.domains.erd.design.api.domain.usecase

import com.dadadadev.prototype_me.domains.board.core.api.domain.model.BoardSnapshot
import com.dadadadev.prototype_me.domains.erd.design.api.domain.model.ErdEntityNode
import com.dadadadev.prototype_me.domains.erd.design.api.domain.model.ErdRelationEdge

/**
 * Serialises a board snapshot to its JSON string representation.
 *
 * This is a pure, synchronous transformation — no network or disk I/O is involved.
 */
interface ExportBoardJsonUseCase {
    operator fun invoke(snapshot: BoardSnapshot<ErdEntityNode, ErdRelationEdge>): String
}
