package com.dadadadev.prototype_me.erd.board.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import com.dadadadev.prototype_me.erd.board.ui.DragSourceAnchor
import com.dadadadev.prototype_me.erd.board.ui.EdgeAnchors
import com.dadadadev.prototype_me.erd.board.ui.PortKey
import com.dadadadev.prototype_me.erd.board.ui.computeEdgeAnchors
import com.dadadadev.prototype_me.erd.board.ui.findSourceDragPortAnchor
import com.dadadadev.prototype_me.domains.erd.design.api.domain.model.EntityNode
import com.dadadadev.prototype_me.domains.erd.design.api.domain.model.RelationEdge
import kotlin.math.abs

@Composable
internal fun EdgesLayer(
    edges: Map<String, RelationEdge>,
    nodes: Map<String, EntityNode>,
    scale: Float,
    panOffset: Offset,
    density: Float,
    selectedEdgeId: String?,
    portPositions: Map<PortKey, Offset>,
    draggingEdgeFromNodeId: String?,
    draggingEdgeFromFieldId: String?,
    draggingEdgeCurrentPos: Offset?,
    draggingEdgeSnapTargetNodeId: String?,
    draggingEdgeSnapTargetFieldId: String?,
    draggingEdgeSnapTargetIsRight: Boolean?,
) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        // Multi-connection spread index per target port
        val targetPortCount = mutableMapOf<String, Int>()
        val edgePortIndex = mutableMapOf<String, Int>()
        edges.values.forEach { edge ->
            val key = "${edge.targetNodeId}:${edge.targetFieldId ?: "h"}"
            val idx = targetPortCount[key] ?: 0
            edgePortIndex[edge.id] = idx
            targetPortCount[key] = idx + 1
        }

        // Draw committed edges
        edges.values.forEach { edge ->
            val anchors = computeEdgeAnchors(edge, nodes, scale, panOffset, density) ?: return@forEach
            val spread = (edgePortIndex[edge.id] ?: 0) * 5f
            val isSelected = edge.id == selectedEdgeId
            val edgeColor = if (isSelected) Color(0xFF111111) else Color(0xFF888888)
            val strokeWidth = if (isSelected) 2.5f else 1.5f

            drawEdgeBezier(
                anchors = anchors,
                spreadY = spread,
                color = edgeColor,
                strokeWidth = strokeWidth,
            )

            if (isSelected) {
                val src = anchors.src
                val tgt = anchors.tgt.copy(y = anchors.tgt.y + spread)
                val mid = Offset((src.x + tgt.x) / 2f, (src.y + tgt.y) / 2f)
                drawCircle(Color(0xFF111111), 5f, mid)
            }
        }

        // Rubber-band line while dragging a new edge
        if (draggingEdgeFromNodeId != null && draggingEdgeCurrentPos != null) {
            val sourceAnchor = findSourceDragPortAnchor(
                sourceNodeId = draggingEdgeFromNodeId,
                sourceFieldId = draggingEdgeFromFieldId,
                pointer = draggingEdgeCurrentPos,
                portPositions = portPositions,
            ) ?: return@Canvas

            val snappedTargetPos = when {
                draggingEdgeSnapTargetNodeId != null &&
                        draggingEdgeSnapTargetFieldId != null &&
                        draggingEdgeSnapTargetIsRight != null -> portPositions[
                    PortKey(
                        nodeId = draggingEdgeSnapTargetNodeId,
                        fieldId = draggingEdgeSnapTargetFieldId,
                        side = draggingEdgeSnapTargetIsRight,
                    )
                ]
                else -> null
            }

            if (snappedTargetPos != null && draggingEdgeSnapTargetIsRight != null) {
                drawRubberBandSnapped(
                    sourceAnchor = sourceAnchor,
                    tgt = snappedTargetPos,
                    tgtIsRight = draggingEdgeSnapTargetIsRight,
                )
            } else {
                drawRubberBandFree(src = sourceAnchor.position, dragTo = draggingEdgeCurrentPos)
            }
        }
    }
}

// ── DrawScope helpers ─────────────────────────────────────────────────────────

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawEdgeBezier(
    anchors: EdgeAnchors,
    spreadY: Float,
    color: Color,
    strokeWidth: Float,
) {
    val src = anchors.src
    val tgt = anchors.tgt.copy(y = anchors.tgt.y + spreadY)
    val dx = abs(tgt.x - src.x).coerceAtLeast(40f)
    val ctrl = (dx * 0.45f).coerceIn(40f, 250f)
    val srcDir = if (anchors.srcIsRight) 1f else -1f
    val tgtDir = if (anchors.tgtIsRight) 1f else -1f
    val c1 = Offset(src.x + srcDir * ctrl, src.y)
    val c2 = Offset(tgt.x + tgtDir * ctrl, tgt.y)
    val path = Path().apply { moveTo(src.x, src.y); cubicTo(c1.x, c1.y, c2.x, c2.y, tgt.x, tgt.y) }
    drawPath(path, color, style = Stroke(strokeWidth, join = StrokeJoin.Round))
    drawCircle(color, 3f, src)
    drawCircle(color, 3f, tgt)
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawRubberBandSnapped(
    sourceAnchor: DragSourceAnchor,
    tgt: Offset,
    tgtIsRight: Boolean,
) {
    val src = sourceAnchor.position
    val dx = abs(tgt.x - src.x).coerceAtLeast(40f)
    val ctrl = (dx * 0.45f).coerceIn(40f, 250f)
    val srcDir = if (sourceAnchor.isRight) 1f else -1f
    val tgtDir = if (tgtIsRight) 1f else -1f
    val c1 = Offset(src.x + srcDir * ctrl, src.y)
    val c2 = Offset(tgt.x + tgtDir * ctrl, tgt.y)
    val path = Path().apply { moveTo(src.x, src.y); cubicTo(c1.x, c1.y, c2.x, c2.y, tgt.x, tgt.y) }
    drawPath(path, Color(0xFF888888), style = Stroke(1.5f, join = StrokeJoin.Round))
    drawCircle(Color(0xFF888888), 3f, src)
    drawCircle(Color(0xFF888888), 3f, tgt)
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawRubberBandFree(
    src: Offset,
    dragTo: Offset,
) {
    drawLine(
        Color(0xFF888888), src, dragTo,
        strokeWidth = 2f,
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 5f)),
        cap = StrokeCap.Round,
    )
    drawCircle(Color(0xFF555555), 5f, dragTo)
    drawCircle(Color(0xFF555555), 5f, src)
}


