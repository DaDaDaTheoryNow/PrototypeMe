package com.dadadadev.prototype_me.erd.board.ui.canvas

import androidx.compose.ui.geometry.Offset
import com.dadadadev.prototype_me.erd.board.config.ErdEdgeConfig
import kotlin.math.abs

/**
 * Pre-calculated Bézier curve anchors with control points.
 *
 * Extracted from the previously duplicated logic in [EdgesLayer] and [ErdBoardHitTesting]
 * to serve as a single source of truth for edge path geometry.
 */
internal data class BezierCurve(
    val src: Offset,
    val c1: Offset,
    val c2: Offset,
    val tgt: Offset,
)

/**
 * Calculates cubic Bézier control points for an edge, given its anchors and spread offset.
 *
 * @param anchors Resolved screen-space anchors for source and target.
 * @param spreadY Vertical pixel offset when multiple edges share a target port.
 */
internal fun calculateEdgeBezier(
    anchors: EdgeAnchors,
    spreadY: Float = 0f,
): BezierCurve {
    val src = anchors.src
    val tgt = anchors.tgt.copy(y = anchors.tgt.y + spreadY)
    val dx = abs(tgt.x - src.x).coerceAtLeast(ErdEdgeConfig.BEZIER_MIN_DX)
    val ctrl = (dx * ErdEdgeConfig.BEZIER_CTRL_FRACTION)
        .coerceIn(ErdEdgeConfig.BEZIER_MIN_DX, ErdEdgeConfig.BEZIER_MAX_CTRL)
    val srcDir = if (anchors.srcIsRight) 1f else -1f
    val tgtDir = if (anchors.tgtIsRight) 1f else -1f
    return BezierCurve(
        src = src,
        c1 = Offset(src.x + srcDir * ctrl, src.y),
        c2 = Offset(tgt.x + tgtDir * ctrl, tgt.y),
        tgt = tgt,
    )
}

/**
 * Overload accepting raw positions and side flags (for rubber-band snapped edges).
 */
internal fun calculateEdgeBezier(
    src: Offset,
    tgt: Offset,
    srcIsRight: Boolean,
    tgtIsRight: Boolean,
): BezierCurve = calculateEdgeBezier(
    anchors = EdgeAnchors(src, tgt, srcIsRight, tgtIsRight),
)
