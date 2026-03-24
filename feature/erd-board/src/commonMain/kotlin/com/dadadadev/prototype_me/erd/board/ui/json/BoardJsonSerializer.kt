package com.dadadadev.prototype_me.erd.board.ui.json

import com.dadadadev.prototype_me.domains.erd.design.api.domain.model.EntityNode
import com.dadadadev.prototype_me.domains.erd.design.api.domain.model.FieldType
import com.dadadadev.prototype_me.domains.erd.design.api.domain.model.NodeField
import com.dadadadev.prototype_me.domains.erd.design.api.domain.model.Position
import com.dadadadev.prototype_me.domains.erd.design.api.domain.model.RelationEdge
import com.dadadadev.prototype_me.erd.board.presentation.contract.ErdBoardState
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val boardJson = Json {
    prettyPrint = true
    encodeDefaults = false
    ignoreUnknownKeys = true
}

// ── Export ────────────────────────────────────────────────────────────────────

fun ErdBoardState.toJsonString(): String = boardJson.encodeToString(toSnapshot())

private fun ErdBoardState.toSnapshot(): BoardSnapshot = BoardSnapshot(
    nodes = nodes.values.map { it.toNodeSnapshot() },
    edges = edges.values.map { it.toEdgeSnapshot() },
)

private fun EntityNode.toNodeSnapshot(): NodeSnapshot = NodeSnapshot(
    id = id,
    name = name,
    x = position.x,
    y = position.y,
    fields = fields.map { field ->
        FieldSnapshot(id = field.id, name = field.name, type = field.type.name)
    },
)

private fun RelationEdge.toEdgeSnapshot(): EdgeSnapshot = EdgeSnapshot(
    id = id,
    sourceNodeId = sourceNodeId,
    sourceFieldId = sourceFieldId,
    targetNodeId = targetNodeId,
    targetFieldId = targetFieldId,
    label = label,
)

// ── Import ────────────────────────────────────────────────────────────────────

/**
 * Parses a JSON string produced by [toJsonString] back into domain objects.
 * Returns null if the JSON is invalid or cannot be parsed.
 */
fun parseJsonToBoard(json: String): Pair<List<EntityNode>, List<RelationEdge>>? = runCatching {
    boardJson.decodeFromString<BoardSnapshot>(json).toDomain()
}.getOrNull()

private fun BoardSnapshot.toDomain(): Pair<List<EntityNode>, List<RelationEdge>> {
    val domainNodes = nodes.map { snap ->
        EntityNode(
            id = snap.id,
            name = snap.name,
            position = Position(x = snap.x, y = snap.y),
            fields = snap.fields.map { fieldSnap ->
                NodeField(
                    id = fieldSnap.id,
                    name = fieldSnap.name,
                    type = FieldType.entries.firstOrNull { it.name == fieldSnap.type } ?: FieldType.TEXT,
                )
            },
        )
    }
    val domainEdges = edges.map { snap ->
        RelationEdge(
            id = snap.id,
            sourceNodeId = snap.sourceNodeId,
            sourceFieldId = snap.sourceFieldId,
            targetNodeId = snap.targetNodeId,
            targetFieldId = snap.targetFieldId,
            label = snap.label,
        )
    }
    return domainNodes to domainEdges
}
