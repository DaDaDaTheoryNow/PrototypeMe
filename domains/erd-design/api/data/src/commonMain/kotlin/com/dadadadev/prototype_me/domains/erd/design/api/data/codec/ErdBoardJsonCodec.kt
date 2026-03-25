package com.dadadadev.prototype_me.domains.erd.design.api.data.codec

import com.dadadadev.prototype_me.domains.board.core.api.data.codec.BoardSnapshotJsonCodec
import com.dadadadev.prototype_me.domains.erd.design.api.domain.model.EntityNode
import com.dadadadev.prototype_me.domains.erd.design.api.domain.model.RelationEdge

interface ErdBoardJsonCodec : BoardSnapshotJsonCodec<EntityNode, RelationEdge>
