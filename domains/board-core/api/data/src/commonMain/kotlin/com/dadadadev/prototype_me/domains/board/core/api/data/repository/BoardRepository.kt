package com.dadadadev.prototype_me.domains.board.core.api.data.repository

import com.dadadadev.prototype_me.domains.board.core.api.domain.model.BoardEdge
import com.dadadadev.prototype_me.domains.board.core.api.domain.model.BoardEntity
import com.dadadadev.prototype_me.domains.board.core.api.domain.model.BoardSnapshot
import kotlinx.coroutines.flow.Flow

interface BoardReadRepository<TNode : BoardEntity, TEdge : BoardEdge> {
    fun observeBoard(boardId: String): Flow<BoardSnapshot<TNode, TEdge>>
    fun observeEntity(boardId: String, entityId: String): Flow<TNode?>
}

interface BoardWriteRepository<TNode : BoardEntity, TEdge : BoardEdge> {
    suspend fun upsertEntity(boardId: String, entity: TNode)
    suspend fun deleteEntity(boardId: String, entityId: String)
    suspend fun upsertEdge(boardId: String, edge: TEdge)
    suspend fun deleteEdge(boardId: String, edgeId: String)

    suspend fun upsertEntities(boardId: String, entities: Collection<TNode>) {
        entities.forEach { entity -> upsertEntity(boardId, entity) }
    }

    suspend fun deleteEntities(boardId: String, entityIds: Collection<String>) {
        entityIds.forEach { entityId -> deleteEntity(boardId, entityId) }
    }

    suspend fun upsertEdges(boardId: String, edges: Collection<TEdge>) {
        edges.forEach { edge -> upsertEdge(boardId, edge) }
    }

    suspend fun deleteEdges(boardId: String, edgeIds: Collection<String>) {
        edgeIds.forEach { edgeId -> deleteEdge(boardId, edgeId) }
    }
}

interface BoardRepository<TNode : BoardEntity, TEdge : BoardEdge> :
    BoardReadRepository<TNode, TEdge>,
    BoardWriteRepository<TNode, TEdge>
