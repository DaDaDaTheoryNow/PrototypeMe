package com.dadadadev.prototype_me.erd.board.ui.canvas

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import com.dadadadev.prototype_me.domains.erd.design.api.domain.model.ErdEntityNode
import com.dadadadev.prototype_me.feature.board.core.ui.viewport.boardToScreenX
import com.dadadadev.prototype_me.feature.board.core.ui.viewport.boardToScreenY

internal fun isPointerOnPort(
    pointer: Offset,
    portPositions: Map<PortKey, Offset>,
    portTargetRadiusPx: Float,
): Boolean = portPositions.values.any { position ->
    (pointer - position).getDistance() <= portTargetRadiusPx
}

internal fun findTopNodeAt(
    pointer: Offset,
    nodes: Map<String, ErdEntityNode>,
    scale: Float,
    panOffset: Offset,
    density: Float,
): String? = nodes.values
    .toList()
    .asReversed()
    .firstOrNull { node ->
        val left = boardToScreenX(node.position.x, scale, panOffset.x, density)
        val top = boardToScreenY(node.position.y, scale, panOffset.y, density)
        Rect(
            left = left,
            top = top,
            right = left + node.size.width * density * scale,
            bottom = top + node.size.height * density * scale,
        ).contains(pointer)
    }
    ?.id
