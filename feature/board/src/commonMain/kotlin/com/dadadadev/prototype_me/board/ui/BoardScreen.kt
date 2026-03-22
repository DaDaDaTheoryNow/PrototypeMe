package com.dadadadev.prototype_me.board.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dadadadev.prototype_me.board.presentation.BoardIntent
import com.dadadadev.prototype_me.board.presentation.BoardSideEffect
import com.dadadadev.prototype_me.board.presentation.BoardViewModel
import com.dadadadev.prototype_me.domain.models.EntityNode
import com.dadadadev.prototype_me.domain.models.FieldType
import com.dadadadev.prototype_me.domain.models.NodeField
import com.dadadadev.prototype_me.domain.models.Position
import com.dadadadev.prototype_me.domain.models.RelationEdge
import org.koin.compose.viewmodel.koinViewModel
import org.orbitmvi.orbit.compose.collectSideEffect
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.sqrt

// в”Ђв”Ђ Port key вЂ” identifies a connection point в”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђ
// side: true = right, false = left.  fieldId is never null (no entity-level ports)
data class PortKey(val nodeId: String, val fieldId: String, val side: Boolean = true)

private const val GRID_BASE_STEP_PX = 32f
private const val GRID_MIN_STEP_PX = 14f
private const val GRID_MAX_DOTS_PER_FRAME = 4500
private const val EDGE_HIT_SEGMENTS = 18
private const val EDGE_SNAP_IN_MULTIPLIER = 0.95f
private const val EDGE_SNAP_OUT_MULTIPLIER = 1.20f

private data class GridRenderConfig(val stepPx: Float, val dotRadiusPx: Float)

// в”Ђв”Ђ BoardScreen в”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђ

@Composable
fun BoardScreen(viewModel: BoardViewModel = koinViewModel()) {
    val state by viewModel.container.stateFlow.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val density = LocalDensity.current.density

    var showAddNodeDialog by remember { mutableStateOf(false) }
    var newNodeName by remember { mutableStateOf("") }

    val isConnecting = state.connectingFromNodeId != null || state.draggingEdgeFromNodeId != null

    // Port positions вЂ” field-level only, left + right of each field row
    val portPositions = remember(state.nodes, state.scale, state.panOffset, density) {
        computeAllPortPositions(state.nodes, state.scale, state.panOffset, density)
    }
    val latestPortPositions by rememberUpdatedState(portPositions)

    val PORT_HIT_PX = 20f * density
    val EDGE_HIT_PX = 20f * density

    // Edge polylines for full-path hit-test and toolbar placement.
    val edgeHitPolylines = remember(state.edges, state.nodes, state.scale, state.panOffset, density) {
        val targetPortCount = mutableMapOf<String, Int>()
        val edgePortIndex = mutableMapOf<String, Int>()
        state.edges.values.forEach { edge ->
            val key = "${edge.targetNodeId}:${edge.targetFieldId ?: "h"}"
            val idx = targetPortCount[key] ?: 0
            edgePortIndex[edge.id] = idx
            targetPortCount[key] = idx + 1
        }

        state.edges.values.mapNotNull { edge ->
            val anchors = computeEdgeAnchors(edge, state.nodes, state.scale, state.panOffset, density)
                ?: return@mapNotNull null

            val spread = (edgePortIndex[edge.id] ?: 0) * 5f
            val src = anchors.src
            val tgt = anchors.tgt.copy(y = anchors.tgt.y + spread)

            val dx = abs(tgt.x - src.x).coerceAtLeast(40f)
            val ctrl = (dx * 0.45f).coerceIn(40f, 250f)
            val srcDir = if (anchors.srcIsRight) 1f else -1f
            val tgtDir = if (anchors.tgtIsRight) 1f else -1f
            val c1 = Offset(src.x + srcDir * ctrl, src.y)
            val c2 = Offset(tgt.x + tgtDir * ctrl, tgt.y)

            edge.id to sampleCubicBezierPoints(src, c1, c2, tgt, EDGE_HIT_SEGMENTS)
        }.toMap()
    }
    val edgeMidpoints = remember(edgeHitPolylines) {
        edgeHitPolylines.mapValues { (_, points) -> points.getOrNull(points.size / 2) }
    }
    val latestEdgeHitPolylines by rememberUpdatedState(edgeHitPolylines)
    val latestState by rememberUpdatedState(state)

    // Fields highlighted by selected edge
    val highlightedFields = remember(state.selectedEdgeId, state.edges) {
        val edge = state.edges[state.selectedEdgeId]
        buildSet {
            edge?.sourceFieldId?.let { add(it) }
            edge?.targetFieldId?.let { add(it) }
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

    viewModel.collectSideEffect { effect ->
        when (effect) {
            is BoardSideEffect.ShowLockError ->
                snackbarHostState.showSnackbar("Locked by ${effect.lockedBy}")
            is BoardSideEffect.ShowConnectionLost ->
                snackbarHostState.showSnackbar("Connection lost вЂ” reconnectingвЂ¦")
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                // в”Ђв”Ђ Mouse wheel = zoom в”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђ
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent()
                            if (event.type == PointerEventType.Scroll) {
                                val change = event.changes.firstOrNull() ?: continue
                                val dy = change.scrollDelta.y
                                if (dy == 0f) continue
                                change.consume()
                                val factor = if (dy < 0f) 1.12f else 0.88f
                                viewModel.onIntent(
                                    BoardIntent.OnPanZoom(change.position, Offset.Zero, factor)
                                )
                            }
                        }
                    }
                }
                // в”Ђв”Ђ Single handler: tap / LMB-drag-pan / two-finger pinch в”Ђв”Ђв”Ђв”Ђ
                .pointerInput(Unit) {
                    canvasGestureHandler(
                        onPanZoom = { centroid, pan, zoom ->
                            viewModel.onIntent(BoardIntent.OnPanZoom(centroid, pan, zoom))
                        },
                        onTap = { tap ->
                            val tappedEdgeId = findTappedEdgeId(tap, latestEdgeHitPolylines, EDGE_HIT_PX)
                            val onPort = latestPortPositions.values.any { pos ->
                                (tap - pos).getDistance() < PORT_HIT_PX
                            }
                            when {
                                tappedEdgeId != null -> viewModel.onIntent(BoardIntent.OnSelectEdge(tappedEdgeId))
                                !onPort -> {
                                    viewModel.onIntent(BoardIntent.OnSelectEdge(null))
                                    viewModel.onIntent(BoardIntent.OnNodeMenu(null))
                                    if (latestState.connectingFromNodeId != null)
                                        viewModel.onIntent(BoardIntent.OnCancelConnect)
                                    if (latestState.draggingEdgeFromNodeId != null)
                                        viewModel.onIntent(BoardIntent.OnEdgeDragEnd(null, null))
                                }
                            }
                        }
                    )
                }
        ) {
            val screenW = constraints.maxWidth.toFloat()
            val screenH = constraints.maxHeight.toFloat()

            // No culling вЂ” render all nodes
            val allNodes = state.nodes.values.toList()

            // в”Ђв”Ђ Canvas: grid + edges + rubber band в”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђ
            val capturedState = state
            val capturedDensity = density
            val capturedPortPositions = portPositions

            Canvas(modifier = Modifier.fillMaxSize()) {
                // Dot grid (adaptive LOD + capped draw count per frame).
                val grid = computeGridRenderConfig(capturedState.scale, size.width, size.height)
                if (grid != null) {
                    val offX = capturedState.panOffset.x.positiveMod(grid.stepPx)
                    val offY = capturedState.panOffset.y.positiveMod(grid.stepPx)
                    var gx = offX
                    while (gx < size.width) {
                        var gy = offY
                        while (gy < size.height) {
                            drawCircle(Color(0xFFDDDDDD), grid.dotRadiusPx, Offset(gx, gy))
                            gy += grid.stepPx
                        }
                        gx += grid.stepPx
                    }
                }

                // Multi-connection spread
                val targetPortCount = mutableMapOf<String, Int>()
                val edgePortIndex = mutableMapOf<String, Int>()
                capturedState.edges.values.forEach { edge ->
                    val key = "${edge.targetNodeId}:${edge.targetFieldId ?: "h"}"
                    val idx = targetPortCount[key] ?: 0
                    edgePortIndex[edge.id] = idx
                    targetPortCount[key] = idx + 1
                }

                // Edges вЂ” simple bezier curves
                capturedState.edges.values.forEach { edge ->
                    val anchors = computeEdgeAnchors(
                        edge, capturedState.nodes, capturedState.scale, capturedState.panOffset, capturedDensity
                    ) ?: return@forEach

                    val spread = (edgePortIndex[edge.id] ?: 0) * 5f
                    val src = anchors.src
                    val tgt = anchors.tgt.copy(y = anchors.tgt.y + spread)
                    val isSelected = edge.id == capturedState.selectedEdgeId

                    val edgeColor = if (isSelected) Color(0xFF111111) else Color(0xFF888888)
                    val sw = if (isSelected) 2.5f else 1.5f

                    // Horizontal bezier
                    val dx = abs(tgt.x - src.x).coerceAtLeast(40f)
                    val ctrl = (dx * 0.45f).coerceIn(40f, 250f)
                    val srcDir = if (anchors.srcIsRight) 1f else -1f
                    val tgtDir = if (anchors.tgtIsRight) 1f else -1f
                    val c1 = Offset(src.x + srcDir * ctrl, src.y)
                    val c2 = Offset(tgt.x + tgtDir * ctrl, tgt.y)

                    val path = Path().apply {
                        moveTo(src.x, src.y)
                        cubicTo(c1.x, c1.y, c2.x, c2.y, tgt.x, tgt.y)
                    }
                    drawPath(path, edgeColor, style = Stroke(sw, join = StrokeJoin.Round))

                    // Small circles at endpoints
                    drawCircle(edgeColor, 3f, src)
                    drawCircle(edgeColor, 3f, tgt)

                    // Selection indicator
                    if (isSelected) {
                        val mid = Offset((src.x + tgt.x) / 2f, (src.y + tgt.y) / 2f)
                        drawCircle(Color(0xFF111111), 5f, mid)
                    }
                }

                // Rubber band line while dragging new edge
                val dragFrom = capturedState.draggingEdgeFromNodeId
                val dragTo = capturedState.draggingEdgeCurrentPos
                if (dragFrom != null && dragTo != null) {
                    val srcPortKey = capturedPortPositions.keys.firstOrNull { k ->
                        k.nodeId == dragFrom && k.fieldId == capturedState.draggingEdgeFromFieldId
                    }
                    val srcPos = srcPortKey?.let { capturedPortPositions[it] }
                    if (srcPos != null) {
                        drawLine(
                            Color(0xFF888888), srcPos, dragTo,
                            strokeWidth = 2f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 5f)),
                            cap = StrokeCap.Round
                        )
                        drawCircle(Color(0xFF555555), 5f, dragTo)
                        drawCircle(Color(0xFF555555), 5f, srcPos)
                    }
                }
            }

            // в”Ђв”Ђ Entity cards в”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђ
            allNodes.forEach { node ->
                key(node.id) {
                    EntityCard(
                        nodeId = node.id,
                        stateFlow = viewModel.container.stateFlow,
                        isSourceNode = (state.connectingFromNodeId == node.id) ||
                            (state.draggingEdgeFromNodeId == node.id),
                        isConnecting = isConnecting,
                        onDragStart = { viewModel.onIntent(BoardIntent.OnDragStart(node.id)) },
                        onDrag = { delta -> viewModel.onIntent(BoardIntent.OnDragNode(node.id, delta)) },
                        onDragEnd = { viewModel.onIntent(BoardIntent.OnDragEnd(node.id)) },
                        onTap = { viewModel.onIntent(BoardIntent.OnNodeMenu(node.id)) },
                        onLongPress = { viewModel.onIntent(BoardIntent.OnSelectNode(node.id)) },
                        highlightedFieldIds = highlightedFields
                    )
                }
            }

            // в”Ђв”Ђ Port dots (field-level only, left + right) в”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђ
            portPositions.forEach { (portKey, screenPos) ->
                val portIsSource =
                    (state.connectingFromNodeId == portKey.nodeId && state.connectingFromFieldId == portKey.fieldId) ||
                    (state.draggingEdgeFromNodeId == portKey.nodeId && state.draggingEdgeFromFieldId == portKey.fieldId)
                val isConnectedPort = "${portKey.nodeId}:${portKey.fieldId}" in connectedFieldKeys
                val dotColor = when {
                    portIsSource || isConnectedPort -> Color(0xFF111111)
                    isConnecting -> Color(0xFF888888)
                    else         -> Color(0xFFCCCCCC)
                }

                Box(
                    modifier = Modifier
                        .offset {
                            IntOffset(
                                (screenPos.x - 10.dp.toPx()).roundToInt(),
                                (screenPos.y - 10.dp.toPx()).roundToInt()
                            )
                        }
                        .size(20.dp)
                        .pointerInput(portKey.nodeId, portKey.fieldId, portKey.side) {
                            val hitPx = PORT_HIT_PX
                            val snapInPx = hitPx * EDGE_SNAP_IN_MULTIPLIER
                            val snapOutPx = hitPx * EDGE_SNAP_OUT_MULTIPLIER
                            val dotRadiusPx = 10.dp.toPx()
                            val boxTopLeft = Offset(
                                (screenPos.x - dotRadiusPx).roundToInt().toFloat(),
                                (screenPos.y - dotRadiusPx).roundToInt().toFloat(),
                            )
                            awaitEachGesture {
                                val down = awaitFirstDown()
                                down.consume()
                                var isDragging = false
                                var snappedTarget: Pair<PortKey, Offset>? = null
                                var latestLocalPos = down.position
                                var latestRawScreenPos = boxTopLeft + latestLocalPos
                                var latestRenderScreenPos = latestRawScreenPos

                                while (true) {
                                    val event = awaitPointerEvent()
                                    val change = event.changes.find { it.id == down.id } ?: break
                                    change.consume()
                                    latestLocalPos = change.position
                                    latestRawScreenPos = boxTopLeft + latestLocalPos

                                    val nearestSnapCandidate = findNearestTargetPort(
                                        pointer = latestRawScreenPos,
                                        sourceNodeId = portKey.nodeId,
                                        portPositions = latestPortPositions,
                                        maxDistancePx = snapInPx,
                                    )

                                    val activeSnap = snappedTarget
                                    snappedTarget = when {
                                        nearestSnapCandidate != null -> nearestSnapCandidate
                                        activeSnap != null -> {
                                            if ((latestRawScreenPos - activeSnap.second).getDistance() <= snapOutPx) {
                                                activeSnap
                                            } else {
                                                null
                                            }
                                        }
                                        else -> null
                                    }

                                    latestRenderScreenPos = snappedTarget?.second ?: latestRawScreenPos

                                    if (!change.pressed) {
                                        if (isDragging) {
                                            val finalTarget = snappedTarget ?: findNearestTargetPort(
                                                pointer = latestRawScreenPos,
                                                sourceNodeId = portKey.nodeId,
                                                portPositions = latestPortPositions,
                                                maxDistancePx = hitPx,
                                            )
                                            viewModel.onIntent(
                                                BoardIntent.OnEdgeDragEnd(finalTarget?.first?.nodeId, finalTarget?.first?.fieldId)
                                            )
                                        } else {
                                            viewModel.onIntent(BoardIntent.OnNodeFieldTap(portKey.nodeId, portKey.fieldId))
                                        }
                                        break
                                    }

                                    if (!isDragging &&
                                        (latestLocalPos - down.position).getDistance() > viewConfiguration.touchSlop
                                    ) {
                                        isDragging = true
                                        viewModel.onIntent(BoardIntent.OnEdgeDragStart(portKey.nodeId, portKey.fieldId))
                                    }
                                    if (isDragging) {
                                        viewModel.onIntent(BoardIntent.OnEdgeDragMove(latestRenderScreenPos))
                                    }
                                }
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Box(Modifier.size(8.dp).background(dotColor, CircleShape))
                }
            }

            // в”Ђв”Ђ Tap-to-connect hint в”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђ
            if (state.connectingFromNodeId != null) {
                val srcName = state.nodes[state.connectingFromNodeId]?.let { n ->
                    val field = n.fields.firstOrNull { it.id == state.connectingFromFieldId }
                    if (field != null) "${n.name}.${field.name}" else n.name
                }
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 16.dp)
                        .background(Color(0xFF111111), RoundedCornerShape(20.dp))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = if (srcName == null) "Tap a port to start" else "From: $srcName  ->  tap target",
                        color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium
                    )
                }
            }

            // в”Ђв”Ђ Edge selection toolbar в”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђ
            val selEdge = state.edges[state.selectedEdgeId]
            if (selEdge != null) {
                val mid = edgeMidpoints[selEdge.id]
                val tbX = ((mid?.x ?: (screenW / 2f)) - 60f).coerceIn(4f, screenW - 140f)
                val tbY = ((mid?.y ?: (screenH - 80f)) - 48f).coerceIn(4f, screenH - 48f)

                Box(
                    modifier = Modifier
                        .offset { IntOffset(tbX.roundToInt(), tbY.roundToInt()) }
                        .background(Color.White, RoundedCornerShape(20.dp))
                        .border(1.dp, Color(0xFFEEEEEE), RoundedCornerShape(20.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = buildEdgeLabel(selEdge, state.nodes),
                            fontSize = 11.sp,
                            color = Color(0xFF666666),
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                        TextButton(onClick = { viewModel.onIntent(BoardIntent.OnDeleteEdge(selEdge.id)) }) {
                            Text("Delete", color = Color(0xFFCC3333), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }

            // в”Ђв”Ђ Node action menu в”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђ
            val menuNode = state.nodes[state.nodeMenuNodeId]
            if (menuNode != null) {
                val cardRightPx = menuNode.position.x * state.scale + state.panOffset.x +
                    CARD_WIDTH_DP * density * state.scale
                val cardTopPx = menuNode.position.y * state.scale + state.panOffset.y

                Box(
                    modifier = Modifier
                        .offset {
                            IntOffset(
                                (cardRightPx + 6.dp.toPx()).roundToInt(),
                                cardTopPx.roundToInt()
                            )
                        }
                        .background(Color.White, RoundedCornerShape(10.dp))
                        .border(1.dp, Color(0xFFEEEEEE), RoundedCornerShape(10.dp))
                        .padding(vertical = 4.dp, horizontal = 2.dp)
                ) {
                    Column {
                        TextButton(onClick = {
                            viewModel.onIntent(BoardIntent.OnSelectNode(menuNode.id))
                        }) {
                            Text("Edit Fields", fontSize = 12.sp, color = Color(0xFF333333))
                        }
                        TextButton(onClick = {
                            viewModel.onIntent(BoardIntent.OnDeleteNode(menuNode.id))
                        }) {
                            Text("Delete", fontSize = 12.sp, color = Color(0xFFCC3333))
                        }
                    }
                }
            }

            // в”Ђв”Ђ Bottom toolbar в”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђ
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 24.dp)
                    .background(Color.White, RoundedCornerShape(28.dp))
                    .border(1.dp, Color(0xFFEEEEEE), RoundedCornerShape(28.dp))
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                TextButton(onClick = { newNodeName = ""; showAddNodeDialog = true }) {
                    Text("+ Add Entity", color = Color(0xFF111111), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        ) { data -> Snackbar(snackbarData = data) }
    }

    // в”Ђв”Ђ Add Entity dialog в”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђ
    if (showAddNodeDialog) {
        val panOffset = state.panOffset
        val scale = state.scale
        AlertDialog(
            onDismissRequest = { showAddNodeDialog = false },
            title = { Text("New Entity", fontWeight = FontWeight.SemiBold, color = Color(0xFF111111)) },
            text = {
                OutlinedTextField(
                    value = newNodeName, onValueChange = { newNodeName = it },
                    placeholder = { Text("Entity name", color = Color(0xFFAAAAAA)) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF111111),
                        unfocusedBorderColor = Color(0xFFCCCCCC),
                        cursorColor = Color(0xFF111111)
                    )
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showAddNodeDialog = false
                    val cx = (400f - panOffset.x) / scale
                    val cy = (300f - panOffset.y) / scale
                    viewModel.onIntent(BoardIntent.OnAddNode(newNodeName.ifBlank { "Entity" }, Position(cx, cy)))
                    newNodeName = ""
                }) { Text("Add", color = Color(0xFF111111), fontWeight = FontWeight.SemiBold) }
            },
            dismissButton = {
                TextButton(onClick = { showAddNodeDialog = false }) {
                    Text("Cancel", color = Color(0xFF888888))
                }
            },
            containerColor = Color.White, shape = RoundedCornerShape(12.dp)
        )
    }

    // в”Ђв”Ђ Field editor dialog (long-press) в”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђ
    val selNodeId = state.selectedNodeId
    val selNode = state.nodes[selNodeId]
    if (selNodeId != null && selNode != null) {
        NodeDetailDialog(
            nodeName = selNode.name,
            fields = selNode.fields,
            onAddField = { name, type -> viewModel.onIntent(BoardIntent.OnAddField(selNodeId, name, type)) },
            onRemoveField = { fieldId -> viewModel.onIntent(BoardIntent.OnRemoveField(selNodeId, fieldId)) },
            onDismiss = { viewModel.onIntent(BoardIntent.OnSelectNode(null)) }
        )
    }
}

private fun computeGridRenderConfig(
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

private fun Float.positiveMod(base: Float): Float {
    if (base == 0f) return this
    val m = this % base
    return if (m < 0f) m + base else m
}

private fun sampleCubicBezierPoints(
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

private fun findTappedEdgeId(
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

private fun findNearestTargetPort(
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
// в”Ђв”Ђ Port positions (field-level only, left + right) в”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђ

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

        // Field-level ports only (no entity-level ports at title)
        node.fields.forEachIndexed { idx, field ->
            val cy = by + headerH + dividerH + rowH * idx + rowH / 2f
            result[PortKey(node.id, field.id, side = true)]  = Offset(bx + cardW, cy)
            result[PortKey(node.id, field.id, side = false)] = Offset(bx, cy)
        }
    }
    return result
}

// в”Ђв”Ђ Edge anchor computation в”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђ

data class EdgeAnchors(val src: Offset, val tgt: Offset, val srcIsRight: Boolean, val tgtIsRight: Boolean)

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
        return if (fieldId == null) base + headerH / 2f
        else {
            val idx = node.fields.indexOfFirst { it.id == fieldId }.coerceAtLeast(0)
            base + headerH + dividerH + rowH * idx + rowH / 2f
        }
    }

    val srcLeft  = srcNode.position.x * scale + panOffset.x
    val srcRight = srcLeft + cardW
    val tgtLeft  = tgtNode.position.x * scale + panOffset.x
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

// в”Ђв”Ђ Combined canvas gesture handler в”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђ
// Handles: single-finger pan (LMB drag), two-finger pinch+pan, and tap.
// Single handler avoids race conditions between separate detectTapGestures
// and customPanZoom modifiers.

private suspend fun PointerInputScope.canvasGestureHandler(
    onPanZoom: (centroid: Offset, pan: Offset, zoom: Float) -> Unit,
    onTap: (Offset) -> Unit,
) {
    awaitEachGesture {
        val first = awaitFirstDown(requireUnconsumed = true)
        val downPos = first.position
        var pos1 = first.position
        var secondId = Long.MIN_VALUE
        var pos2: Offset? = null
        var movedPastSlop = false

        while (true) {
            val event = awaitPointerEvent()

            // Detect second finger
            for (c in event.changes) {
                if (c.pressed && !c.previousPressed && c.id.value != first.id.value && secondId == Long.MIN_VALUE) {
                    secondId = c.id.value
                    pos2 = c.position
                    movedPastSlop = true  // two-finger always counts as gesture
                }
            }

            val c1 = event.changes.firstOrNull { it.id == first.id }
            if (c1 == null || !c1.pressed) {
                // Released вЂ” if never moved past slop, it's a tap
                if (!movedPastSlop) {
                    onTap(downPos)
                }
                break
            }

            val c2 = if (secondId != Long.MIN_VALUE)                event.changes.firstOrNull { it.id.value == secondId } else null

            val newPos1 = c1.position
            val newPos2 = if (c2?.pressed == true) c2.position else null

            if (!movedPastSlop) {
                if ((newPos1 - downPos).getDistance() > viewConfiguration.touchSlop) {
                    movedPastSlop = true
                }
            }

            if (movedPastSlop) {
                if (newPos2 != null && pos2 != null) {
                    // Two-finger: pinch zoom + pan
                    val centroid = (newPos1 + newPos2) / 2f
                    val prevCentroid = (pos1 + pos2!!) / 2f
                    val prevDist = (pos1 - pos2!!).getDistance()
                    val newDist = (newPos1 - newPos2).getDistance()
                    val zoom = if (prevDist > 1f) newDist / prevDist else 1f
                    event.changes.forEach { it.consume() }
                    onPanZoom(centroid, centroid - prevCentroid, zoom)
                    pos2 = newPos2
                } else {
                    // Single finger: pan
                    val pan = newPos1 - pos1
                    if (pan.getDistance() > 0f) {
                        c1.consume()
                        onPanZoom(newPos1, pan, 1f)
                    }
                }
            }

            pos1 = newPos1
            if (c2?.pressed == false) { secondId = Long.MIN_VALUE; pos2 = null }
        }
    }
}

// в”Ђв”Ђ Edge label helper в”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђ

private fun buildEdgeLabel(edge: RelationEdge, nodes: Map<String, EntityNode>): String {
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

// в”Ђв”Ђ Field editor dialog в”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђ

@Composable
private fun NodeDetailDialog(
    nodeName: String,
    fields: List<NodeField>,
    onAddField: (name: String, type: FieldType) -> Unit,
    onRemoveField: (fieldId: String) -> Unit,
    onDismiss: () -> Unit,
) {
    var newFieldName by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(FieldType.TEXT) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        shape = RoundedCornerShape(12.dp),
        title = { Text(nodeName, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = Color(0xFF111111)) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                if (fields.isNotEmpty()) {
                    Text("Fields", fontSize = 11.sp, fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFAAAAAA), modifier = Modifier.padding(bottom = 4.dp))
                    fields.forEach { field ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(field.name, fontSize = 13.sp, color = Color(0xFF333333), modifier = Modifier.weight(1f))
                            Text(field.type.name.lowercase(), fontSize = 11.sp, color = Color(0xFFAAAAAA),
                                modifier = Modifier.padding(horizontal = 8.dp))
                            TextButton(onClick = { onRemoveField(field.id) }) {
                                Text("x", color = Color(0xFFCCCCCC), fontSize = 12.sp)
                            }
                        }
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color(0xFFEEEEEE))
                }
                Text("Add field", fontSize = 11.sp, fontWeight = FontWeight.SemiBold,
                    color = Color(0xFFAAAAAA), modifier = Modifier.padding(bottom = 6.dp))
                OutlinedTextField(
                    value = newFieldName, onValueChange = { newFieldName = it },
                    placeholder = { Text("Field name", color = Color(0xFFCCCCCC)) },
                    singleLine = true, modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF111111),
                        unfocusedBorderColor = Color(0xFFDDDDDD),
                        cursorColor = Color(0xFF111111)
                    )
                )
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FieldType.entries.forEach { type ->
                        val sel = selectedType == type
                        TextButton(
                            onClick = { selectedType = type },
                            modifier = Modifier.background(
                                if (sel) Color(0xFF111111) else Color(0xFFF0F0F0), RoundedCornerShape(16.dp)
                            )
                        ) {
                            Text(type.name.lowercase(), fontSize = 11.sp,
                                color = if (sel) Color.White else Color(0xFF555555))
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                TextButton(
                    onClick = {
                        if (newFieldName.isNotBlank()) { onAddField(newFieldName.trim(), selectedType); newFieldName = "" }
                    },
                    modifier = Modifier.fillMaxWidth().background(Color(0xFF111111), RoundedCornerShape(8.dp))
                ) {
                    Text("Add field", color = Color.White, fontWeight = FontWeight.SemiBold)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Done", color = Color(0xFF111111), fontWeight = FontWeight.SemiBold)
            }
        }
    )
}
