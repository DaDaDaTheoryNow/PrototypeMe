package com.dadadadev.prototype_me.domains.erd.design.impl.data.mock

import com.dadadadev.prototype_me.domains.board.core.api.domain.model.BoardSyncEffect
import com.dadadadev.prototype_me.domains.erd.design.api.domain.model.ErdBoardAction
import com.dadadadev.prototype_me.domains.erd.design.api.domain.model.ErdBoardContext
import com.dadadadev.prototype_me.domains.erd.design.api.domain.model.withFields
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

class MockBoardServer {

    private val _stateFlow = MutableStateFlow(InitialMockData.create())
    val serverStateFlow: StateFlow<ErdBoardContext> = _stateFlow.asStateFlow()

    private val _effects = MutableSharedFlow<BoardSyncEffect>(extraBufferCapacity = 16)
    val effectsFlow: SharedFlow<BoardSyncEffect> = _effects.asSharedFlow()

    suspend fun processAction(action: ErdBoardAction, userId: String) {
        processActions(listOf(action), userId)
    }

    suspend fun processActions(actions: List<ErdBoardAction>, userId: String) {
        if (actions.isEmpty()) return
        delay(NETWORK_DELAY_MS)
        val updatedContext = actions.fold(_stateFlow.value) { context, action ->
            applyAction(context, action)
        }
        _stateFlow.value = updatedContext
    }

    suspend fun requestLock(nodeId: String, userId: String) {
        delay(LOCK_DELAY_MS)
        val ctx = _stateFlow.value
        val node = ctx.nodes[nodeId] ?: return
        if (node.lockedBy == null) {
            _stateFlow.value = ctx.copy(
                nodes = ctx.nodes + (nodeId to node.copy(lockedBy = userId)),
            )
        } else {
            _effects.emit(BoardSyncEffect.LockRejected(nodeId, node.lockedBy!!))
        }
    }

    suspend fun releaseLock(nodeId: String, userId: String) {
        delay(LOCK_DELAY_MS)
        val ctx = _stateFlow.value
        val node = ctx.nodes[nodeId] ?: return
        if (node.lockedBy == userId) {
            _stateFlow.value = ctx.copy(
                nodes = ctx.nodes + (nodeId to node.copy(lockedBy = null)),
            )
        }
    }

    private fun applyAction(context: ErdBoardContext, action: ErdBoardAction): ErdBoardContext = when (action) {
        is ErdBoardAction.MoveNode -> {
            val node = context.nodes[action.nodeId] ?: return context
            context.copy(
                nodes = context.nodes + (action.nodeId to node.copy(position = action.newPosition)),
            )
        }

        is ErdBoardAction.AddNode -> {
            context.copy(nodes = context.nodes + (action.node.id to action.node))
        }

        is ErdBoardAction.DeleteNode -> {
            val remainingEdges = context.edges.filter { (_, edge) ->
                edge.sourceNodeId != action.nodeId && edge.targetNodeId != action.nodeId
            }
            context.copy(nodes = context.nodes - action.nodeId, edges = remainingEdges)
        }

        is ErdBoardAction.AddEdge -> {
            context.copy(edges = context.edges + (action.edge.id to action.edge))
        }

        is ErdBoardAction.AddField -> {
            val node = context.nodes[action.nodeId] ?: return context
            context.copy(
                nodes = context.nodes + (action.nodeId to node.withFields(node.fields + action.field)),
            )
        }

        is ErdBoardAction.RemoveField -> {
            val node = context.nodes[action.nodeId] ?: return context
            context.copy(
                nodes = context.nodes + (
                    action.nodeId to node.withFields(node.fields.filter { it.id != action.fieldId })
                ),
            )
        }

        is ErdBoardAction.RenameField -> {
            val node = context.nodes[action.nodeId] ?: return context
            val updatedFields = node.fields.map { field ->
                if (field.id == action.fieldId) {
                    field.copy(name = action.newName, type = action.newType)
                } else {
                    field
                }
            }
            context.copy(
                nodes = context.nodes + (action.nodeId to node.withFields(updatedFields)),
            )
        }

        is ErdBoardAction.DeleteEdge -> {
            context.copy(edges = context.edges - action.edgeId)
        }
    }

    private companion object {
        const val NETWORK_DELAY_MS = 300L
        const val LOCK_DELAY_MS = 150L
    }
}


