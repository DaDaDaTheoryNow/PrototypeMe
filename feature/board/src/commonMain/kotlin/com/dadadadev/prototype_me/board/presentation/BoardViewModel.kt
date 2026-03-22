package com.dadadadev.prototype_me.board.presentation

import androidx.compose.ui.geometry.Offset
import com.dadadadev.prototype_me.core.mvi.BaseViewModel
import com.dadadadev.prototype_me.domain.models.BoardAction
import com.dadadadev.prototype_me.domain.models.BoardSyncEffect
import com.dadadadev.prototype_me.domain.models.EntityNode
import com.dadadadev.prototype_me.domain.models.FieldType
import com.dadadadev.prototype_me.domain.models.NodeField
import com.dadadadev.prototype_me.domain.models.Position
import com.dadadadev.prototype_me.domain.models.RelationEdge
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
                    val merged = mergeNodes(state.nodes, remote.nodes, state.locallyMovedNodeIds)
                    // Remove from locallyMovedNodeIds once server position matches local
                    val caughtUp = state.locallyMovedNodeIds.filter { id ->
                        val local = state.nodes[id]
                        val rem = remote.nodes[id]
                        local != null && rem != null &&
                            local.position.x == rem.position.x &&
                            local.position.y == rem.position.y
                    }.toSet()
                    state.copy(
                        nodes = merged,
                        edges = remote.edges,
                        locallyMovedNodeIds = state.locallyMovedNodeIds - caughtUp
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

            is BoardIntent.OnPan -> reduce {
                state.copy(panOffset = state.panOffset + boardIntent.delta)
            }

            // ── Node drag ─────────────────────────────────────────────────────
            is BoardIntent.OnDragStart -> {
                reduce { state.copy(locallyMovedNodeIds = state.locallyMovedNodeIds + boardIntent.nodeId) }
                repository.requestLock(boardIntent.nodeId)
            }

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

            // ── Add / delete node ─────────────────────────────────────────────
            is BoardIntent.OnAddNode -> {
                val nodeId = uuid()
                val node = EntityNode(
                    id = nodeId,
                    name = boardIntent.name.ifBlank { "Node" },
                    position = boardIntent.position,
                    fields = listOf(NodeField(uuid(), "id", FieldType.NUMBER))
                )
                reduce { state.copy(nodes = state.nodes + (nodeId to node)) }
                repository.sendAction(BoardAction.AddNode(node, uuid()))
            }

            is BoardIntent.OnDeleteNode -> {
                val nodeId = boardIntent.nodeId
                val cleanedEdges = state.edges.filter { (_, e) ->
                    e.sourceNodeId != nodeId && e.targetNodeId != nodeId
                }
                reduce {
                    state.copy(
                        nodes = state.nodes - nodeId,
                        edges = cleanedEdges,
                        nodeMenuNodeId = null,
                        selectedEdgeId = if (cleanedEdges.containsKey(state.selectedEdgeId)) state.selectedEdgeId else null
                    )
                }
                repository.sendAction(BoardAction.DeleteNode(nodeId, uuid()))
            }

            // ── Tap-to-connect ────────────────────────────────────────────────
            is BoardIntent.OnNodeTap -> {
                val nodeId = boardIntent.nodeId
                val sourceId = state.connectingFromNodeId
                when {
                    sourceId == null -> reduce {
                        state.copy(connectingFromNodeId = nodeId, connectingFromFieldId = null, nodeMenuNodeId = null)
                    }
                    sourceId == nodeId -> reduce {
                        state.copy(connectingFromNodeId = null, connectingFromFieldId = null)
                    }
                    else -> createEdge(sourceId, state.connectingFromFieldId, nodeId, null)
                }
            }

            is BoardIntent.OnNodeFieldTap -> {
                val nodeId = boardIntent.nodeId
                val fieldId = boardIntent.fieldId
                val sourceId = state.connectingFromNodeId
                when {
                    sourceId == null -> reduce {
                        state.copy(connectingFromNodeId = nodeId, connectingFromFieldId = fieldId, nodeMenuNodeId = null)
                    }
                    sourceId == nodeId -> reduce {
                        state.copy(connectingFromNodeId = null, connectingFromFieldId = null)
                    }
                    else -> createEdge(sourceId, state.connectingFromFieldId, nodeId, fieldId)
                }
            }

            is BoardIntent.OnCancelConnect -> reduce {
                state.copy(connectingFromNodeId = null, connectingFromFieldId = null)
            }

            // ── Drag-to-connect ────────────────────────────────────────────────
            is BoardIntent.OnEdgeDragStart -> reduce {
                state.copy(
                    draggingEdgeFromNodeId = boardIntent.nodeId,
                    draggingEdgeFromFieldId = boardIntent.fieldId,
                    draggingEdgeCurrentPos = null,
                    connectingFromNodeId = null,
                    connectingFromFieldId = null,
                    nodeMenuNodeId = null,
                )
            }

            is BoardIntent.OnEdgeDragMove -> reduce {
                state.copy(draggingEdgeCurrentPos = boardIntent.screenPos)
            }

            is BoardIntent.OnEdgeDragEnd -> {
                val fromNodeId = state.draggingEdgeFromNodeId
                val toNodeId = boardIntent.targetNodeId
                if (fromNodeId != null && toNodeId != null && fromNodeId != toNodeId) {
                    createEdge(fromNodeId, state.draggingEdgeFromFieldId, toNodeId, boardIntent.targetFieldId)
                }
                reduce {
                    state.copy(
                        draggingEdgeFromNodeId = null,
                        draggingEdgeFromFieldId = null,
                        draggingEdgeCurrentPos = null,
                    )
                }
            }

            // ── Edge interaction ──────────────────────────────────────────────
            is BoardIntent.OnSelectEdge -> reduce {
                state.copy(selectedEdgeId = boardIntent.edgeId, nodeMenuNodeId = null)
            }

            is BoardIntent.OnDeleteEdge -> {
                reduce { state.copy(edges = state.edges - boardIntent.edgeId, selectedEdgeId = null) }
                repository.sendAction(BoardAction.DeleteEdge(boardIntent.edgeId, uuid()))
            }

            // ── Node selection menu ───────────────────────────────────────────
            is BoardIntent.OnNodeMenu -> reduce {
                state.copy(nodeMenuNodeId = boardIntent.nodeId, selectedEdgeId = null)
            }

            // ── Field editor ──────────────────────────────────────────────────
            is BoardIntent.OnSelectNode -> reduce {
                state.copy(selectedNodeId = boardIntent.nodeId, nodeMenuNodeId = null)
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
                reduce { state.copy(nodes = state.nodes + (boardIntent.nodeId to node.copy(fields = updatedFields))) }
                repository.sendAction(
                    BoardAction.RenameField(boardIntent.nodeId, boardIntent.fieldId, boardIntent.newName, boardIntent.newType, uuid())
                )
            }
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun createEdge(
        sourceNodeId: String, sourceFieldId: String?,
        targetNodeId: String, targetFieldId: String?,
    ) = intent {
        val edgeId = uuid()
        val edge = RelationEdge(
            id = edgeId,
            sourceNodeId = sourceNodeId,
            sourceFieldId = sourceFieldId,
            targetNodeId = targetNodeId,
            targetFieldId = targetFieldId,
        )
        reduce {
            state.copy(
                edges = state.edges + (edgeId to edge),
                connectingFromNodeId = null,
                connectingFromFieldId = null,
                selectedEdgeId = edgeId,
            )
        }
        repository.sendAction(BoardAction.AddEdge(edge, uuid()))
    }

    private fun mergeNodes(
        local: Map<String, EntityNode>,
        remote: Map<String, EntityNode>,
        locallyMovedNodeIds: Set<String>,
    ): Map<String, EntityNode> = remote.mapValues { (id, remoteNode) ->
        val localNode = local[id]
        if (localNode != null && id in locallyMovedNodeIds) {
            // Node was recently dragged by us — keep our local position,
            // take everything else (fields, lockedBy) from server
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
