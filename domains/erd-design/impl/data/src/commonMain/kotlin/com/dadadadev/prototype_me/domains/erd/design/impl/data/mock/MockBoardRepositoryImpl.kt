package com.dadadadev.prototype_me.domains.erd.design.impl.data.mock

import com.dadadadev.prototype_me.domains.board.core.api.domain.model.BoardSnapshot
import com.dadadadev.prototype_me.domains.board.core.api.domain.model.BoardSyncEffect
import com.dadadadev.prototype_me.domains.board.core.api.domain.model.toBoardSnapshot
import com.dadadadev.prototype_me.domains.erd.design.api.domain.model.EntityNode
import com.dadadadev.prototype_me.domains.erd.design.api.domain.model.ErdBoardAction
import com.dadadadev.prototype_me.domains.erd.design.api.domain.model.ErdBoardContext
import com.dadadadev.prototype_me.domains.erd.design.api.domain.model.RelationEdge
import com.dadadadev.prototype_me.domains.erd.design.api.data.repository.ErdBoardRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class MockBoardRepositoryImpl(
    private val fakeServer: MockBoardServer,
    private val currentUserId: String,
) : ErdBoardRepository {

    private val actionQueue = ArrayDeque<ErdBoardAction>()
    private var actionSequence: Long = 0

    override fun observeBoard(boardId: String): Flow<BoardSnapshot<EntityNode, RelationEdge>> =
        observeBoardState(boardId).map(ErdBoardContext::toBoardSnapshot)

    override fun observeEntity(boardId: String, entityId: String): Flow<EntityNode?> =
        observeBoardState(boardId).map { context -> context.nodes[entityId] }

    override suspend fun upsertEntity(boardId: String, entity: EntityNode) {
        sendAction(ErdBoardAction.AddNode(node = entity, actionId = nextActionId()))
    }

    override suspend fun deleteEntity(boardId: String, entityId: String) {
        sendAction(ErdBoardAction.DeleteNode(nodeId = entityId, actionId = nextActionId()))
    }

    override suspend fun upsertEdge(boardId: String, edge: RelationEdge) {
        sendAction(ErdBoardAction.AddEdge(edge = edge, actionId = nextActionId()))
    }

    override suspend fun deleteEdge(boardId: String, edgeId: String) {
        sendAction(ErdBoardAction.DeleteEdge(edgeId = edgeId, actionId = nextActionId()))
    }

    override fun observeBoardState(boardId: String): Flow<ErdBoardContext> = fakeServer.serverStateFlow

    override fun observeSideEffects(): Flow<BoardSyncEffect> = fakeServer.effectsFlow

    override suspend fun connect(boardId: String, currentUserId: String) = Unit

    override suspend fun disconnect() = Unit

    override suspend fun requestLock(entityId: String) {
        fakeServer.requestLock(entityId, currentUserId)
    }

    override suspend fun releaseLock(entityId: String) {
        fakeServer.releaseLock(entityId, currentUserId)
    }

    override suspend fun sendAction(action: ErdBoardAction) {
        sendActions(listOf(action))
    }

    override suspend fun sendActions(actions: Collection<ErdBoardAction>) {
        if (actions.isEmpty()) return
        actionQueue.addAll(actions)
        flushQueue()
    }

    private suspend fun flushQueue() {
        if (actionQueue.isEmpty()) return

        val pendingActions = buildList {
            while (actionQueue.isNotEmpty()) {
                add(actionQueue.removeFirst())
            }
        }
        fakeServer.processActions(pendingActions, currentUserId)
    }

    private fun nextActionId(): String {
        actionSequence += 1
        return "core-action-$actionSequence"
    }
}
