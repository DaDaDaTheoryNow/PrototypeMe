package com.dadadadev.prototype_me.domains.erd.design.impl.data.mock

import com.dadadadev.prototype_me.domains.board.core.api.domain.model.BoardSnapshot
import com.dadadadev.prototype_me.domains.board.core.api.domain.model.BoardSyncEffect
import com.dadadadev.prototype_me.domains.board.core.api.domain.model.toBoardSnapshot
import com.dadadadev.prototype_me.domains.erd.design.api.domain.model.ErdEntityNode
import com.dadadadev.prototype_me.domains.erd.design.api.domain.model.ErdBoardAction
import com.dadadadev.prototype_me.domains.erd.design.api.domain.model.ErdBoardContext
import com.dadadadev.prototype_me.domains.erd.design.api.domain.model.Position
import com.dadadadev.prototype_me.domains.erd.design.api.domain.model.ErdRelationEdge
import com.dadadadev.prototype_me.domains.erd.design.api.data.repository.ErdBoardRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class MockBoardRepositoryImpl(
    private val fakeServer: MockBoardServer,
) : ErdBoardRepository {

    private val actionQueue = ArrayDeque<ErdBoardAction>()
    private var actionSequence: Long = 0
    private var connectedUserId: String = DEFAULT_USER_ID

    override fun observeBoard(boardId: String): Flow<BoardSnapshot<ErdEntityNode, ErdRelationEdge>> =
        observeBoardState(boardId).map(ErdBoardContext::toBoardSnapshot)

    override fun observeEntity(boardId: String, entityId: String): Flow<ErdEntityNode?> =
        observeBoardState(boardId).map { context -> context.nodes[entityId] }

    override suspend fun upsertEntity(boardId: String, entity: ErdEntityNode) {
        sendAction(ErdBoardAction.AddNode(node = entity, actionId = nextActionId()))
    }

    override suspend fun deleteEntity(boardId: String, entityId: String) {
        sendAction(ErdBoardAction.DeleteNode(nodeId = entityId, actionId = nextActionId()))
    }

    override suspend fun upsertEdge(boardId: String, edge: ErdRelationEdge) {
        sendAction(ErdBoardAction.AddEdge(edge = edge, actionId = nextActionId()))
    }

    override suspend fun deleteEdge(boardId: String, edgeId: String) {
        sendAction(ErdBoardAction.DeleteEdge(edgeId = edgeId, actionId = nextActionId()))
    }

    override fun observeBoardState(boardId: String): Flow<ErdBoardContext> = fakeServer.serverStateFlow

    override fun observeSideEffects(): Flow<BoardSyncEffect> = fakeServer.effectsFlow

    override suspend fun connect(boardId: String, currentUserId: String) {
        connectedUserId = currentUserId
    }

    override suspend fun disconnect() = Unit

    override suspend fun requestLock(entityId: String) {
        fakeServer.requestLock(entityId, connectedUserId)
    }

    override suspend fun releaseLock(entityId: String) {
        fakeServer.releaseLock(entityId, connectedUserId)
    }

    override suspend fun sendAction(action: ErdBoardAction) {
        sendActions(listOf(action))
    }

    override suspend fun sendNodeDragUpdate(nodeId: String, position: Position) = Unit

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
        fakeServer.processActions(pendingActions, connectedUserId)
    }

    private fun nextActionId(): String {
        actionSequence += 1
        return "core-action-$actionSequence"
    }

    private companion object {
        const val DEFAULT_USER_ID = "unknown_user"
    }
}
