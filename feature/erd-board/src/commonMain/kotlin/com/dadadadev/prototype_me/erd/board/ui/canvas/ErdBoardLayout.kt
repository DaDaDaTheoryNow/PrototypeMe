package com.dadadadev.prototype_me.erd.board.ui.canvas

import androidx.compose.ui.geometry.Offset
import com.dadadadev.prototype_me.domains.erd.design.api.domain.model.ErdEntityNode
import com.dadadadev.prototype_me.domains.erd.design.api.domain.model.ErdRelationEdge
import com.dadadadev.prototype_me.erd.board.config.ErdEdgeConfig
import com.dadadadev.prototype_me.erd.board.layout.ErdNodeDimens
import com.dadadadev.prototype_me.feature.board.core.ui.viewport.boardToScreenX
import com.dadadadev.prototype_me.feature.board.core.ui.viewport.boardToScreenY

internal data class PortKey(
    val nodeId: String,
    val fieldId: String,
    val side: Boolean = true,
)

internal data class EdgeAnchors(
    val src: Offset,
    val tgt: Offset,
    val srcIsRight: Boolean,
    val tgtIsRight: Boolean,
)

internal data class EdgeSideOrientation(
    val srcIsRight: Boolean,
    val tgtIsRight: Boolean,
)

internal data class DragSourceAnchor(
    val position: Offset,
    val isRight: Boolean,
)

/**
 * Scaled card dimension set, pre-calculated once per frame to avoid repeated multiplications.
 */
private class ScaledCardDimens(density: Float, scale: Float) {
    val headerPx = ErdNodeDimens.CARD_HEADER_DP * density * scale
    val dividerPx = ErdNodeDimens.CARD_DIVIDER_DP * density * scale
    val rowPx = ErdNodeDimens.CARD_FIELD_ROW_DP * density * scale

    /** Y-center of a field row at [fieldIndex] relative to the node top. */
    fun fieldCenterYOffset(fieldIndex: Int): Float =
        headerPx + dividerPx + rowPx * fieldIndex + rowPx / 2f
}

internal fun computeAllPortPositions(
    nodes: Map<String, ErdEntityNode>,
    scale: Float,
    panOffset: Offset,
    density: Float,
): Map<PortKey, Offset> {
    val result = mutableMapOf<PortKey, Offset>()
    val dimens = ScaledCardDimens(density, scale)

    nodes.values.forEach { node ->
        val nodeLeft = boardToScreenX(node.position.x, scale, panOffset.x, density)
        val nodeTop = boardToScreenY(node.position.y, scale, panOffset.y, density)
        val nodeWidth = node.size.width * density * scale

        node.fields.forEachIndexed { index, field ->
            val centerY = nodeTop + dimens.fieldCenterYOffset(index)
            result[PortKey(node.id, field.id, side = true)] = Offset(nodeLeft + nodeWidth, centerY)
            result[PortKey(node.id, field.id, side = false)] = Offset(nodeLeft, centerY)
        }
    }

    return result
}

internal fun computeEdgeAnchors(
    edge: ErdRelationEdge,
    nodes: Map<String, ErdEntityNode>,
    scale: Float,
    panOffset: Offset,
    density: Float,
    sideOrientation: EdgeSideOrientation? = null,
): EdgeAnchors? {
    val sourceNode = nodes[edge.sourceNodeId] ?: return null
    val targetNode = nodes[edge.targetNodeId] ?: return null
    val dimens = ScaledCardDimens(density, scale)

    fun anchorY(node: ErdEntityNode, fieldId: String?): Float {
        val top = boardToScreenY(node.position.y, scale, panOffset.y, density)
        return if (fieldId == null) {
            top + dimens.headerPx / 2f
        } else {
            val fieldIndex = node.fields.indexOfFirst { it.id == fieldId }.coerceAtLeast(0)
            top + dimens.fieldCenterYOffset(fieldIndex)
        }
    }

    val sourceLeft = boardToScreenX(sourceNode.position.x, scale, panOffset.x, density)
    val sourceRight = sourceLeft + sourceNode.size.width * density * scale
    val targetLeft = boardToScreenX(targetNode.position.x, scale, panOffset.x, density)
    val targetRight = targetLeft + targetNode.size.width * density * scale
    val sourceY = anchorY(sourceNode, edge.sourceFieldId)
    val targetY = anchorY(targetNode, edge.targetFieldId)
    val resolvedOrientation = sideOrientation
        ?: resolveStableEdgeSideOrientation(sourceNode = sourceNode, targetNode = targetNode)

    return EdgeAnchors(
        src = Offset(if (resolvedOrientation.srcIsRight) sourceRight else sourceLeft, sourceY),
        tgt = Offset(if (resolvedOrientation.tgtIsRight) targetRight else targetLeft, targetY),
        srcIsRight = resolvedOrientation.srcIsRight,
        tgtIsRight = resolvedOrientation.tgtIsRight,
    )
}

internal fun buildStableEdgeSideOrientations(
    edges: Collection<ErdRelationEdge>,
    nodes: Map<String, ErdEntityNode>,
    previousOrientations: Map<String, EdgeSideOrientation>,
): Map<String, EdgeSideOrientation> = buildMap {
    edges.forEach { edge ->
        val sourceNode = nodes[edge.sourceNodeId] ?: return@forEach
        val targetNode = nodes[edge.targetNodeId] ?: return@forEach
        put(
            edge.id,
            resolveStableEdgeSideOrientation(
                sourceNode = sourceNode,
                targetNode = targetNode,
                previousOrientation = previousOrientations[edge.id],
            ),
        )
    }
}

internal fun resolveStableEdgeSideOrientation(
    sourceNode: ErdEntityNode,
    targetNode: ErdEntityNode,
    previousOrientation: EdgeSideOrientation? = null,
): EdgeSideOrientation {
    val deltaX = (targetNode.position.x + targetNode.size.width / 2f) -
        (sourceNode.position.x + sourceNode.size.width / 2f)

    return when {
        deltaX > ErdEdgeConfig.SIDE_FLIP_HYSTERESIS_DP -> EdgeSideOrientation(srcIsRight = true, tgtIsRight = false)
        deltaX < -ErdEdgeConfig.SIDE_FLIP_HYSTERESIS_DP -> EdgeSideOrientation(srcIsRight = false, tgtIsRight = true)
        previousOrientation != null -> previousOrientation
        deltaX >= 0f -> EdgeSideOrientation(srcIsRight = true, tgtIsRight = false)
        else -> EdgeSideOrientation(srcIsRight = false, tgtIsRight = true)
    }
}

internal fun buildEdgeLabel(edge: ErdRelationEdge, nodes: Map<String, ErdEntityNode>): String {
    val sourceNode = nodes[edge.sourceNodeId]
    val targetNode = nodes[edge.targetNodeId]
    val sourceLabel = if (edge.sourceFieldId != null) {
        sourceNode?.fields?.firstOrNull { it.id == edge.sourceFieldId }?.name ?: "?"
    } else {
        sourceNode?.name ?: "?"
    }
    val targetLabel = if (edge.targetFieldId != null) {
        targetNode?.fields?.firstOrNull { it.id == edge.targetFieldId }?.name ?: "?"
    } else {
        targetNode?.name ?: "?"
    }
    return "$sourceLabel -> $targetLabel"
}
