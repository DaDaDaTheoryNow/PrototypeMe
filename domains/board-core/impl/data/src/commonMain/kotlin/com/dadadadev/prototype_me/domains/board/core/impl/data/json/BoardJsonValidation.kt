package com.dadadadev.prototype_me.domains.board.core.impl.data.json

import com.dadadadev.prototype_me.domains.board.core.api.domain.model.BoardEdge
import com.dadadadev.prototype_me.domains.board.core.api.domain.model.BoardEntity
import com.dadadadev.prototype_me.domains.board.core.api.domain.model.BoardSnapshot

fun interface BoardJsonSnapshotValidator<TNode : BoardEntity, TEdge : BoardEdge> {
    fun validate(snapshot: BoardSnapshot<TNode, TEdge>)
}

internal open class BoardJsonException(message: String) : IllegalArgumentException(message)

internal class BoardJsonFormatMismatchException(actual: String) :
    BoardJsonException("Unsupported board JSON format: $actual")

internal class BoardJsonVersionMismatchException(actual: Int) :
    BoardJsonException("Unsupported board JSON version: $actual")

internal class BoardJsonTypeMismatchException(
    expected: String,
    actual: String,
) : BoardJsonException("Expected boardType=$expected but got $actual")

internal class BoardJsonDuplicateNodeIdException(ids: Set<String>) :
    BoardJsonException("Duplicate node ids in JSON: ${ids.joinToString()}")

internal class BoardJsonDuplicateEdgeIdException(ids: Set<String>) :
    BoardJsonException("Duplicate edge ids in JSON: ${ids.joinToString()}")

internal class BoardJsonBrokenEdgeReferenceException(edgeIds: List<String>) :
    BoardJsonException("Edges reference unknown nodes: ${edgeIds.joinToString()}")
