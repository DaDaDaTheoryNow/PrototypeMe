package com.dadadadev.prototype_me.erd.board.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.dadadadev.prototype_me.domains.erd.design.api.domain.model.ErdEntityNode
import com.dadadadev.prototype_me.erd.board.presentation.contract.ErdBoardIntent
import com.dadadadev.prototype_me.erd.board.ui.canvas.findTopNodeAt
import com.dadadadev.prototype_me.erd.board.ui.canvas.isPointerOnPort
import com.dadadadev.prototype_me.erd.board.ui.canvas.PortKey
import com.dadadadev.prototype_me.erd.board.ui.canvas.findNearestTargetPort
import com.dadadadev.prototype_me.erd.board.config.ErdEdgeConfig
import com.dadadadev.prototype_me.erd.board.ui.dimens.ErdBoardDimens
import com.dadadadev.prototype_me.erd.board.ui.node.EntityCard
import com.dadadadev.prototype_me.erd.board.ui.mappers.toBoardVector
import com.dadadadev.prototype_me.erd.board.ui.theme.ErdBoardColors
import com.dadadadev.prototype_me.feature.board.core.ui.viewport.screenDeltaToBoardDelta
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.math.roundToInt

@Composable
internal fun NodesLayer(
    nodes: Map<String, ErdEntityNode>,
    scale: Float,
    panOffset: Offset,
    density: Float,
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
    onIntent: (ErdBoardIntent) -> Unit,
) {
    val latestNodes by rememberUpdatedState(nodes)
    val latestOnIntent by rememberUpdatedState(onIntent)
    val latestPortPositions by rememberUpdatedState(portPositions)
    val latestScale by rememberUpdatedState(scale)
    val latestPanOffset by rememberUpdatedState(panOffset)
    val latestDensity by rememberUpdatedState(density)
    val portTargetRadiusPx = ErdBoardDimens.PORT_HIT_TARGET_DP * density

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    if (isPointerOnPort(down.position, latestPortPositions, portTargetRadiusPx)) {
                        return@awaitEachGesture
                    }

                    val hitNodeId = findTopNodeAt(
                        pointer = down.position,
                        nodes = latestNodes,
                        scale = latestScale,
                        panOffset = latestPanOffset,
                        density = latestDensity,
                    ) ?: return@awaitEachGesture

                    down.consume()

                    val downPosition = down.position
                    var isDragging = false
                    var wasReleased = false

                    val completedBeforeLongPress = withTimeoutOrNull(viewConfiguration.longPressTimeoutMillis) {
                        while (true) {
                            val event = awaitPointerEvent()
                            val change = event.changes.find { it.id == down.id } ?: break

                            if (!change.pressed) {
                                wasReleased = true
                                change.consume()
                                break
                            }

                            val distance = (change.position - downPosition).getDistance()
                            if (!isDragging && distance > viewConfiguration.touchSlop) {
                                isDragging = true
                                latestOnIntent(ErdBoardIntent.OnDragStart(hitNodeId))
                            }

                            if (isDragging) {
                                val delta = change.position - change.previousPosition
                                if (delta.getDistance() > 0f) {
                                    change.consume()
                                    latestOnIntent(
                                        ErdBoardIntent.OnDragNode(
                                            nodeId = hitNodeId,
                                            delta = screenDeltaToBoardDelta(delta, latestScale, latestDensity).toBoardVector(),
                                        ),
                                    )
                                }
                            }
                        }
                    }

                    if (isDragging) {
                        if (!wasReleased) {
                            while (true) {
                                val event = awaitPointerEvent()
                                val change = event.changes.find { it.id == down.id } ?: break
                                if (!change.pressed) {
                                    change.consume()
                                    break
                                }

                                val delta = change.position - change.previousPosition
                                if (delta.getDistance() > 0f) {
                                    change.consume()
                                    latestOnIntent(
                                        ErdBoardIntent.OnDragNode(
                                            nodeId = hitNodeId,
                                            delta = screenDeltaToBoardDelta(delta, latestScale, latestDensity).toBoardVector(),
                                        ),
                                    )
                                }
                            }
                        }

                        latestOnIntent(ErdBoardIntent.OnDragEnd(hitNodeId))
                    } else if (completedBeforeLongPress == null) {
                        latestOnIntent(ErdBoardIntent.OnSelectNode(hitNodeId))
                    } else if (wasReleased) {
                        latestOnIntent(ErdBoardIntent.OnNodeMenu(hitNodeId))
                    }
                }
            },
    ) {
        nodes.values.forEach { node ->
            key(node.id) {
                EntityCard(
                    node = node,
                    scale = scale,
                    panOffset = panOffset,
                    density = density,
                    isSourceNode = node.id == connectingFromNodeId ||
                            node.id == draggingEdgeFromNodeId ||
                            node.id == nodeMenuNodeId,
                    isSelected = node.id in selectedNodeIds,
                    highlightedFieldIds = highlightedFieldIds,
                )
            }
        }

        // Port dots (field-level, left + right of each field row)
        portPositions.forEach { (portKey, screenPos) ->
            val portIsSource = (connectingFromNodeId == portKey.nodeId && connectingFromFieldId == portKey.fieldId) ||
                    (draggingEdgeFromNodeId == portKey.nodeId && draggingEdgeFromFieldId == portKey.fieldId)
            val isConnectedPort = "${portKey.nodeId}:${portKey.fieldId}" in connectedFieldKeys
            val dotColor = when {
                portIsSource || isConnectedPort -> ErdBoardColors.portActive
                isConnecting -> ErdBoardColors.portAvailable
                else -> ErdBoardColors.portInactive
            }
            val snapInPx = portHitPx * ErdEdgeConfig.SNAP_IN_MULTIPLIER
            val snapOutPx = portHitPx * ErdEdgeConfig.SNAP_OUT_MULTIPLIER
            val dotRadiusDp = ErdBoardDimens.PORT_HIT_TARGET_DP.dp

            Box(
                modifier = Modifier
                    .offset {
                        IntOffset(
                            (screenPos.x - dotRadiusDp.toPx()).roundToInt(),
                            (screenPos.y - dotRadiusDp.toPx()).roundToInt(),
                        )
                    }
                    .size(ErdBoardDimens.PORT_HIT_TARGET_SIZE_DP.dp)
                    .pointerInput(portKey.nodeId, portKey.fieldId, portKey.side) {
                        fun currentBoxTopLeft(): Offset {
                            val currentPos = latestPortPositions[portKey] ?: screenPos
                            val r = ErdBoardDimens.PORT_HIT_TARGET_DP.dp.toPx()
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
                                            ErdBoardIntent.OnEdgeDragEnd(
                                                finalTarget?.first?.nodeId,
                                                finalTarget?.first?.fieldId,
                                            )
                                        )
                                    } else {
                                        onIntent(ErdBoardIntent.OnNodeFieldTap(portKey.nodeId, portKey.fieldId))
                                    }
                                    break
                                }

                                if (!isDragging &&
                                    (latestLocalPos - down.position).getDistance() > viewConfiguration.touchSlop
                                ) {
                                    isDragging = true
                                    onIntent(ErdBoardIntent.OnEdgeDragStart(portKey.nodeId, portKey.fieldId))
                                }

                                if (isDragging) {
                                    onIntent(
                                        ErdBoardIntent.OnEdgeDragMove(
                                            screenPos = latestRenderScreenPos.toBoardVector(),
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
                Box(Modifier.size(ErdBoardDimens.PORT_DOT_SIZE_DP.dp).background(dotColor, CircleShape))
            }
        }
    }
}
