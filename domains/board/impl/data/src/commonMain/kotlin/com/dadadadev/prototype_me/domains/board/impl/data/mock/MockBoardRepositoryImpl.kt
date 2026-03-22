package com.dadadadev.prototype_me.domains.board.impl.data.mock

import com.dadadadev.prototype_me.domain.models.BoardAction
import com.dadadadev.prototype_me.domain.models.BoardContext
import com.dadadadev.prototype_me.domain.models.BoardSyncEffect
import com.dadadadev.prototype_me.domain.repository.BoardRepository
import kotlinx.coroutines.flow.Flow

/**
 * Mock implementation of [BoardRepository] backed by [MockBoardServer].
 *
 * Uses an in-memory queue to batch and flush actions similarly to a reconnect-capable backend.
 */
class MockBoardRepositoryImpl(
    private val fakeServer: MockBoardServer,
    private val currentUserId: String
) : BoardRepository {

    private val actionQueue = ArrayDeque<BoardAction>()

    override fun observeBoardState(boardId: String): Flow<BoardContext> =
        fakeServer.serverStateFlow

    override fun observeSideEffects(): Flow<BoardSyncEffect> =
        fakeServer.effectsFlow

    override suspend fun connect(boardId: String, currentUserId: String) {
        // No-op for mock: the server is always "connected".
    }

    override suspend fun disconnect() {
        // No-op for mock.
    }

    override suspend fun requestLock(nodeId: String) {
        fakeServer.requestLock(nodeId, currentUserId)
    }

    override suspend fun releaseLock(nodeId: String) {
        fakeServer.releaseLock(nodeId, currentUserId)
    }

    override suspend fun sendAction(action: BoardAction) {
        actionQueue.add(action)
        flushQueue()
    }

    private suspend fun flushQueue() {
        while (actionQueue.isNotEmpty()) {
            val action = actionQueue.removeFirst()
            fakeServer.processAction(action, currentUserId)
        }
    }
}
