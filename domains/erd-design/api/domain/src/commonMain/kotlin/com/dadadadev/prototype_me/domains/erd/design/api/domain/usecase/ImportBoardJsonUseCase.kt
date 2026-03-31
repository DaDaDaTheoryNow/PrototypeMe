package com.dadadadev.prototype_me.domains.erd.design.api.domain.usecase

import com.dadadadev.prototype_me.domains.board.core.api.domain.model.BoardSnapshot
import com.dadadadev.prototype_me.domains.erd.design.api.domain.model.ErdEntityNode
import com.dadadadev.prototype_me.domains.erd.design.api.domain.model.ErdRelationEdge

/**
 * Deserialises a raw JSON string into a board snapshot.
 *
 * Returns a [Result] so the caller can handle malformed JSON gracefully without
 * relying on exceptions for control flow.
 *
 * This is a pure, synchronous transformation — no network or disk I/O is involved.
 */
interface ImportBoardJsonUseCase {
    operator fun invoke(rawJson: String): Result<BoardSnapshot<ErdEntityNode, ErdRelationEdge>>
}
