package com.dadadadev.prototype_me.domains.board.core.api.data.codec

import com.dadadadev.prototype_me.domains.board.core.api.domain.model.BoardEdge
import com.dadadadev.prototype_me.domains.board.core.api.domain.model.BoardEntity
import com.dadadadev.prototype_me.domains.board.core.api.domain.model.BoardSnapshot

interface BoardSnapshotJsonCodec<TNode : BoardEntity, TEdge : BoardEdge> {
    fun encode(snapshot: BoardSnapshot<TNode, TEdge>): String
    fun decode(rawJson: String): Result<BoardSnapshot<TNode, TEdge>>
}
