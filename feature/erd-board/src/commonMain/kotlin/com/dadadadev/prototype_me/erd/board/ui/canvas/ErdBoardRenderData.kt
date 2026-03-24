package com.dadadadev.prototype_me.erd.board.ui.canvas

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.dadadadev.prototype_me.erd.board.presentation.contract.ErdBoardState

internal data class ErdBoardRenderData(
    val isConnecting: Boolean,
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

    val edgeSideOrientations = remember(state.edges, state.nodes) {
        buildStableEdgeSideOrientations(
            edges = state.edges.values,
            nodes = state.nodes,
            previousOrientations = stableEdgeSideCache,
        ).also { resolvedOrientations ->
            stableEdgeSideCache.clear()
            stableEdgeSideCache.putAll(resolvedOrientations)
        }
    }

    val portPositions = remember(state.nodes, state.scale, state.panOffset, density) {
        computeAllPortPositions(state.nodes, state.scale, state.panOffset, density)
    }

    val edgeHitPolylines = remember(
        state.edges,
        state.nodes,
        state.scale,
        state.panOffset,
        density,
        edgeSideOrientations,
    ) {
        buildEdgeHitPolylines(
            edges = state.edges.values.toList(),
            nodes = state.nodes,
            scale = state.scale,
            panOffset = state.panOffset,
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
        edgeSideOrientations = edgeSideOrientations,
        portPositions = portPositions,
        edgeHitPolylines = edgeHitPolylines,
        edgeMidpoints = edgeMidpoints,
        highlightedFieldIds = highlightedFieldIds,
        connectedFieldKeys = connectedFieldKeys,
    )
}

