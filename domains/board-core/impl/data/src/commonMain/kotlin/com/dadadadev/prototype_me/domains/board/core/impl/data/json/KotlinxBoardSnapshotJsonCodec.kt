package com.dadadadev.prototype_me.domains.board.core.impl.data.json

import com.dadadadev.prototype_me.domains.board.core.api.data.codec.BoardSnapshotJsonCodec
import com.dadadadev.prototype_me.domains.board.core.api.domain.model.BoardEdge
import com.dadadadev.prototype_me.domains.board.core.api.domain.model.BoardEntity
import com.dadadadev.prototype_me.domains.board.core.api.domain.model.BoardPoint
import com.dadadadev.prototype_me.domains.board.core.api.domain.model.BoardSnapshot
import kotlinx.serialization.KSerializer
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

internal val defaultBoardJson: Json = Json {
    prettyPrint = true
    encodeDefaults = false
    ignoreUnknownKeys = true
}

interface BoardJsonPayloadAdapter<TNode : BoardEntity, TEdge : BoardEdge, TNodePayload, TEdgePayload> {
    val boardType: String

    fun nodePayloadOf(node: TNode): TNodePayload
    fun edgePayloadOf(edge: TEdge): TEdgePayload

    fun nodeFromJson(
        id: String,
        position: BoardPoint,
        payload: TNodePayload,
    ): TNode

    fun edgeFromJson(
        id: String,
        sourceId: String,
        targetId: String,
        payload: TEdgePayload,
    ): TEdge
}

class KotlinxBoardSnapshotJsonCodec<TNode : BoardEntity, TEdge : BoardEdge, TNodePayload, TEdgePayload>(
    private val nodePayloadSerializer: KSerializer<TNodePayload>,
    private val edgePayloadSerializer: KSerializer<TEdgePayload>,
    private val adapter: BoardJsonPayloadAdapter<TNode, TEdge, TNodePayload, TEdgePayload>,
    private val validator: BoardJsonSnapshotValidator<TNode, TEdge> = BoardJsonSnapshotValidator {},
    private val json: Json = defaultBoardJson,
) : BoardSnapshotJsonCodec<TNode, TEdge> {

    private val documentSerializer =
        BoardJsonDocument.serializer(nodePayloadSerializer, edgePayloadSerializer)

    override fun encode(snapshot: BoardSnapshot<TNode, TEdge>): String = json.encodeToString(
        serializer = documentSerializer,
        value = snapshot.toDocument(),
    )

    override fun decode(rawJson: String): Result<BoardSnapshot<TNode, TEdge>> = runCatching {
        val document = json.decodeFromString(documentSerializer, rawJson)
        validate(document)
        validateUniqueIds(document)

        BoardSnapshot(
            entities = document.nodes
                .map { node ->
                    adapter.nodeFromJson(
                        id = node.id,
                        position = BoardPoint(node.position.x, node.position.y),
                        payload = node.payload,
                    )
                }
                .associateBy(BoardEntity::id),
            edges = document.edges
                .map { edge ->
                    adapter.edgeFromJson(
                        id = edge.id,
                        sourceId = edge.sourceId,
                        targetId = edge.targetId,
                        payload = edge.payload,
                    )
                }
                .associateBy(BoardEdge::id),
        ).also(::validateSnapshot)
    }

    private fun BoardSnapshot<TNode, TEdge>.toDocument(): BoardJsonDocument<TNodePayload, TEdgePayload> =
        BoardJsonDocument(
            boardType = adapter.boardType,
            nodes = entities.values.map { node ->
                BoardJsonNode(
                    id = node.id,
                    position = BoardJsonPoint(
                        x = node.position.x,
                        y = node.position.y,
                    ),
                    payload = adapter.nodePayloadOf(node),
                )
            },
            edges = edges.values.map { edge ->
                BoardJsonEdge(
                    id = edge.id,
                    sourceId = edge.sourceId,
                    targetId = edge.targetId,
                    payload = adapter.edgePayloadOf(edge),
                )
            },
        )

    private fun validate(document: BoardJsonDocument<TNodePayload, TEdgePayload>) {
        if (document.format != BOARD_JSON_FORMAT) {
            throw BoardJsonFormatMismatchException(document.format)
        }
        if (document.version != BOARD_JSON_VERSION) {
            throw BoardJsonVersionMismatchException(document.version)
        }
        if (document.boardType != adapter.boardType) {
            throw BoardJsonTypeMismatchException(
                expected = adapter.boardType,
                actual = document.boardType,
            )
        }
    }

    private fun validateUniqueIds(document: BoardJsonDocument<TNodePayload, TEdgePayload>) {
        val duplicateNodeIds = document.nodes.findDuplicateIds { it.id }
        if (duplicateNodeIds.isNotEmpty()) {
            throw BoardJsonDuplicateNodeIdException(duplicateNodeIds)
        }

        val duplicateEdgeIds = document.edges.findDuplicateIds { it.id }
        if (duplicateEdgeIds.isNotEmpty()) {
            throw BoardJsonDuplicateEdgeIdException(duplicateEdgeIds)
        }
    }

    private fun validateSnapshot(snapshot: BoardSnapshot<TNode, TEdge>) {
        val brokenEdgeIds = snapshot.edges.values
            .filter { edge -> edge.sourceId !in snapshot.entities || edge.targetId !in snapshot.entities }
            .map(BoardEdge::id)

        if (brokenEdgeIds.isNotEmpty()) {
            throw BoardJsonBrokenEdgeReferenceException(brokenEdgeIds)
        }

        validator.validate(snapshot)
    }
}

private fun <T> List<T>.findDuplicateIds(idSelector: (T) -> String): Set<String> = buildSet {
    val seenIds = mutableSetOf<String>()
    for (item in this@findDuplicateIds) {
        val id = idSelector(item)
        if (!seenIds.add(id)) {
            add(id)
        }
    }
}
