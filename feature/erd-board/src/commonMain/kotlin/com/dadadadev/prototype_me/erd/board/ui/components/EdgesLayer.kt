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
import com.dadadadev.prototype_me.erd.board.ui.canvas.DragSourceAnchor
import com.dadadadev.prototype_me.erd.board.ui.canvas.EdgeAnchors
import com.dadadadev.prototype_me.erd.board.ui.canvas.EdgeSideOrientation
import com.dadadadev.prototype_me.erd.board.ui.canvas.PortKey
import com.dadadadev.prototype_me.erd.board.ui.canvas.calculateEdgeBezier
import com.dadadadev.prototype_me.erd.board.ui.canvas.computeEdgeAnchors
import com.dadadadev.prototype_me.erd.board.ui.canvas.findSourceDragPortAnchor
import com.dadadadev.prototype_me.erd.board.config.ErdEdgeConfig
import com.dadadadev.prototype_me.erd.board.ui.theme.ErdBoardColors
import com.dadadadev.prototype_me.domains.erd.design.api.domain.model.ErdEntityNode
import com.dadadadev.prototype_me.domains.erd.design.api.domain.model.ErdRelationEdge

@Composable
internal fun EdgesLayer(
    edges: Map<String, ErdRelationEdge>,
    nodes: Map<String, ErdEntityNode>,
    scale: Float,
    panOffset: Offset,
    density: Float,
    selectedEdgeId: String?,
    edgeSideOrientations: Map<String, EdgeSideOrientation>,
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
            val anchors = computeEdgeAnchors(
                edge = edge,
                nodes = nodes,
                scale = scale,
                panOffset = panOffset,
                density = density,
                sideOrientation = edgeSideOrientations[edge.id],
            ) ?: return@forEach
            val spread = (edgePortIndex[edge.id] ?: 0) * ErdEdgeConfig.MULTI_EDGE_SPREAD_PX
            val isSelected = edge.id == selectedEdgeId
            val edgeColor = if (isSelected) ErdBoardColors.edgeSelected else ErdBoardColors.edgeDefault
            val strokeWidth = if (isSelected) ErdEdgeConfig.STROKE_SELECTED else ErdEdgeConfig.STROKE_DEFAULT

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
                drawCircle(ErdBoardColors.edgeSelected, ErdEdgeConfig.HANDLE_DOT_RADIUS, mid)
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
    val curve = calculateEdgeBezier(anchors, spreadY)
    val path = Path().apply {
        moveTo(curve.src.x, curve.src.y)
        cubicTo(curve.c1.x, curve.c1.y, curve.c2.x, curve.c2.y, curve.tgt.x, curve.tgt.y)
    }
    drawPath(path, color, style = Stroke(strokeWidth, join = StrokeJoin.Round))
    drawCircle(color, ErdEdgeConfig.ENDPOINT_DOT_RADIUS, curve.src)
    drawCircle(color, ErdEdgeConfig.ENDPOINT_DOT_RADIUS, curve.tgt)
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawRubberBandSnapped(
    sourceAnchor: DragSourceAnchor,
    tgt: Offset,
    tgtIsRight: Boolean,
) {
    val curve = calculateEdgeBezier(
        src = sourceAnchor.position,
        tgt = tgt,
        srcIsRight = sourceAnchor.isRight,
        tgtIsRight = tgtIsRight,
    )
    val path = Path().apply {
        moveTo(curve.src.x, curve.src.y)
        cubicTo(curve.c1.x, curve.c1.y, curve.c2.x, curve.c2.y, curve.tgt.x, curve.tgt.y)
    }
    drawPath(path, ErdBoardColors.edgeDefault, style = Stroke(ErdEdgeConfig.STROKE_DEFAULT, join = StrokeJoin.Round))
    drawCircle(ErdBoardColors.edgeDefault, ErdEdgeConfig.ENDPOINT_DOT_RADIUS, curve.src)
    drawCircle(ErdBoardColors.edgeDefault, ErdEdgeConfig.ENDPOINT_DOT_RADIUS, curve.tgt)
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawRubberBandFree(
    src: Offset,
    dragTo: Offset,
) {
    drawLine(
        ErdBoardColors.edgeDefault, src, dragTo,
        strokeWidth = ErdEdgeConfig.STROKE_RUBBER_BAND,
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(ErdEdgeConfig.DASH_ON, ErdEdgeConfig.DASH_OFF)),
        cap = StrokeCap.Round,
    )
    drawCircle(ErdBoardColors.edgeDragHandle, ErdEdgeConfig.HANDLE_DOT_RADIUS, dragTo)
    drawCircle(ErdBoardColors.edgeDragHandle, ErdEdgeConfig.HANDLE_DOT_RADIUS, src)
}


