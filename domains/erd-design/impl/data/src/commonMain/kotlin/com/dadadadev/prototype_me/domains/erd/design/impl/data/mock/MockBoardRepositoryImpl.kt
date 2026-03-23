package com.dadadadev.prototype_me.domains.erd.design.impl.data.mock

import com.dadadadev.prototype_me.domains.erd.design.api.data.repository.BoardRepository
import com.dadadadev.prototype_me.domains.erd.design.api.domain.model.BoardAction
import com.dadadadev.prototype_me.domains.erd.design.api.domain.model.BoardContext
import com.dadadadev.prototype_me.domains.board.core.api.domain.model.BoardSyncEffect
import com.dadadadev.prototype_me.domains.erd.design.api.domain.model.EntityNode
import com.dadadadev.prototype_me.domains.erd.design.api.domain.model.RelationEdge
import com.dadadadev.prototype_me.domains.board.core.api.domain.model.toBoardSnapshot
import com.dadadadev.prototype_me.domains.board.core.api.domain.model.BoardEdge
import com.dadadadev.prototype_me.domains.board.core.api.domain.model.BoardSnapshot
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Mock implementation of [BoardRepository] backed by [MockBoardServer].
 *
 * Uses an in-memory queue to batch and flush actions similarly to a reconnect-capable backend.
 */
class MockBoardRepositoryImpl(
    private val fakeServer: MockBoardServer,
    private val currentUserId: String,
) : BoardRepository {

    private val actionQueue = ArrayDeque<BoardAction>()
    private var actionSequence: Long = 0

    override fun observeBoard(boardId: String): Flow<BoardSnapshot<EntityNode>> =
        observeBoardState(boardId).map { context -> context.toBoardSnapshot() }

    override fun observeEntity(boardId: String, entityId: String): Flow<EntityNode?> =
        observeBoardState(boardId).map { context -> context.nodes[entityId] }

    override suspend fun upsertEntity(boardId: String, entity: EntityNode) {
        sendAction(
            BoardAction.AddNode(
                node = entity,
                actionId = nextActionId(),
            ),
        )
    }

    override suspend fun deleteEntity(boardId: String, entityId: String) {
        sendAction(
            BoardAction.DeleteNode(
                nodeId = entityId,
                actionId = nextActionId(),
            ),
        )
    }

    override suspend fun upsertEdge(boardId: String, edge: BoardEdge) {
        val relationEdge = edge.toRelationEdge()
        sendAction(
            BoardAction.AddEdge(
                edge = relationEdge,
                actionId = nextActionId(),
            ),
        )
    }

    override suspend fun deleteEdge(boardId: String, edgeId: String) {
        sendAction(
            BoardAction.DeleteEdge(
                edgeId = edgeId,
                actionId = nextActionId(),
            ),
        )
    }

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

    override suspend fun requestLock(entityId: String) {
        fakeServer.requestLock(entityId, currentUserId)
    }

    override suspend fun releaseLock(entityId: String) {
        fakeServer.releaseLock(entityId, currentUserId)
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

    private fun nextActionId(): String {
        actionSequence += 1
        return "core-action-$actionSequence"
    }

    private fun BoardEdge.toRelationEdge(): RelationEdge = when (this) {
        is RelationEdge -> this
        else -> RelationEdge(
            id = id,
            sourceNodeId = sourceId,
            targetNodeId = targetId,
            label = label,
        )
    }
}




