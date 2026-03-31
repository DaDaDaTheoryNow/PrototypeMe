package com.dadadadev.prototype_me.domains.erd.design.impl.data.json

import com.dadadadev.prototype_me.domains.board.core.api.domain.model.BoardPoint
import com.dadadadev.prototype_me.domains.board.core.api.domain.model.BoardSnapshot
import com.dadadadev.prototype_me.domains.board.core.impl.data.json.BoardJsonPayloadAdapter
import com.dadadadev.prototype_me.domains.board.core.impl.data.json.BoardJsonSnapshotValidator
import com.dadadadev.prototype_me.domains.board.core.impl.data.json.KotlinxBoardSnapshotJsonCodec
import com.dadadadev.prototype_me.domains.erd.design.api.data.codec.ErdBoardJsonCodec
import com.dadadadev.prototype_me.domains.erd.design.api.domain.model.ErdEntityNode
import com.dadadadev.prototype_me.domains.erd.design.api.domain.model.FieldType
import com.dadadadev.prototype_me.domains.erd.design.api.domain.model.ErdNodeField
import com.dadadadev.prototype_me.domains.erd.design.api.domain.model.Position
import com.dadadadev.prototype_me.domains.erd.design.api.domain.model.ErdRelationEdge
import kotlinx.serialization.Serializable

private const val ERD_BOARD_JSON_TYPE = "erd"

@Serializable
private data class ErdNodePayload(
    val name: String,
    val fields: List<ErdFieldPayload> = emptyList(),
)

@Serializable
private data class ErdFieldPayload(
    val id: String,
    val name: String,
    val type: ErdFieldTypePayload,
)

@Serializable
private enum class ErdFieldTypePayload {
    TEXT,
    NUMBER,
    BOOLEAN,
    DATE,
}

@Serializable
private data class ErdEdgePayload(
    val sourceFieldId: String? = null,
    val targetFieldId: String? = null,
    val label: String? = null,
)

private object ErdBoardPayloadAdapter :
    BoardJsonPayloadAdapter<ErdEntityNode, ErdRelationEdge, ErdNodePayload, ErdEdgePayload> {

    override val boardType: String = ERD_BOARD_JSON_TYPE

    override fun nodePayloadOf(node: ErdEntityNode): ErdNodePayload = ErdNodePayload(
        name = node.name,
        fields = node.fields.map { field ->
            ErdFieldPayload(
                id = field.id,
                name = field.name,
                type = field.type.toPayload(),
            )
        },
    )

    override fun edgePayloadOf(edge: ErdRelationEdge): ErdEdgePayload = ErdEdgePayload(
        sourceFieldId = edge.sourceFieldId,
        targetFieldId = edge.targetFieldId,
        label = edge.label,
    )

    override fun nodeFromJson(
        id: String,
        position: BoardPoint,
        payload: ErdNodePayload,
    ): ErdEntityNode = ErdEntityNode(
        id = id,
        name = payload.name,
        position = Position(position.x, position.y),
        fields = payload.fields.map { field ->
            ErdNodeField(
                id = field.id,
                name = field.name,
                type = field.type.toDomain(),
            )
        },
    )

    override fun edgeFromJson(
        id: String,
        sourceId: String,
        targetId: String,
        payload: ErdEdgePayload,
    ): ErdRelationEdge = ErdRelationEdge(
        id = id,
        sourceNodeId = sourceId,
        sourceFieldId = payload.sourceFieldId,
        targetNodeId = targetId,
        targetFieldId = payload.targetFieldId,
        label = payload.label,
    )
}

private val erdBoardJsonValidator = BoardJsonSnapshotValidator<ErdEntityNode, ErdRelationEdge> { snapshot ->
    snapshot.edges.values.forEach { edge ->
        val sourceNode = snapshot.entities[edge.sourceNodeId]
            ?: error("Unknown source node: ${edge.sourceNodeId}")
        val targetNode = snapshot.entities[edge.targetNodeId]
            ?: error("Unknown target node: ${edge.targetNodeId}")

        if (edge.sourceFieldId != null) {
            check(sourceNode.fields.any { it.id == edge.sourceFieldId }) {
                "Unknown source field: ${edge.sourceFieldId}"
            }
        }
        if (edge.targetFieldId != null) {
            check(targetNode.fields.any { it.id == edge.targetFieldId }) {
                "Unknown target field: ${edge.targetFieldId}"
            }
        }
    }
}

internal class ErdBoardJsonCodecImpl : ErdBoardJsonCodec {
    private val delegate = KotlinxBoardSnapshotJsonCodec(
        nodePayloadSerializer = ErdNodePayload.serializer(),
        edgePayloadSerializer = ErdEdgePayload.serializer(),
        adapter = ErdBoardPayloadAdapter,
        validator = erdBoardJsonValidator,
    )

    override fun encode(snapshot: BoardSnapshot<ErdEntityNode, ErdRelationEdge>): String =
        delegate.encode(snapshot)

    override fun decode(rawJson: String): Result<BoardSnapshot<ErdEntityNode, ErdRelationEdge>> =
        delegate.decode(rawJson)
}

private fun FieldType.toPayload(): ErdFieldTypePayload = when (this) {
    FieldType.TEXT -> ErdFieldTypePayload.TEXT
    FieldType.NUMBER -> ErdFieldTypePayload.NUMBER
    FieldType.BOOLEAN -> ErdFieldTypePayload.BOOLEAN
    FieldType.DATE -> ErdFieldTypePayload.DATE
}

private fun ErdFieldTypePayload.toDomain(): FieldType = when (this) {
    ErdFieldTypePayload.TEXT -> FieldType.TEXT
    ErdFieldTypePayload.NUMBER -> FieldType.NUMBER
    ErdFieldTypePayload.BOOLEAN -> FieldType.BOOLEAN
    ErdFieldTypePayload.DATE -> FieldType.DATE
}
