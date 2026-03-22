package com.dadadadev.prototype_me.board.presentation

import androidx.compose.ui.geometry.Offset
import com.dadadadev.prototype_me.core.mvi.BaseViewModel
import com.dadadadev.prototype_me.domain.models.BoardAction
import com.dadadadev.prototype_me.domain.models.BoardSyncEffect
import com.dadadadev.prototype_me.domain.models.EntityNode
import com.dadadadev.prototype_me.domain.models.NodeField
import com.dadadadev.prototype_me.domain.models.Position
import com.dadadadev.prototype_me.domain.models.RelationEdge
import com.dadadadev.prototype_me.domain.models.RelationType
import com.dadadadev.prototype_me.domain.repository.BoardRepository
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class BoardViewModel(
    private val repository: BoardRepository
) : BaseViewModel<BoardState, BoardSideEffect>(BoardState()) {

    init {
        intent { repository.connect("board_1", state.currentUserId) }

        intent {
            repository.observeBoardState("board_1").collect { remote ->
                reduce {
                    state.copy(
                        nodes = mergeNodes(state.nodes, remote.nodes, state.currentUserId),
                        edges = remote.edges
                    )
                }
            }
        }

        intent {
            repository.observeSideEffects().collect { effect ->
                when (effect) {
                    is BoardSyncEffect.LockRejected ->
                        postSideEffect(BoardSideEffect.ShowLockError(effect.nodeId, effect.lockedBy))
                    is BoardSyncEffect.ConnectionLost ->
                        postSideEffect(BoardSideEffect.ShowConnectionLost)
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        intent { repository.disconnect() }
    }

    fun onIntent(boardIntent: BoardIntent) = intent {
        when (boardIntent) {

            // ── Canvas ────────────────────────────────────────────────────────
            is BoardIntent.OnPanZoom -> reduce {
                val newScale = (state.scale * boardIntent.zoom).coerceIn(MIN_SCALE, MAX_SCALE)
                val f = newScale / state.scale
                state.copy(
                    scale = newScale,
                    panOffset = Offset(
                        boardIntent.centroid.x - f * (boardIntent.centroid.x - state.panOffset.x) + boardIntent.pan.x,
                        boardIntent.centroid.y - f * (boardIntent.centroid.y - state.panOffset.y) + boardIntent.pan.y
                    )
                )
            }

            // ── Drag ──────────────────────────────────────────────────────────
            is BoardIntent.OnDragStart -> repository.requestLock(boardIntent.nodeId)

            is BoardIntent.OnDragNode -> {
                val node = state.nodes[boardIntent.nodeId] ?: return@intent
                val newPos = Position(
                    node.position.x + boardIntent.delta.x / state.scale,
                    node.position.y + boardIntent.delta.y / state.scale
                )
                reduce { state.copy(nodes = state.nodes + (boardIntent.nodeId to node.copy(position = newPos))) }
                repository.sendAction(BoardAction.MoveNode(boardIntent.nodeId, newPos, uuid()))
            }

            is BoardIntent.OnDragEnd -> repository.releaseLock(boardIntent.nodeId)

            // ── Add node ──────────────────────────────────────────────────────
            is BoardIntent.OnAddNode -> {
                val nodeId = uuid()
                val node = EntityNode(nodeId, boardIntent.name.ifBlank { "Node" }, boardIntent.position)
                reduce { state.copy(nodes = state.nodes + (nodeId to node)) }
                repository.sendAction(BoardAction.AddNode(node, uuid()))
            }

            // ── Connect: port dot taps ─────────────────────────────────────────
            is BoardIntent.OnNodeTap -> {
                val nodeId = boardIntent.nodeId
                val sourceId = state.connectingFromNodeId
                when {
                    sourceId == null -> reduce {
                        state.copy(connectingFromNodeId = nodeId, connectingFromFieldId = null)
                    }
                    sourceId == nodeId -> reduce {
                        state.copy(connectingFromNodeId = null, connectingFromFieldId = null)
                    }
                    else -> {
                        val edgeId = uuid()
                        val edge = RelationEdge(
                            id = edgeId,
                            sourceNodeId = sourceId,
                            sourceFieldId = state.connectingFromFieldId,
                            targetNodeId = nodeId,
                            targetFieldId = null,
                            type = RelationType.ONE_TO_MANY
                        )
                        reduce {
                            state.copy(
                                edges = state.edges + (edgeId to edge),
                                connectingFromNodeId = null,
                                connectingFromFieldId = null
                            )
                        }
                        repository.sendAction(BoardAction.AddEdge(edge, uuid()))
                    }
                }
            }

            is BoardIntent.OnNodeFieldTap -> {
                val nodeId = boardIntent.nodeId
                val fieldId = boardIntent.fieldId
                val sourceId = state.connectingFromNodeId
                when {
                    sourceId == null -> reduce {
                        state.copy(connectingFromNodeId = nodeId, connectingFromFieldId = fieldId)
                    }
                    sourceId == nodeId -> reduce {
                        state.copy(connectingFromNodeId = null, connectingFromFieldId = null)
                    }
                    else -> {
                        val edgeId = uuid()
                        val edge = RelationEdge(
                            id = edgeId,
                            sourceNodeId = sourceId,
                            sourceFieldId = state.connectingFromFieldId,
                            targetNodeId = nodeId,
                            targetFieldId = fieldId,
                            type = RelationType.ONE_TO_MANY
                        )
                        reduce {
                            state.copy(
                                edges = state.edges + (edgeId to edge),
                                connectingFromNodeId = null,
                                connectingFromFieldId = null
                            )
                        }
                        repository.sendAction(BoardAction.AddEdge(edge, uuid()))
                    }
                }
            }

            is BoardIntent.OnCancelConnect -> reduce {
                state.copy(connectingFromNodeId = null, connectingFromFieldId = null)
            }

            // ── Edge interaction ──────────────────────────────────────────────
            is BoardIntent.OnSelectEdge -> reduce {
                state.copy(selectedEdgeId = boardIntent.edgeId)
            }

            is BoardIntent.OnDeleteEdge -> {
                reduce { state.copy(edges = state.edges - boardIntent.edgeId, selectedEdgeId = null) }
                repository.sendAction(BoardAction.DeleteEdge(boardIntent.edgeId, uuid()))
            }

            is BoardIntent.OnChangeEdgeType -> {
                val edge = state.edges[boardIntent.edgeId] ?: return@intent
                val updated = edge.copy(type = boardIntent.type)
                reduce { state.copy(edges = state.edges + (boardIntent.edgeId to updated)) }
                repository.sendAction(BoardAction.ChangeEdgeType(boardIntent.edgeId, boardIntent.type, uuid()))
            }

            // ── Field editing ─────────────────────────────────────────────────
            is BoardIntent.OnSelectNode -> reduce {
                state.copy(selectedNodeId = boardIntent.nodeId)
            }

            is BoardIntent.OnAddField -> {
                val node = state.nodes[boardIntent.nodeId] ?: return@intent
                val field = NodeField(uuid(), boardIntent.name, boardIntent.type)
                val updated = node.copy(fields = node.fields + field)
                reduce { state.copy(nodes = state.nodes + (boardIntent.nodeId to updated)) }
                repository.sendAction(BoardAction.AddField(boardIntent.nodeId, field, uuid()))
            }

            is BoardIntent.OnRemoveField -> {
                val node = state.nodes[boardIntent.nodeId] ?: return@intent
                val updated = node.copy(fields = node.fields.filter { it.id != boardIntent.fieldId })
                reduce { state.copy(nodes = state.nodes + (boardIntent.nodeId to updated)) }
                repository.sendAction(BoardAction.RemoveField(boardIntent.nodeId, boardIntent.fieldId, uuid()))
            }

            is BoardIntent.OnRenameField -> {
                val node = state.nodes[boardIntent.nodeId] ?: return@intent
                val updatedFields = node.fields.map {
                    if (it.id == boardIntent.fieldId) it.copy(name = boardIntent.newName, type = boardIntent.newType) else it
                }
                val updated = node.copy(fields = updatedFields)
                reduce { state.copy(nodes = state.nodes + (boardIntent.nodeId to updated)) }
                repository.sendAction(
                    BoardAction.RenameField(boardIntent.nodeId, boardIntent.fieldId, boardIntent.newName, boardIntent.newType, uuid())
                )
            }
        }
    }

    private fun mergeNodes(
        local: Map<String, EntityNode>,
        remote: Map<String, EntityNode>,
        currentUserId: String
    ): Map<String, EntityNode> = remote.mapValues { (id, remoteNode) ->
        val localNode = local[id]
        if (localNode != null && remoteNode.lockedBy == currentUserId) {
            remoteNode.copy(position = localNode.position)
        } else remoteNode
    }

    private companion object {
        const val MIN_SCALE = 0.2f
        const val MAX_SCALE = 5f

        @OptIn(ExperimentalUuidApi::class)
        fun uuid(): String = Uuid.random().toString()
    }
}
