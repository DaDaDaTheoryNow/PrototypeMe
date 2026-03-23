package com.dadadadev.prototype_me.board.ui

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import com.dadadadev.prototype_me.domain.models.EntityNode
import com.dadadadev.prototype_me.domain.models.RelationEdge
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

// ── Constants ─────────────────────────────────────────────────────────────────

internal const val GRID_BASE_STEP_PX = 32f
internal const val GRID_MIN_STEP_PX = 14f
internal const val GRID_MAX_DOTS_PER_FRAME = 4500
internal const val EDGE_HIT_SEGMENTS = 18
internal const val EDGE_SNAP_IN_MULTIPLIER = 0.95f
internal const val EDGE_SNAP_OUT_MULTIPLIER = 1.20f

// ── Data classes ──────────────────────────────────────────────────────────────

/** Identifies a connection point on a node field. [side] true = right, false = left. */
data class PortKey(val nodeId: String, val fieldId: String, val side: Boolean = true)

data class EdgeAnchors(
    val src: Offset,
    val tgt: Offset,
    val srcIsRight: Boolean,
    val tgtIsRight: Boolean,
)

internal data class GridRenderConfig(val stepPx: Float, val dotRadiusPx: Float)

internal data class DragSourceAnchor(val position: Offset, val isRight: Boolean)

// ── Grid ──────────────────────────────────────────────────────────────────────

internal fun computeGridRenderConfig(
    scale: Float,
    viewportWidth: Float,
    viewportHeight: Float,
): GridRenderConfig? {
    if (scale <= 0f || viewportWidth <= 0f || viewportHeight <= 0f) return null
    var step = GRID_BASE_STEP_PX * scale
    while (step < GRID_MIN_STEP_PX) step *= 2f
    val estimatedCols = (viewportWidth / step).toInt() + 2
    val estimatedRows = (viewportHeight / step).toInt() + 2
    val estimatedDots = estimatedCols * estimatedRows
    if (estimatedDots > GRID_MAX_DOTS_PER_FRAME) {
        val factor = sqrt(estimatedDots.toFloat() / GRID_MAX_DOTS_PER_FRAME.toFloat())
        step *= factor
    }
    val dotRadius = (1.5f * scale).coerceIn(1f, 3f)
    return GridRenderConfig(stepPx = step, dotRadiusPx = dotRadius)
}

internal fun Float.positiveMod(base: Float): Float {
    if (base == 0f) return this
    val m = this % base
    return if (m < 0f) m + base else m
}

// ── Port positions ────────────────────────────────────────────────────────────

fun computeAllPortPositions(
    nodes: Map<String, EntityNode>,
    scale: Float,
    panOffset: Offset,
    density: Float,
): Map<PortKey, Offset> {
    val result = mutableMapOf<PortKey, Offset>()
    val cardW = CARD_WIDTH_DP * density * scale
    val headerH = CARD_HEADER_DP * density * scale
    val dividerH = CARD_DIVIDER_DP * density * scale
    val rowH = CARD_FIELD_ROW_DP * density * scale
    nodes.values.forEach { node ->
        val bx = node.position.x * scale + panOffset.x
        val by = node.position.y * scale + panOffset.y
        node.fields.forEachIndexed { idx, field ->
            val cy = by + headerH + dividerH + rowH * idx + rowH / 2f
            result[PortKey(node.id, field.id, side = true)] = Offset(bx + cardW, cy)
            result[PortKey(node.id, field.id, side = false)] = Offset(bx, cy)
        }
    }
    return result
}

// ── Edge anchors ──────────────────────────────────────────────────────────────

fun computeEdgeAnchors(
    edge: RelationEdge,
    nodes: Map<String, EntityNode>,
    scale: Float,
    panOffset: Offset,
    density: Float,
): EdgeAnchors? {
    val srcNode = nodes[edge.sourceNodeId] ?: return null
    val tgtNode = nodes[edge.targetNodeId] ?: return null
    val cardW = CARD_WIDTH_DP * density * scale
    val headerH = CARD_HEADER_DP * density * scale
    val dividerH = CARD_DIVIDER_DP * density * scale
    val rowH = CARD_FIELD_ROW_DP * density * scale

    fun anchorY(node: EntityNode, fieldId: String?): Float {
        val base = node.position.y * scale + panOffset.y
        return if (fieldId == null) {
            base + headerH / 2f
        } else {
            val idx = node.fields.indexOfFirst { it.id == fieldId }.coerceAtLeast(0)
            base + headerH + dividerH + rowH * idx + rowH / 2f
        }
    }

    val srcLeft = srcNode.position.x * scale + panOffset.x
    val srcRight = srcLeft + cardW
    val tgtLeft = tgtNode.position.x * scale + panOffset.x
    val tgtRight = tgtLeft + cardW
    val srcY = anchorY(srcNode, edge.sourceFieldId)
    val tgtY = anchorY(tgtNode, edge.targetFieldId)
    val srcCX = (srcLeft + srcRight) / 2f
    val tgtCX = (tgtLeft + tgtRight) / 2f
    val srcIsRight: Boolean
    val tgtIsRight: Boolean
    if (srcCX < tgtCX) {
        srcIsRight = true
        tgtIsRight = false
    } else {
        srcIsRight = false
        tgtIsRight = true
    }
    val srcX = if (srcIsRight) srcRight else srcLeft
    val tgtX = if (tgtIsRight) tgtRight else tgtLeft
    return EdgeAnchors(Offset(srcX, srcY), Offset(tgtX, tgtY), srcIsRight, tgtIsRight)
}

// ── Bezier sampling ───────────────────────────────────────────────────────────

internal fun sampleCubicBezierPoints(
    p0: Offset,
    p1: Offset,
    p2: Offset,
    p3: Offset,
    segments: Int,
): List<Offset> {
    val safeSegments = segments.coerceAtLeast(1)
    return List(safeSegments + 1) { i ->
        cubicBezierPoint(p0, p1, p2, p3, i.toFloat() / safeSegments.toFloat())
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

// ── Edge hit testing ──────────────────────────────────────────────────────────

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
    for (i in 0 until polyline.lastIndex) {
        val d = distancePointToSegment(point, polyline[i], polyline[i + 1])
        if (d < best) best = d
    }
    return best
}

private fun distancePointToSegment(point: Offset, a: Offset, b: Offset): Float {
    val ab = b - a
    val ap = point - a
    val abLenSq = ab.x * ab.x + ab.y * ab.y
    if (abLenSq <= 1e-6f) return (point - a).getDistance()
    val t = ((ap.x * ab.x + ap.y * ab.y) / abLenSq).coerceIn(0f, 1f)
    val closest = Offset(a.x + ab.x * t, a.y + ab.y * t)
    return (point - closest).getDistance()
}

// ── Port/drag helpers ─────────────────────────────────────────────────────────

internal fun findSourceDragPortAnchor(
    sourceNodeId: String,
    sourceFieldId: String?,
    pointer: Offset,
    portPositions: Map<PortKey, Offset>,
): DragSourceAnchor? {
    val fieldId = sourceFieldId ?: return null
    val rightPos = portPositions[PortKey(sourceNodeId, fieldId, side = true)]
    val leftPos = portPositions[PortKey(sourceNodeId, fieldId, side = false)]
    return when {
        rightPos != null && leftPos != null -> {
            val centerX = (leftPos.x + rightPos.x) / 2f
            if (pointer.x >= centerX) DragSourceAnchor(rightPos, isRight = true)
            else DragSourceAnchor(leftPos, isRight = false)
        }
        rightPos != null -> DragSourceAnchor(rightPos, isRight = true)
        leftPos != null -> DragSourceAnchor(leftPos, isRight = false)
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
    portPositions.forEach { (key, pos) ->
        if (key.nodeId == sourceNodeId) return@forEach
        val d = (pointer - pos).getDistance()
        if (d <= bestDistance) {
            bestDistance = d
            best = key to pos
        }
    }
    return best
}

// ── Selection rect ────────────────────────────────────────────────────────────

internal fun buildSelectionRect(start: Offset, end: Offset): Rect = Rect(
    left = min(start.x, end.x),
    top = min(start.y, end.y),
    right = max(start.x, end.x),
    bottom = max(start.y, end.y),
)

internal fun findNodesIntersectingRect(
    rect: Rect,
    nodes: Map<String, EntityNode>,
    scale: Float,
    panOffset: Offset,
    density: Float,
): Set<String> {
    val cardWidth = CARD_WIDTH_DP * density * scale
    val headerHeight = CARD_HEADER_DP * density * scale
    val dividerHeight = CARD_DIVIDER_DP * density * scale
    val rowHeight = CARD_FIELD_ROW_DP * density * scale
    return nodes.values.asSequence().filter { node ->
        val left = node.position.x * scale + panOffset.x
        val top = node.position.y * scale + panOffset.y
        val fieldsHeight = rowHeight * node.fields.size
        val bodyHeight = if (node.fields.isNotEmpty()) dividerHeight + fieldsHeight else 0f
        val right = left + cardWidth
        val bottom = top + headerHeight + bodyHeight
        rect.left <= right && rect.right >= left && rect.top <= bottom && rect.bottom >= top
    }.map { it.id }.toSet()
}

// ── Edge label ────────────────────────────────────────────────────────────────

internal fun buildEdgeLabel(edge: RelationEdge, nodes: Map<String, EntityNode>): String {
    val srcNode = nodes[edge.sourceNodeId]
    val tgtNode = nodes[edge.targetNodeId]
    val srcLabel = if (edge.sourceFieldId != null)
        srcNode?.fields?.firstOrNull { it.id == edge.sourceFieldId }?.name ?: "?"
    else srcNode?.name ?: "?"
    val tgtLabel = if (edge.targetFieldId != null)
        tgtNode?.fields?.firstOrNull { it.id == edge.targetFieldId }?.name ?: "?"
    else tgtNode?.name ?: "?"
    return "$srcLabel -> $tgtLabel"
}
