package com.dadadadev.prototype_me.erd.board.ui.canvas

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import com.dadadadev.prototype_me.domains.erd.design.api.domain.model.EntityNode
import com.dadadadev.prototype_me.domains.erd.design.api.domain.model.RelationEdge
import kotlin.math.abs

internal const val EDGE_HIT_SEGMENTS = 18
internal const val EDGE_SNAP_IN_MULTIPLIER = 0.95f
internal const val EDGE_SNAP_OUT_MULTIPLIER = 1.20f

internal fun buildEdgeHitPolylines(
    edges: List<RelationEdge>,
    nodes: Map<String, EntityNode>,
    scale: Float,
    panOffset: Offset,
    density: Float,
    edgeSideOrientations: Map<String, EdgeSideOrientation>,
): Map<String, List<Offset>> {
    val targetPortCount = mutableMapOf<String, Int>()
    val edgePortIndex = mutableMapOf<String, Int>()

    edges.forEach { edge ->
        val key = "${edge.targetNodeId}:${edge.targetFieldId ?: "h"}"
        val index = targetPortCount[key] ?: 0
        edgePortIndex[edge.id] = index
        targetPortCount[key] = index + 1
    }

    return edges.mapNotNull { edge ->
        val anchors = computeEdgeAnchors(
            edge = edge,
            nodes = nodes,
            scale = scale,
            panOffset = panOffset,
            density = density,
            sideOrientation = edgeSideOrientations[edge.id],
        ) ?: return@mapNotNull null

        val spread = (edgePortIndex[edge.id] ?: 0) * 5f
        val source = anchors.src
        val target = anchors.tgt.copy(y = anchors.tgt.y + spread)
        val dx = abs(target.x - source.x).coerceAtLeast(40f)
        val control = (dx * 0.45f).coerceIn(40f, 250f)
        val sourceDirection = if (anchors.srcIsRight) 1f else -1f
        val targetDirection = if (anchors.tgtIsRight) 1f else -1f
        val c1 = Offset(source.x + sourceDirection * control, source.y)
        val c2 = Offset(target.x + targetDirection * control, target.y)

        edge.id to sampleCubicBezierPoints(source, c1, c2, target, EDGE_HIT_SEGMENTS)
    }.toMap()
}

internal fun sampleCubicBezierPoints(
    p0: Offset,
    p1: Offset,
    p2: Offset,
    p3: Offset,
    segments: Int,
): List<Offset> {
    val safeSegments = segments.coerceAtLeast(1)
    return List(safeSegments + 1) { index ->
        cubicBezierPoint(p0, p1, p2, p3, index.toFloat() / safeSegments.toFloat())
    }
}

private fun cubicBezierPoint(
    p0: Offset,
    p1: Offset,
    p2: Offset,
    p3: Offset,
    t: Float,
): Offset {
    val u = 1f - t
    val tt = t * t
    val uu = u * u
    val uuu = uu * u
    val ttt = tt * t
    return Offset(
        x = uuu * p0.x + 3f * uu * t * p1.x + 3f * u * tt * p2.x + ttt * p3.x,
        y = uuu * p0.y + 3f * uu * t * p1.y + 3f * u * tt * p2.y + ttt * p3.y,
    )
}

internal fun findTappedEdgeId(
    tap: Offset,
    edgeHitPolylines: Map<String, List<Offset>>,
    hitRadiusPx: Float,
): String? {
    var bestEdgeId: String? = null
    var bestDistance = Float.MAX_VALUE

    edgeHitPolylines.forEach { (edgeId, polyline) ->
        val distance = distanceToPolyline(tap, polyline)
        if (distance <= hitRadiusPx && distance < bestDistance) {
            bestDistance = distance
            bestEdgeId = edgeId
        }
    }

    return bestEdgeId
}

private fun distanceToPolyline(point: Offset, polyline: List<Offset>): Float {
    if (polyline.isEmpty()) return Float.MAX_VALUE
    if (polyline.size == 1) return (point - polyline.first()).getDistance()

    var best = Float.MAX_VALUE
    for (index in 0 until polyline.lastIndex) {
        val distance = distancePointToSegment(point, polyline[index], polyline[index + 1])
        if (distance < best) best = distance
    }
    return best
}

private fun distancePointToSegment(point: Offset, start: Offset, end: Offset): Float {
    val segment = end - start
    val pointFromStart = point - start
    val segmentLengthSquared = segment.x * segment.x + segment.y * segment.y
    if (segmentLengthSquared <= 1e-6f) return (point - start).getDistance()

    val t = ((pointFromStart.x * segment.x + pointFromStart.y * segment.y) / segmentLengthSquared)
        .coerceIn(0f, 1f)
    val closest = Offset(start.x + segment.x * t, start.y + segment.y * t)
    return (point - closest).getDistance()
}

internal fun findSourceDragPortAnchor(
    sourceNodeId: String,
    sourceFieldId: String?,
    pointer: Offset,
    portPositions: Map<PortKey, Offset>,
): DragSourceAnchor? {
    val fieldId = sourceFieldId ?: return null
    val rightPosition = portPositions[PortKey(sourceNodeId, fieldId, side = true)]
    val leftPosition = portPositions[PortKey(sourceNodeId, fieldId, side = false)]

    return when {
        rightPosition != null && leftPosition != null -> {
            val centerX = (leftPosition.x + rightPosition.x) / 2f
            if (pointer.x >= centerX) {
                DragSourceAnchor(rightPosition, isRight = true)
            } else {
                DragSourceAnchor(leftPosition, isRight = false)
            }
        }
        rightPosition != null -> DragSourceAnchor(rightPosition, isRight = true)
        leftPosition != null -> DragSourceAnchor(leftPosition, isRight = false)
        else -> null
    }
}

internal fun findNearestTargetPort(
    pointer: Offset,
    sourceNodeId: String,
    portPositions: Map<PortKey, Offset>,
    maxDistancePx: Float,
): Pair<PortKey, Offset>? {
    var best: Pair<PortKey, Offset>? = null
    var bestDistance = maxDistancePx

    portPositions.forEach { (key, position) ->
        if (key.nodeId == sourceNodeId) return@forEach
        val distance = (pointer - position).getDistance()
        if (distance <= bestDistance) {
            bestDistance = distance
            best = key to position
        }
    }

    return best
}
