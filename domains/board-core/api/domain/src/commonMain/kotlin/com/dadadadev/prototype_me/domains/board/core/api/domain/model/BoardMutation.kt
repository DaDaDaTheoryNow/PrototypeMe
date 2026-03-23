package com.dadadadev.prototype_me.domains.board.core.api.domain.model

interface BoardMutation {
    val actionId: String
}

interface MoveEntityMutation : BoardMutation {
    val entityId: String
    val newPosition: BoardPoint
}

interface UpsertEntityMutation<T : BoardEntity> : BoardMutation {
    val entity: T
}

interface DeleteEntityMutation : BoardMutation {
    val entityId: String
}

interface UpsertEdgeMutation<T : BoardEdge> : BoardMutation {
    val edge: T
}

interface DeleteEdgeMutation : BoardMutation {
    val edgeId: String
}
