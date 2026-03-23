package com.dadadadev.prototype_me.domains.board.core.api.data.repository

import com.dadadadev.prototype_me.domains.board.core.api.domain.model.BoardContext
import com.dadadadev.prototype_me.domains.board.core.api.domain.model.BoardEdge
import com.dadadadev.prototype_me.domains.board.core.api.domain.model.BoardEntity
import com.dadadadev.prototype_me.domains.board.core.api.domain.model.BoardMutation
import com.dadadadev.prototype_me.domains.board.core.api.domain.model.BoardSyncEffect
import kotlinx.coroutines.flow.Flow

interface RealtimeBoardRepository<T : BoardEntity, E : BoardEdge, A : BoardMutation> : BoardRepository<T> {
    fun observeBoardState(boardId: String): Flow<BoardContext<T, E>>
    fun observeSideEffects(): Flow<BoardSyncEffect>
    suspend fun connect(boardId: String, currentUserId: String)
    suspend fun disconnect()
    suspend fun requestLock(entityId: String)
    suspend fun releaseLock(entityId: String)
    suspend fun sendAction(action: A)
}
