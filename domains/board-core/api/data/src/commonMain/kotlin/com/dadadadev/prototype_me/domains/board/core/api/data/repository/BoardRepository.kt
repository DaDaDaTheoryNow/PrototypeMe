package com.dadadadev.prototype_me.domains.board.core.api.data.repository

import com.dadadadev.prototype_me.domains.board.core.api.domain.model.BoardEdge
import com.dadadadev.prototype_me.domains.board.core.api.domain.model.BoardEntity
import com.dadadadev.prototype_me.domains.board.core.api.domain.model.BoardSnapshot
import kotlinx.coroutines.flow.Flow

interface BoardReadRepository<T : BoardEntity> {
    fun observeBoard(boardId: String): Flow<BoardSnapshot<T>>
    fun observeEntity(boardId: String, entityId: String): Flow<T?>
}

interface BoardWriteRepository<T : BoardEntity> {
    suspend fun upsertEntity(boardId: String, entity: T)
    suspend fun deleteEntity(boardId: String, entityId: String)
    suspend fun upsertEdge(boardId: String, edge: BoardEdge)
    suspend fun deleteEdge(boardId: String, edgeId: String)
}

interface BoardRepository<T : BoardEntity> :
    BoardReadRepository<T>,
    BoardWriteRepository<T>
