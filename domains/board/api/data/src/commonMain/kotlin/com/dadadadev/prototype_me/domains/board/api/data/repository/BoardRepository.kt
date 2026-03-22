package com.dadadadev.prototype_me.domain.repository

import com.dadadadev.prototype_me.domain.models.BoardAction
import com.dadadadev.prototype_me.domain.models.BoardContext
import com.dadadadev.prototype_me.domain.models.BoardSyncEffect
import kotlinx.coroutines.flow.Flow

interface BoardRepository {
    /** Hot flow of the full board state, replayed on subscription. */
    fun observeBoardState(boardId: String): Flow<BoardContext>

    /** Hot flow of side-effects: lock conflicts, disconnections, and so on. */
    fun observeSideEffects(): Flow<BoardSyncEffect>

    /** Establish connection to the board (sets up socket or mock binding). */
    suspend fun connect(boardId: String, currentUserId: String)

    /** Clean up connection resources. */
    suspend fun disconnect()

    /** Request an element-level lock before dragging a node. */
    suspend fun requestLock(nodeId: String)

    /** Release the lock after dragging ends. */
    suspend fun releaseLock(nodeId: String)

    /** Enqueue an action that will be flushed to the server. */
    suspend fun sendAction(action: BoardAction)
}
