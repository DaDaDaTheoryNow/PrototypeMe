package com.dadadadev.prototype_me.domains.board.core.impl.data.json

import kotlinx.serialization.Serializable

internal const val BOARD_JSON_FORMAT = "prototype_me.board"
internal const val BOARD_JSON_VERSION = 1

@Serializable
internal data class BoardJsonDocument<TNodePayload, TEdgePayload>(
    val format: String = BOARD_JSON_FORMAT,
    val version: Int = BOARD_JSON_VERSION,
    val boardType: String,
    val nodes: List<BoardJsonNode<TNodePayload>>,
    val edges: List<BoardJsonEdge<TEdgePayload>>,
)

@Serializable
internal data class BoardJsonNode<TPayload>(
    val id: String,
    val position: BoardJsonPoint,
    val payload: TPayload,
)

@Serializable
internal data class BoardJsonEdge<TPayload>(
    val id: String,
    val sourceId: String,
    val targetId: String,
    val payload: TPayload,
)

@Serializable
internal data class BoardJsonPoint(
    val x: Float,
    val y: Float,
)
