package com.dadadadev.prototype_me.erd.board.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.isPrimaryPressed
import androidx.compose.ui.input.pointer.isSecondaryPressed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.dadadadev.prototype_me.erd.board.presentation.BoardIntent
import com.dadadadev.prototype_me.erd.board.presentation.BoardSideEffect
import com.dadadadev.prototype_me.erd.board.presentation.BoardViewModel
import com.dadadadev.prototype_me.erd.board.ui.components.AddEntityToolbar
import com.dadadadev.prototype_me.erd.board.ui.components.ConnectingHintBanner
import com.dadadadev.prototype_me.erd.board.ui.components.EdgesLayer
import com.dadadadev.prototype_me.erd.board.ui.components.EdgeSelectionToolbar
import com.dadadadev.prototype_me.erd.board.ui.components.GridBackground
import com.dadadadev.prototype_me.erd.board.ui.components.MultiSelectMenu
import com.dadadadev.prototype_me.erd.board.ui.components.NodeActionMenu
import com.dadadadev.prototype_me.erd.board.ui.components.NodesLayer
import com.dadadadev.prototype_me.erd.board.ui.components.SelectionMarquee
import com.dadadadev.prototype_me.erd.board.ui.dialogs.AddEntityDialog
import com.dadadadev.prototype_me.erd.board.ui.dialogs.NodeDetailDialog
import com.dadadadev.prototype_me.domains.erd.design.api.domain.model.Position
import org.koin.compose.viewmodel.koinViewModel
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect
import kotlin.math.abs

@Composable
fun BoardScreen(viewModel: BoardViewModel = koinViewModel()) {
    val state by viewModel.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val density = LocalDensity.current.density

    // Marquee selection local state (not part of MVI — purely UI ephemeral)
    var marqueeStart by remember { mutableStateOf<Offset?>(null) }
    var marqueeCurrent by remember { mutableStateOf<Offset?>(null) }
    var marqueeSelectedNodeIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    var multiSelectMenuPos by remember { mutableStateOf<Offset?>(null) }
    var showAddNodeDialog by remember { mutableStateOf(false) }

    val selectedNodeIds = remember(marqueeSelectedNodeIds, state.nodes) {
        marqueeSelectedNodeIds.filterTo(mutableSetOf()) { state.nodes.containsKey(it) }
    }
    val isConnecting = state.connectingFromNodeId != null || state.draggingEdgeFromNodeId != null

    // Port screen positions — derived from state, passed down to avoid recomputation
    val portPositions = remember(state.nodes, state.scale, state.panOffset, density) {
        computeAllPortPositions(state.nodes, state.scale, state.panOffset, density)
    }

    // Edge polylines for full-path hit-testing and toolbar mid-point placement
    val edgeHitPolylines = remember(state.edges, state.nodes, state.scale, state.panOffset, density) {
        buildEdgeHitPolylines(state.edges.values.toList(), state.nodes, state.scale, state.panOffset, density)
    }
    val edgeMidpoints = remember(edgeHitPolylines) {
        edgeHitPolylines.mapValues { (_, points) -> points.getOrNull(points.size / 2) }
    }

    // Fields highlighted by the currently selected edge
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

    // Stable refs for gesture callbacks
    val latestPortPositions by rememberUpdatedState(portPositions)
    val latestEdgeHitPolylines by rememberUpdatedState(edgeHitPolylines)
    val latestState by rememberUpdatedState(state)
    val latestSelectedNodeIds by rememberUpdatedState(selectedNodeIds)

    val portHitPx = 20f * density
    val edgeHitPx = 20f * density

    viewModel.collectSideEffect { effect ->
        when (effect) {
            is BoardSideEffect.ShowLockError ->
                snackbarHostState.showSnackbar("Locked by ${effect.lockedBy}")
            is BoardSideEffect.ShowConnectionLost ->
                snackbarHostState.showSnackbar("Connection lost - reconnecting...")
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                // Mouse-wheel zoom
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent()
                            if (event.type != PointerEventType.Scroll) continue
                            val change = event.changes.firstOrNull() ?: continue
                            val dy = change.scrollDelta.y
                            if (dy == 0f) continue
                            change.consume()
                            val factor = if (dy < 0f) 1.12f else 0.88f
                            viewModel.onIntent(BoardIntent.OnPanZoom(change.position, Offset.Zero, factor))
                        }
                    }
                }
                // RMB pan + multi-select context menu
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        var previousMousePos: Offset? = null
                        var secondaryDownPos: Offset? = null
                        var secondaryMoved = false
                        while (true) {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull() ?: continue
                            if (event.buttons.isSecondaryPressed) {
                                if (secondaryDownPos == null) {
                                    secondaryDownPos = change.position
                                    previousMousePos = change.position
                                    secondaryMoved = false
                                    continue
                                }
                                val prev = previousMousePos ?: change.position
                                val pan = change.position - prev
                                if (pan.getDistance() > 0f) {
                                    if (!secondaryMoved) {
                                        val start = checkNotNull(secondaryDownPos)
                                        if ((change.position - start).getDistance() > viewConfiguration.touchSlop) {
                                            secondaryMoved = true
                                        }
                                    }
                                    if (secondaryMoved) {
                                        multiSelectMenuPos = null
                                        change.consume()
                                        viewModel.onIntent(BoardIntent.OnPan(pan))
                                    }
                                }
                                previousMousePos = change.position
                            } else {
                                if (secondaryDownPos != null && !secondaryMoved) {
                                    if (latestSelectedNodeIds.isNotEmpty()) {
                                        multiSelectMenuPos = change.position
                                        viewModel.onIntent(BoardIntent.OnNodeMenu(null))
                                        viewModel.onIntent(BoardIntent.OnSelectEdge(null))
                                    } else {
                                        multiSelectMenuPos = null
                                    }
                                }
                                previousMousePos = null
                                secondaryDownPos = null
                                secondaryMoved = false
                            }
                        }
                    }
                }
                // LMB tap + two-finger pan/pinch (touch)
                .pointerInput(Unit) {
                    canvasGestureHandler(
                        onPanZoom = { centroid, pan, zoom ->
                            viewModel.onIntent(BoardIntent.OnPanZoom(centroid, pan, zoom))
                        },
                        onTap = { tap ->
                            val tappedEdgeId = findTappedEdgeId(tap, latestEdgeHitPolylines, edgeHitPx)
                            val onPort = latestPortPositions.values.any { pos ->
                                (tap - pos).getDistance() < portHitPx
                            }
                            when {
                                tappedEdgeId != null -> {
                                    marqueeSelectedNodeIds = emptySet()
                                    multiSelectMenuPos = null
                                    viewModel.onIntent(BoardIntent.OnSelectEdge(tappedEdgeId))
                                }
                                !onPort -> {
                                    marqueeSelectedNodeIds = emptySet()
                                    multiSelectMenuPos = null
                                    viewModel.onIntent(BoardIntent.OnSelectEdge(null))
                                    viewModel.onIntent(BoardIntent.OnNodeMenu(null))
                                    if (latestState.connectingFromNodeId != null)
                                        viewModel.onIntent(BoardIntent.OnCancelConnect)
                                    if (latestState.draggingEdgeFromNodeId != null)
                                        viewModel.onIntent(BoardIntent.OnEdgeDragEnd(null, null))
                                }
                            }
                        },
                    )
                },
        ) {
            val screenW = constraints.maxWidth.toFloat()
            val screenH = constraints.maxHeight.toFloat()
            val marqueeRect: Rect? = marqueeStart?.let { start ->
                marqueeCurrent?.let { current -> buildSelectionRect(start, current) }
            }

            // ── Background layers ─────────────────────────────────────────────
            GridBackground(scale = state.scale, panOffset = state.panOffset)

            EdgesLayer(
                edges = state.edges,
                nodes = state.nodes,
                scale = state.scale,
                panOffset = state.panOffset,
                density = density,
                selectedEdgeId = state.selectedEdgeId,
                portPositions = portPositions,
                draggingEdgeFromNodeId = state.draggingEdgeFromNodeId,
                draggingEdgeFromFieldId = state.draggingEdgeFromFieldId,
                draggingEdgeCurrentPos = state.draggingEdgeCurrentPos,
                draggingEdgeSnapTargetNodeId = state.draggingEdgeSnapTargetNodeId,
                draggingEdgeSnapTargetFieldId = state.draggingEdgeSnapTargetFieldId,
                draggingEdgeSnapTargetIsRight = state.draggingEdgeSnapTargetIsRight,
            )

            // Marquee gesture overlay — sits between background and node layer,
            // so card pointer events naturally take priority.
            androidx.compose.foundation.Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        awaitPointerEventScope {
                            var primaryDownPos: Offset? = null
                            var isSelecting = false
                            while (true) {
                                val event = awaitPointerEvent()
                                val change = event.changes.firstOrNull() ?: continue
                                if (event.buttons.isPrimaryPressed && !event.buttons.isSecondaryPressed) {
                                    if (primaryDownPos == null) {
                                        primaryDownPos = change.position
                                        marqueeStart = change.position
                                        marqueeCurrent = change.position
                                        isSelecting = false
                                        multiSelectMenuPos = null
                                        continue
                                    }
                                    val start = checkNotNull(primaryDownPos)
                                    val current = change.position
                                    if (!isSelecting && (current - start).getDistance() > viewConfiguration.touchSlop) {
                                        isSelecting = true
                                        viewModel.onIntent(BoardIntent.OnSelectEdge(null))
                                        viewModel.onIntent(BoardIntent.OnNodeMenu(null))
                                    }
                                    if (isSelecting) {
                                        change.consume()
                                        marqueeCurrent = current
                                        val rect = buildSelectionRect(start, current)
                                        marqueeSelectedNodeIds = findNodesIntersectingRect(
                                            rect = rect,
                                            nodes = latestState.nodes,
                                            scale = latestState.scale,
                                            panOffset = latestState.panOffset,
                                            density = density,
                                        )
                                    }
                                } else {
                                    if (primaryDownPos != null) {
                                        marqueeStart = null
                                        marqueeCurrent = null
                                    }
                                    primaryDownPos = null
                                    isSelecting = false
                                }
                            }
                        }
                    }
            ) { /* transparent hit area — drawing handled by SelectionMarquee */ }

            SelectionMarquee(marqueeRect = marqueeRect)

            // ── Node + port layer ─────────────────────────────────────────────
            NodesLayer(
                nodes = state.nodes,
                scale = state.scale,
                panOffset = state.panOffset,
                portPositions = portPositions,
                connectingFromNodeId = state.connectingFromNodeId,
                connectingFromFieldId = state.connectingFromFieldId,
                draggingEdgeFromNodeId = state.draggingEdgeFromNodeId,
                draggingEdgeFromFieldId = state.draggingEdgeFromFieldId,
                nodeMenuNodeId = state.nodeMenuNodeId,
                selectedNodeIds = selectedNodeIds,
                highlightedFieldIds = highlightedFields,
                connectedFieldKeys = connectedFieldKeys,
                isConnecting = isConnecting,
                portHitPx = portHitPx,
                onIntent = viewModel::onIntent,
            )

            // ── Overlays: hints, toolbars, menus ─────────────────────────────

            ConnectingHintBanner(
                connectingFromNodeId = state.connectingFromNodeId,
                connectingFromFieldId = state.connectingFromFieldId,
                nodes = state.nodes,
                modifier = Modifier.align(Alignment.TopCenter).padding(top = 16.dp),
            )

            state.edges[state.selectedEdgeId]?.let { selEdge ->
                EdgeSelectionToolbar(
                    edge = selEdge,
                    midpoint = edgeMidpoints[selEdge.id],
                    screenW = screenW,
                    screenH = screenH,
                    nodes = state.nodes,
                    onDeleteEdge = { viewModel.onIntent(BoardIntent.OnDeleteEdge(selEdge.id)) },
                )
            }

            state.nodes[state.nodeMenuNodeId]?.let { menuNode ->
                NodeActionMenu(
                    node = menuNode,
                    scale = state.scale,
                    panOffset = state.panOffset,
                    density = density,
                    onEditFields = { viewModel.onIntent(BoardIntent.OnSelectNode(menuNode.id)) },
                    onDelete = { viewModel.onIntent(BoardIntent.OnDeleteNode(menuNode.id)) },
                )
            }

            multiSelectMenuPos?.let { anchor ->
                if (selectedNodeIds.isNotEmpty()) {
                    MultiSelectMenu(
                        anchorPos = anchor,
                        selectedCount = selectedNodeIds.size,
                        screenW = screenW,
                        screenH = screenH,
                        onDeleteAll = {
                            val idsToDelete = selectedNodeIds.toList()
                            marqueeSelectedNodeIds = emptySet()
                            multiSelectMenuPos = null
                            idsToDelete.forEach { viewModel.onIntent(BoardIntent.OnDeleteNode(it)) }
                        },
                    )
                }
            }

            AddEntityToolbar(
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 24.dp),
                onAddEntity = { showAddNodeDialog = true },
            )
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter),
        ) { data -> Snackbar(snackbarData = data) }
    }

    // ── Dialogs ───────────────────────────────────────────────────────────────

    if (showAddNodeDialog) {
        AddEntityDialog(
            onConfirm = { name ->
                showAddNodeDialog = false
                val cx = (400f - state.panOffset.x) / state.scale
                val cy = (300f - state.panOffset.y) / state.scale
                viewModel.onIntent(BoardIntent.OnAddNode(name, Position(cx, cy)))
            },
            onDismiss = { showAddNodeDialog = false },
        )
    }

    state.selectedNodeId?.let { selNodeId ->
        state.nodes[selNodeId]?.let { selNode ->
            NodeDetailDialog(
                nodeName = selNode.name,
                fields = selNode.fields,
                onAddField = { name, type ->
                    viewModel.onIntent(BoardIntent.OnAddField(selNodeId, name, type))
                },
                onRemoveField = { fieldId ->
                    viewModel.onIntent(BoardIntent.OnRemoveField(selNodeId, fieldId))
                },
                onDismiss = { viewModel.onIntent(BoardIntent.OnSelectNode(null)) },
            )
        }
    }
}

// ── Private helpers ───────────────────────────────────────────────────────────

/**
 * Builds a map of edgeId -> sampled bezier points used for hit-testing and
 * mid-point calculation (toolbar placement). Accounts for multi-edge spread.
 */
private fun buildEdgeHitPolylines(
    edges: List<com.dadadadev.prototype_me.domains.erd.design.api.domain.model.RelationEdge>,
    nodes: Map<String, com.dadadadev.prototype_me.domains.erd.design.api.domain.model.EntityNode>,
    scale: Float,
    panOffset: Offset,
    density: Float,
): Map<String, List<Offset>> {
    val targetPortCount = mutableMapOf<String, Int>()
    val edgePortIndex = mutableMapOf<String, Int>()
    edges.forEach { edge ->
        val key = "${edge.targetNodeId}:${edge.targetFieldId ?: "h"}"
        val idx = targetPortCount[key] ?: 0
        edgePortIndex[edge.id] = idx
        targetPortCount[key] = idx + 1
    }
    return edges.mapNotNull { edge ->
        val anchors = computeEdgeAnchors(edge, nodes, scale, panOffset, density) ?: return@mapNotNull null
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


