package com.dadadadev.prototype_me.erd.board.ui.canvas

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.util.lerp
import com.dadadadev.prototype_me.domains.board.core.api.domain.model.BoardPoint
import com.dadadadev.prototype_me.domains.erd.design.api.domain.model.ErdEntityNode
import com.dadadadev.prototype_me.erd.board.config.ErdBoardConfig
import com.dadadadev.prototype_me.erd.board.presentation.contract.ErdBoardState
import com.dadadadev.prototype_me.erd.board.ui.mappers.toOffset
import kotlin.math.abs

internal data class ErdBoardRenderData(
    val isConnecting: Boolean,
    val renderedNodes: Map<String, ErdEntityNode>,
    val edgeSideOrientations: Map<String, EdgeSideOrientation>,
    val portPositions: Map<PortKey, androidx.compose.ui.geometry.Offset>,
    val edgeHitPolylines: Map<String, List<androidx.compose.ui.geometry.Offset>>,
    val edgeMidpoints: Map<String, androidx.compose.ui.geometry.Offset?>,
    val highlightedFieldIds: Set<String>,
    val connectedFieldKeys: Set<String>,
)

@Composable
internal fun rememberErdBoardRenderData(
    state: ErdBoardState,
    density: Float,
): ErdBoardRenderData {
    val isConnecting = state.connectingFromNodeId != null || state.draggingEdgeFromNodeId != null
    val stableEdgeSideCache = remember { mutableMapOf<String, EdgeSideOrientation>() }
    val renderedNodes = rememberRenderedNodes(state)

    val edgeSideOrientations = remember(state.edges, renderedNodes) {
        buildStableEdgeSideOrientations(
            edges = state.edges.values,
            nodes = renderedNodes,
            previousOrientations = stableEdgeSideCache,
        ).also { resolvedOrientations ->
            stableEdgeSideCache.clear()
            stableEdgeSideCache.putAll(resolvedOrientations)
        }
    }

    val portPositions = remember(renderedNodes, state.scale, state.panOffset, density) {
        computeAllPortPositions(renderedNodes, state.scale, state.panOffset.toOffset(), density)
    }

    val edgeHitPolylines = remember(
        state.edges,
        renderedNodes,
        state.scale,
        state.panOffset,
        density,
        edgeSideOrientations,
    ) {
        buildEdgeHitPolylines(
            edges = state.edges.values.toList(),
            nodes = renderedNodes,
            scale = state.scale,
            panOffset = state.panOffset.toOffset(),
            density = density,
            edgeSideOrientations = edgeSideOrientations,
        )
    }

    val edgeMidpoints = remember(edgeHitPolylines) {
        edgeHitPolylines.mapValues { (_, points) -> points.getOrNull(points.size / 2) }
    }

    val highlightedFieldIds = remember(state.selectedEdgeId, state.edges) {
        val edge = state.edges[state.selectedEdgeId]
        buildSet {
            edge?.sourceFieldId?.let(::add)
            edge?.targetFieldId?.let(::add)
        }
    }

    val connectedFieldKeys = remember(state.edges) {
        buildSet {
            state.edges.values.forEach { edge ->
                edge.sourceFieldId?.let { add("${edge.sourceNodeId}:$it") }
                edge.targetFieldId?.let { add("${edge.targetNodeId}:$it") }
            }
        }
    }

    return ErdBoardRenderData(
        isConnecting = isConnecting,
        renderedNodes = renderedNodes,
        edgeSideOrientations = edgeSideOrientations,
        portPositions = portPositions,
        edgeHitPolylines = edgeHitPolylines,
        edgeMidpoints = edgeMidpoints,
        highlightedFieldIds = highlightedFieldIds,
        connectedFieldKeys = connectedFieldKeys,
    )
}

@Composable
private fun rememberRenderedNodes(state: ErdBoardState): Map<String, ErdEntityNode> {
    val renderedPositions = remember { mutableStateMapOf<String, BoardPoint>() }

    LaunchedEffect(state.nodes.keys) {
        val activeNodeIds = state.nodes.keys
        renderedPositions.keys.toList()
            .filterNot(activeNodeIds::contains)
            .forEach(renderedPositions::remove)

        state.nodes.forEach { (nodeId, node) ->
            if (nodeId !in renderedPositions) {
                renderedPositions[nodeId] = node.position
            }
        }
    }

    LaunchedEffect(state.nodes, state.draggingNodeIds) {
        while (true) {
            var hasPendingAnimation = false

            state.nodes.forEach { (nodeId, node) ->
                val targetPosition = node.position
                val currentPosition = renderedPositions[nodeId] ?: targetPosition
                val nextPosition = when {
                    nodeId in state.draggingNodeIds -> targetPosition
                    abs(targetPosition.x - currentPosition.x) <= ErdBoardConfig.REMOTE_DRAG_LERP_EPSILON &&
                        abs(targetPosition.y - currentPosition.y) <= ErdBoardConfig.REMOTE_DRAG_LERP_EPSILON -> targetPosition
                    else -> {
                        hasPendingAnimation = true
                        BoardPoint(
                            x = lerp(currentPosition.x, targetPosition.x, ErdBoardConfig.REMOTE_DRAG_LERP_FACTOR),
                            y = lerp(currentPosition.y, targetPosition.y, ErdBoardConfig.REMOTE_DRAG_LERP_FACTOR),
                        )
                    }
                }
                renderedPositions[nodeId] = nextPosition
            }

            if (!hasPendingAnimation) break
            withFrameNanos { }
        }
    }

    return state.nodes.mapValues { (nodeId, node) ->
        node.copy(position = renderedPositions[nodeId] ?: node.position)
    }
}
