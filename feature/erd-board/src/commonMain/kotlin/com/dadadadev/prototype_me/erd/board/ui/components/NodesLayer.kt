package com.dadadadev.prototype_me.erd.board.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.offset
import com.dadadadev.prototype_me.erd.board.presentation.BoardIntent
import com.dadadadev.prototype_me.erd.board.ui.EDGE_SNAP_IN_MULTIPLIER
import com.dadadadev.prototype_me.erd.board.ui.EDGE_SNAP_OUT_MULTIPLIER
import com.dadadadev.prototype_me.erd.board.ui.EntityCard
import com.dadadadev.prototype_me.erd.board.ui.PortKey
import com.dadadadev.prototype_me.erd.board.ui.findNearestTargetPort
import com.dadadadev.prototype_me.domains.erd.design.api.domain.model.EntityNode
import kotlin.math.roundToInt

@Composable
internal fun NodesLayer(
    nodes: Map<String, EntityNode>,
    scale: Float,
    panOffset: Offset,
    portPositions: Map<PortKey, Offset>,
    connectingFromNodeId: String?,
    connectingFromFieldId: String?,
    draggingEdgeFromNodeId: String?,
    draggingEdgeFromFieldId: String?,
    nodeMenuNodeId: String?,
    selectedNodeIds: Set<String>,
    highlightedFieldIds: Set<String>,
    connectedFieldKeys: Set<String>,
    isConnecting: Boolean,
    portHitPx: Float,
    onIntent: (BoardIntent) -> Unit,
) {
    val latestPortPositions by rememberUpdatedState(portPositions)

    // Entity cards
    nodes.values.forEach { node ->
        key(node.id) {
            EntityCard(
                node = node,
                scale = scale,
                panOffset = panOffset,
                isSourceNode = node.id == connectingFromNodeId ||
                        node.id == draggingEdgeFromNodeId ||
                        node.id == nodeMenuNodeId,
                isConnecting = isConnecting,
                isSelected = node.id in selectedNodeIds,
                highlightedFieldIds = highlightedFieldIds,
                onDragStart = { onIntent(BoardIntent.OnDragStart(node.id)) },
                onDrag = { delta -> onIntent(BoardIntent.OnDragNode(node.id, delta)) },
                onDragEnd = { onIntent(BoardIntent.OnDragEnd(node.id)) },
                onTap = { onIntent(BoardIntent.OnNodeMenu(node.id)) },
                onLongPress = { onIntent(BoardIntent.OnSelectNode(node.id)) },
            )
        }
    }

    // Port dots (field-level, left + right of each field row)
    portPositions.forEach { (portKey, screenPos) ->
        val portIsSource = (connectingFromNodeId == portKey.nodeId && connectingFromFieldId == portKey.fieldId) ||
                (draggingEdgeFromNodeId == portKey.nodeId && draggingEdgeFromFieldId == portKey.fieldId)
        val isConnectedPort = "${portKey.nodeId}:${portKey.fieldId}" in connectedFieldKeys
        val dotColor = when {
            portIsSource || isConnectedPort -> Color(0xFF111111)
            isConnecting -> Color(0xFF888888)
            else -> Color(0xFFCCCCCC)
        }
        val snapInPx = portHitPx * EDGE_SNAP_IN_MULTIPLIER
        val snapOutPx = portHitPx * EDGE_SNAP_OUT_MULTIPLIER
        val dotRadiusPx = 10.dp

        Box(
            modifier = Modifier
                .offset {
                    IntOffset(
                        (screenPos.x - dotRadiusPx.toPx()).roundToInt(),
                        (screenPos.y - dotRadiusPx.toPx()).roundToInt(),
                    )
                }
                .size(20.dp)
                .pointerInput(portKey.nodeId, portKey.fieldId, portKey.side) {
                    fun currentBoxTopLeft(): Offset {
                        val currentPos = latestPortPositions[portKey] ?: screenPos
                        val r = 10.dp.toPx()
                        return Offset(
                            (currentPos.x - r).roundToInt().toFloat(),
                            (currentPos.y - r).roundToInt().toFloat(),
                        )
                    }

                    awaitEachGesture {
                        val down = awaitFirstDown()
                        down.consume()
                        var isDragging = false
                        var snappedTarget: Pair<PortKey, Offset>? = null
                        var latestLocalPos = down.position
                        var latestRawScreenPos = currentBoxTopLeft() + latestLocalPos

                        while (true) {
                            val event = awaitPointerEvent()
                            val change = event.changes.find { it.id == down.id } ?: break
                            change.consume()
                            latestLocalPos = change.position
                            latestRawScreenPos = currentBoxTopLeft() + latestLocalPos

                            val nearest = findNearestTargetPort(
                                pointer = latestRawScreenPos,
                                sourceNodeId = portKey.nodeId,
                                portPositions = latestPortPositions,
                                maxDistancePx = snapInPx,
                            )
                            val active = snappedTarget
                            snappedTarget = when {
                                nearest != null -> nearest
                                active != null -> {
                                    if ((latestRawScreenPos - active.second).getDistance() <= snapOutPx) active
                                    else null
                                }
                                else -> null
                            }
                            val latestRenderScreenPos = snappedTarget?.second ?: latestRawScreenPos

                            if (!change.pressed) {
                                if (isDragging) {
                                    val finalTarget = snappedTarget ?: findNearestTargetPort(
                                        pointer = latestRawScreenPos,
                                        sourceNodeId = portKey.nodeId,
                                        portPositions = latestPortPositions,
                                        maxDistancePx = portHitPx,
                                    )
                                    onIntent(
                                        BoardIntent.OnEdgeDragEnd(
                                            finalTarget?.first?.nodeId,
                                            finalTarget?.first?.fieldId,
                                        )
                                    )
                                } else {
                                    onIntent(BoardIntent.OnNodeFieldTap(portKey.nodeId, portKey.fieldId))
                                }
                                break
                            }

                            if (!isDragging &&
                                (latestLocalPos - down.position).getDistance() > viewConfiguration.touchSlop
                            ) {
                                isDragging = true
                                onIntent(BoardIntent.OnEdgeDragStart(portKey.nodeId, portKey.fieldId))
                            }

                            if (isDragging) {
                                onIntent(
                                    BoardIntent.OnEdgeDragMove(
                                        screenPos = latestRenderScreenPos,
                                        snappedTargetNodeId = snappedTarget?.first?.nodeId,
                                        snappedTargetFieldId = snappedTarget?.first?.fieldId,
                                        snappedTargetIsRight = snappedTarget?.first?.side,
                                    )
                                )
                            }
                        }
                    }
                },
            contentAlignment = Alignment.Center,
        ) {
            Box(Modifier.size(8.dp).background(dotColor, CircleShape))
        }
    }
}


