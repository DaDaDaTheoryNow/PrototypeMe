package com.dadadadev.prototype_me.domains.board.core.impl.data.store

import com.dadadadev.prototype_me.domains.board.core.api.domain.model.BoardEntity

class InMemoryBoardStore<T : BoardEntity> {
    private val data = linkedMapOf<String, T>()

    fun snapshot(): Map<String, T> = data.toMap()

    fun put(entity: T) {
        data[entity.id] = entity
    }

    fun remove(entityId: String) {
        data.remove(entityId)
    }
}
