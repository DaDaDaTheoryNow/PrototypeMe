package com.dadadadev.prototype_me.domains.erd.design.impl.data.mock

import com.dadadadev.prototype_me.domains.erd.design.api.domain.model.BoardAction
import com.dadadadev.prototype_me.domains.erd.design.api.domain.model.BoardContext
import com.dadadadev.prototype_me.domains.board.core.api.domain.model.BoardSyncEffect
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

class MockBoardServer {

    private val _stateFlow = MutableStateFlow(InitialMockData.create())
    val serverStateFlow: StateFlow<BoardContext> = _stateFlow.asStateFlow()

    private val _effects = MutableSharedFlow<BoardSyncEffect>(extraBufferCapacity = 16)
    val effectsFlow: SharedFlow<BoardSyncEffect> = _effects.asSharedFlow()

    suspend fun processAction(action: BoardAction, userId: String) {
        delay(NETWORK_DELAY_MS)
        val ctx = _stateFlow.value
        _stateFlow.value = when (action) {
            is BoardAction.MoveNode -> {
                val node = ctx.nodes[action.nodeId] ?: return
                ctx.copy(nodes = ctx.nodes + (action.nodeId to node.copy(position = action.newPosition)))
            }

            is BoardAction.AddNode -> {
                ctx.copy(nodes = ctx.nodes + (action.node.id to action.node))
            }

            is BoardAction.DeleteNode -> {
                val remainingEdges = ctx.edges.filter { (_, e) ->
                    e.sourceNodeId != action.nodeId && e.targetNodeId != action.nodeId
                }
                ctx.copy(nodes = ctx.nodes - action.nodeId, edges = remainingEdges)
            }

            is BoardAction.AddEdge -> {
                ctx.copy(edges = ctx.edges + (action.edge.id to action.edge))
            }

            is BoardAction.AddField -> {
                val node = ctx.nodes[action.nodeId] ?: return
                ctx.copy(nodes = ctx.nodes + (action.nodeId to node.copy(fields = node.fields + action.field)))
            }

            is BoardAction.RemoveField -> {
                val node = ctx.nodes[action.nodeId] ?: return
                ctx.copy(nodes = ctx.nodes + (action.nodeId to node.copy(fields = node.fields.filter { it.id != action.fieldId })))
            }

            is BoardAction.RenameField -> {
                val node = ctx.nodes[action.nodeId] ?: return
                val updatedFields = node.fields.map {
                    if (it.id == action.fieldId) {
                        it.copy(name = action.newName, type = action.newType)
                    } else {
                        it
                    }
                }
                ctx.copy(nodes = ctx.nodes + (action.nodeId to node.copy(fields = updatedFields)))
            }

            is BoardAction.DeleteEdge -> {
                ctx.copy(edges = ctx.edges - action.edgeId)
            }
        }
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

    private companion object {
        const val NETWORK_DELAY_MS = 300L
        const val LOCK_DELAY_MS = 150L
    }
}


