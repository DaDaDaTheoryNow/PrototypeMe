package com.dadadadev.prototype_me.domains.erd.design.impl.data.remote.repository

import com.dadadadev.prototype_me.core.common.error.NetworkException
import com.dadadadev.prototype_me.core.common.result.AppResult
import com.dadadadev.prototype_me.core.common.result.onFailure
import com.dadadadev.prototype_me.core.common.result.onSuccess
import com.dadadadev.prototype_me.domains.board.core.api.domain.model.BoardSnapshot
import com.dadadadev.prototype_me.domains.board.core.api.domain.model.BoardSyncEffect
import com.dadadadev.prototype_me.domains.board.core.api.domain.model.toBoardSnapshot
import com.dadadadev.prototype_me.domains.erd.design.api.data.repository.ErdBoardRemoteRepository
import com.dadadadev.prototype_me.domains.erd.design.api.domain.model.ErdBoardCreationApproval
import com.dadadadev.prototype_me.domains.erd.design.api.domain.model.ErdBoardJoinSession
import com.dadadadev.prototype_me.domains.erd.design.api.domain.model.ErdInviteResolution
import com.dadadadev.prototype_me.domains.erd.design.impl.data.remote.network.ActionSubmitOutcome
import com.dadadadev.prototype_me.domains.erd.design.impl.data.remote.network.ErdBoardRemoteDataSource
import com.dadadadev.prototype_me.domains.erd.design.impl.data.remote.network.LockRequestOutcome
import com.dadadadev.prototype_me.domains.erd.design.impl.data.remote.network.RealtimeTransportEvent
import com.dadadadev.prototype_me.domains.erd.design.impl.data.remote.realtime.ErdRealtimeBoardClient
import com.dadadadev.prototype_me.domains.erd.design.api.domain.model.ErdBoardAction
import com.dadadadev.prototype_me.domains.erd.design.api.domain.model.ErdBoardContext
import com.dadadadev.prototype_me.domains.erd.design.api.domain.model.ErdEntityNode
import com.dadadadev.prototype_me.domains.erd.design.api.domain.model.Position
import com.dadadadev.prototype_me.domains.erd.design.api.domain.model.ErdRelationEdge
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

internal class RemoteErdBoardRepositoryImpl(
    private val dataSource: ErdBoardRemoteDataSource,
    private val realtimeClient: ErdRealtimeBoardClient,
) : ErdBoardRemoteRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val persistedBoardState = MutableStateFlow<ErdBoardContext?>(null)
    private val remoteDragPositions = MutableStateFlow<Map<String, Position>>(emptyMap())
    private val sideEffects = MutableSharedFlow<BoardSyncEffect>(extraBufferCapacity = 32)

    private var eventsJob: Job? = null
    private var activeBoardId: String? = null
    private var activeUserId: String = DEFAULT_USER_ID
    private var currentVersion: Long = 0L
    private var actionSequence: Long = 0L

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

    override fun observeBoardState(boardId: String): Flow<ErdBoardContext> =
        combine(
            persistedBoardState.filterNotNull(),
            remoteDragPositions,
        ) { state, dragPositions ->
            state.overlayRemoteDragPositions(dragPositions)
        }.mapNotNull { state -> state.takeIf { it.boardId == boardId } }

    override fun observeSideEffects(): Flow<BoardSyncEffect> = sideEffects.asSharedFlow()

    override suspend fun connect(boardId: String, currentUserId: String) {
        log("connect:start boardId=$boardId currentUserId=$currentUserId")
        activeBoardId = boardId
        activeUserId = currentUserId
        clearRemoteDragPositions()

        val boardResult = dataSource.getBoard(boardId)
        boardResult
            .onSuccess { snapshot ->
                log("connect:getBoard success boardId=$boardId version=${snapshot.version} nodes=${snapshot.context.nodes.size} edges=${snapshot.context.edges.size}")
                currentVersion = snapshot.version
                persistedBoardState.value = snapshot.context
                clearRemoteDragPositions()
            }
            .onFailure { error ->
                log("connect:getBoard failure boardId=$boardId error=${error.message}")
                sideEffects.tryEmit(BoardSyncEffect.ConnectionFailed(error.toBoardConnectionMessage(boardId)))
            }
        if (boardResult is AppResult.Failure) return

        val joinResult = dataSource.joinBoard(
            boardId = boardId,
            displayName = currentUserId,
            inviteToken = null,
        )

        when (joinResult) {
            is AppResult.Success -> {
                val session = joinResult.data
                activeUserId = session.userId
                log("connect:join success boardId=$boardId role=${session.role} expiresAt=${session.expiresAt}")

                eventsJob?.cancelAndJoin()
                eventsJob = scope.launch {
                    log("events:start boardId=$boardId")
                    realtimeClient.events.collect { event ->
                        log("events:collect boardId=$boardId event=${event.describe()}")
                        handleRealtimeEvent(event)
                    }
                }

                val realtimeResult = realtimeClient.connect(
                    boardId = boardId,
                    sessionToken = session.sessionToken,
                    lastSeenVersion = currentVersion,
                )

                if (realtimeResult is AppResult.Failure) {
                    log("connect:realtime failure boardId=$boardId error=${realtimeResult.error.message}")
                    sideEffects.tryEmit(BoardSyncEffect.ConnectionLost)
                    return
                }
                log("connect:realtime success boardId=$boardId")
            }

            is AppResult.Failure -> {
                log("connect:join failure boardId=$boardId error=${joinResult.error.message}")
                sideEffects.tryEmit(BoardSyncEffect.ConnectionFailed(joinResult.error.toBoardConnectionMessage(boardId)))
            }
        }
    }

    override suspend fun disconnect() {
        log("disconnect:start activeBoardId=$activeBoardId")
        eventsJob?.cancelAndJoin()
        eventsJob = null
        realtimeClient.disconnect()
        clearRemoteDragPositions()
        log("disconnect:done")
    }

    override suspend fun requestLock(entityId: String) {
        val boardId = activeBoardId ?: return

        val realtimeResult = realtimeClient.requestLock(entityId)
        if (realtimeResult is AppResult.Success) return

        dataSource.requestLock(boardId, entityId)
            .onSuccess { outcome ->
                when (outcome) {
                    is LockRequestOutcome.Granted -> {
                        updateNodeLock(outcome.nodeId, outcome.lockedBy)
                    }

                    is LockRequestOutcome.Rejected -> {
                        sideEffects.tryEmit(
                            BoardSyncEffect.LockRejected(
                                nodeId = outcome.nodeId,
                                lockedBy = outcome.lockedBy,
                            ),
                        )
                    }
                }
            }
            .onFailure {
                sideEffects.tryEmit(BoardSyncEffect.ConnectionLost)
            }
    }

    override suspend fun releaseLock(entityId: String) {
        val boardId = activeBoardId ?: return

        val realtimeResult = realtimeClient.releaseLock(entityId)
        if (realtimeResult is AppResult.Success) return

        dataSource.releaseLock(boardId, entityId)
            .onSuccess { released ->
                if (released) {
                    updateNodeLock(entityId, null)
                }
            }
            .onFailure {
                sideEffects.tryEmit(BoardSyncEffect.ConnectionLost)
            }
    }

    override suspend fun sendAction(action: ErdBoardAction) {
        val boardId = activeBoardId ?: return

        val realtimeResult = realtimeClient.sendAction(action)
        if (realtimeResult is AppResult.Success) return

        dataSource.submitAction(boardId, action)
            .onSuccess { outcome ->
                when (outcome) {
                    is ActionSubmitOutcome.Accepted -> {
                        currentVersion = maxOf(currentVersion + 1, outcome.serverVersion)
                        applyActionToState(action)
                    }

                    is ActionSubmitOutcome.Rejected -> {
                        val nodeId = action.lockedEntityId()
                        if (nodeId != null && outcome.lockedBy != null) {
                            sideEffects.tryEmit(
                                BoardSyncEffect.LockRejected(
                                    nodeId = nodeId,
                                    lockedBy = outcome.lockedBy,
                                ),
                            )
                        }
                    }
                }
            }
            .onFailure {
                sideEffects.tryEmit(BoardSyncEffect.ConnectionLost)
            }
    }

    override suspend fun sendNodeDragUpdate(nodeId: String, position: Position) {
        val realtimeResult = realtimeClient.sendNodeDragUpdate(nodeId, position)
        if (realtimeResult is AppResult.Failure) {
            log("send:node-drag-update failure nodeId=$nodeId error=${realtimeResult.error.message}")
        }
    }

    override suspend fun createBoard(
        displayName: String,
        title: String?,
    ): AppResult<ErdBoardCreationApproval, NetworkException> = dataSource.createBoard(displayName, title)

    override suspend fun joinBoard(
        boardId: String,
        displayName: String,
        inviteToken: String?,
    ): AppResult<ErdBoardJoinSession, NetworkException> = dataSource.joinBoard(boardId, displayName, inviteToken)

    override suspend fun resolveInvite(inviteToken: String): AppResult<ErdInviteResolution, NetworkException> =
        dataSource.resolveInvite(inviteToken)

    private fun handleRealtimeEvent(event: RealtimeTransportEvent) {
        log("handle:event ${event.describe()}")
        when (event) {
            is RealtimeTransportEvent.SnapshotFull -> {
                currentVersion = event.version
                persistedBoardState.value = event.context
                clearRemoteDragPositions()
                log("handle:snapshot-full version=$currentVersion nodes=${event.context.nodes.size} edges=${event.context.edges.size}")
            }

            is RealtimeTransportEvent.SnapshotPatch -> {
                var context = persistedBoardState.value ?: return
                event.applied.sortedBy { it.version }.forEach { applied ->
                    context = context.applyAction(applied.action)
                    currentVersion = maxOf(currentVersion, applied.version)
                }
                persistedBoardState.value = context
                clearRemoteDragPositions()
                log("handle:snapshot-patch currentVersion=$currentVersion nodes=${context.nodes.size} edges=${context.edges.size}")
            }

            is RealtimeTransportEvent.ActionApplied -> {
                val context = persistedBoardState.value ?: return
                persistedBoardState.value = context.applyAction(event.payload.action)
                currentVersion = maxOf(currentVersion, event.payload.version)
                if (event.payload.action is ErdBoardAction.MoveNode) {
                    clearRemoteDragPosition(event.payload.action.nodeId)
                }
                log("handle:action-applied version=$currentVersion action=${event.payload.action::class.simpleName}")
            }

            is RealtimeTransportEvent.ActionRejected -> {
                log("handle:action-rejected actionId=${event.actionId} reason=${event.reason} lockedBy=${event.lockedBy}")
            }

            is RealtimeTransportEvent.LockGranted -> {
                updateNodeLock(event.nodeId, event.lockedBy)
                log("handle:lock-granted nodeId=${event.nodeId} lockedBy=${event.lockedBy}")
            }

            is RealtimeTransportEvent.LockRejected -> {
                sideEffects.tryEmit(BoardSyncEffect.LockRejected(event.nodeId, event.lockedBy))
                log("handle:lock-rejected nodeId=${event.nodeId} lockedBy=${event.lockedBy}")
            }

            is RealtimeTransportEvent.LockReleased -> {
                updateNodeLock(event.nodeId, null)
                clearRemoteDragPosition(event.nodeId)
                log("handle:lock-released nodeId=${event.nodeId} releasedBy=${event.releasedBy}")
            }

            is RealtimeTransportEvent.PresenceUpdated -> {
                log("handle:presence-updated boardId=${event.boardId} onlineCount=${event.onlineCount}")
            }

            is RealtimeTransportEvent.NodeDragUpdated -> {
                if (event.actorUserId == activeUserId) {
                    log("handle:node-drag-updated ignored-self nodeId=${event.nodeId}")
                } else {
                    remoteDragPositions.value = remoteDragPositions.value + (event.nodeId to event.position)
                    log("handle:node-drag-updated nodeId=${event.nodeId} actorUserId=${event.actorUserId} position=${event.position}")
                }
            }

            is RealtimeTransportEvent.ConnectionLost -> {
                sideEffects.tryEmit(BoardSyncEffect.ConnectionLost)
                clearRemoteDragPositions()
                log("handle:connection-lost reason=${event.reason}")
            }

            is RealtimeTransportEvent.Unknown -> {
                log("handle:unknown type=${event.type} requestId=${event.requestId}")
            }
        }
    }

    private fun applyActionToState(action: ErdBoardAction) {
        val context = persistedBoardState.value ?: return
        persistedBoardState.value = context.applyAction(action)
        if (action is ErdBoardAction.MoveNode) {
            clearRemoteDragPosition(action.nodeId)
        }
    }

    private fun updateNodeLock(nodeId: String, lockedBy: String?) {
        val context = persistedBoardState.value ?: return
        val node = context.nodes[nodeId] ?: return
        persistedBoardState.value = context.copy(
            nodes = context.nodes + (nodeId to node.copy(lockedBy = lockedBy)),
        )
    }

    private fun clearRemoteDragPositions() {
        remoteDragPositions.value = emptyMap()
    }

    private fun clearRemoteDragPosition(nodeId: String) {
        remoteDragPositions.value = remoteDragPositions.value - nodeId
    }

    private fun ErdBoardContext.overlayRemoteDragPositions(dragPositions: Map<String, Position>): ErdBoardContext =
        if (dragPositions.isEmpty()) {
            this
        } else {
            copy(
                nodes = nodes.mapValues { (nodeId, node) ->
                    val previewPosition = dragPositions[nodeId]
                    if (previewPosition != null) node.copy(position = previewPosition) else node
                },
            )
        }

    private fun NetworkException.toBoardConnectionMessage(boardId: String): String = when (this) {
        is NetworkException.HttpError -> when (statusCode) {
            404 -> "Board '$boardId' was not found on the server."
            401 -> "Backend rejected access to board '$boardId'."
            else -> "Failed to open board '$boardId' (HTTP $statusCode)."
        }

        NetworkException.NoInternet -> "Cannot reach the backend server."
        NetworkException.RequestTimeout -> "Timed out while loading board '$boardId'."
        NetworkException.Unauthorized -> "Backend rejected access to board '$boardId'."
        is NetworkException.SerializationError -> "Backend returned an unexpected payload for board '$boardId'."
        is NetworkException.Unknown -> "Failed to open board '$boardId': ${message}"
    }

    private fun log(message: String) {
        println("[erd-realtime-repository] $message")
    }

    private fun RealtimeTransportEvent.describe(): String = when (this) {
        is RealtimeTransportEvent.SnapshotFull ->
            "SnapshotFull version=$version nodes=${context.nodes.size} edges=${context.edges.size}"
        is RealtimeTransportEvent.SnapshotPatch ->
            "SnapshotPatch fromVersion=$fromVersion toVersion=$toVersion applied=${applied.size}"
        is RealtimeTransportEvent.ActionApplied ->
            "ActionApplied version=${payload.version} action=${payload.action::class.simpleName}"
        is RealtimeTransportEvent.ActionRejected ->
            "ActionRejected actionId=$actionId reason=$reason"
        is RealtimeTransportEvent.LockGranted ->
            "LockGranted nodeId=$nodeId lockedBy=$lockedBy"
        is RealtimeTransportEvent.LockRejected ->
            "LockRejected nodeId=$nodeId lockedBy=$lockedBy"
        is RealtimeTransportEvent.LockReleased ->
            "LockReleased nodeId=$nodeId releasedBy=$releasedBy"
        is RealtimeTransportEvent.PresenceUpdated ->
            "PresenceUpdated boardId=$boardId onlineCount=$onlineCount"
        is RealtimeTransportEvent.NodeDragUpdated ->
            "NodeDragUpdated boardId=$boardId nodeId=$nodeId actorUserId=$actorUserId"
        is RealtimeTransportEvent.ConnectionLost ->
            "ConnectionLost reason=$reason"
        is RealtimeTransportEvent.Unknown ->
            "Unknown type=$type requestId=$requestId"
    }

    private fun ErdBoardContext.applyAction(action: ErdBoardAction): ErdBoardContext = when (action) {
        is ErdBoardAction.MoveNode -> {
            val node = nodes[action.nodeId] ?: return this
            copy(nodes = nodes + (action.nodeId to node.copy(position = action.newPosition)))
        }

        is ErdBoardAction.AddNode -> copy(nodes = nodes + (action.node.id to action.node))

        is ErdBoardAction.DeleteNode -> {
            val remainingEdges = edges.filter { (_, edge) ->
                edge.sourceNodeId != action.nodeId && edge.targetNodeId != action.nodeId
            }
            copy(nodes = nodes - action.nodeId, edges = remainingEdges)
        }

        is ErdBoardAction.AddEdge -> copy(edges = edges + (action.edge.id to action.edge))

        is ErdBoardAction.AddField -> {
            val node = nodes[action.nodeId] ?: return this
            copy(nodes = nodes + (action.nodeId to node.copy(fields = node.fields + action.field)))
        }

        is ErdBoardAction.RemoveField -> {
            val node = nodes[action.nodeId] ?: return this
            copy(
                nodes = nodes + (
                    action.nodeId to node.copy(fields = node.fields.filter { it.id != action.fieldId })
                ),
            )
        }

        is ErdBoardAction.RenameField -> {
            val node = nodes[action.nodeId] ?: return this
            val updatedFields = node.fields.map { field ->
                if (field.id == action.fieldId) {
                    field.copy(name = action.newName, type = action.newType)
                } else {
                    field
                }
            }
            copy(nodes = nodes + (action.nodeId to node.copy(fields = updatedFields)))
        }

        is ErdBoardAction.DeleteEdge -> copy(edges = edges - action.edgeId)
    }

    private fun ErdBoardAction.lockedEntityId(): String? = when (this) {
        is ErdBoardAction.MoveNode -> nodeId
        is ErdBoardAction.AddNode -> node.id
        is ErdBoardAction.DeleteNode -> nodeId
        is ErdBoardAction.AddField -> nodeId
        is ErdBoardAction.RemoveField -> nodeId
        is ErdBoardAction.RenameField -> nodeId
        is ErdBoardAction.AddEdge,
        is ErdBoardAction.DeleteEdge,
        -> null
    }

    private fun nextActionId(): String {
        actionSequence += 1
        return "remote-action-$actionSequence"
    }

    private companion object {
        const val DEFAULT_USER_ID = "unknown_user"
    }
}
