package com.dadadadev.prototype_me.domains.board.core.impl.data.json

import kotlinx.serialization.Serializable

const val BOARD_JSON_FORMAT = "prototype_me.board"
const val BOARD_JSON_VERSION = 1

@Serializable
data class BoardJsonDocument<TNodePayload, TEdgePayload>(
    val format: String = BOARD_JSON_FORMAT,
    val version: Int = BOARD_JSON_VERSION,
    val boardType: String,
    val nodes: List<BoardJsonNode<TNodePayload>>,
    val edges: List<BoardJsonEdge<TEdgePayload>>,
)

@Serializable
data class BoardJsonNode<TPayload>(
    val id: String,
    val position: BoardJsonPoint,
    val payload: TPayload,
)

@Serializable
data class BoardJsonEdge<TPayload>(
    val id: String,
    val sourceId: String,
    val targetId: String,
    val payload: TPayload,
)

@Serializable
data class BoardJsonPoint(
    val x: Float,
    val y: Float,
)
