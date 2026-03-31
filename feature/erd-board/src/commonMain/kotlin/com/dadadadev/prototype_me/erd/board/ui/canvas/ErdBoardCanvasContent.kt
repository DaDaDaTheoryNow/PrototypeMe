package com.dadadadev.prototype_me.erd.board.ui.canvas

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.input.pointer.isPrimaryPressed
import androidx.compose.ui.input.pointer.isSecondaryPressed
import androidx.compose.ui.input.pointer.pointerInput
import com.dadadadev.prototype_me.erd.board.presentation.contract.ErdBoardIntent
import com.dadadadev.prototype_me.erd.board.presentation.contract.ErdBoardState
import com.dadadadev.prototype_me.erd.board.presentation.contract.ErdBoardVector
import com.dadadadev.prototype_me.erd.board.ui.components.EdgesLayer
import com.dadadadev.prototype_me.erd.board.ui.components.NodesLayer
import com.dadadadev.prototype_me.erd.board.config.ErdBoardConfig
import com.dadadadev.prototype_me.erd.board.ui.mappers.toBoardVector
import com.dadadadev.prototype_me.erd.board.ui.mappers.toOffset
import com.dadadadev.prototype_me.erd.board.ui.theme.ErdBoardColors
import com.dadadadev.prototype_me.feature.board.core.ui.viewport.BoardInitialViewportFitEffect
import com.dadadadev.prototype_me.feature.board.core.ui.input.boardMouseWheelZoom
import com.dadadadev.prototype_me.feature.board.core.ui.input.boardSecondaryButtonPan
import com.dadadadev.prototype_me.feature.board.core.ui.viewport.buildSelectionRect
import com.dadadadev.prototype_me.feature.board.core.ui.input.canvasGestureHandler
import com.dadadadev.prototype_me.feature.board.core.ui.viewport.computeBoardEntityBounds
import com.dadadadev.prototype_me.feature.board.core.ui.viewport.findIntersectingBoardEntityIds
import com.dadadadev.prototype_me.feature.board.core.ui.components.BoardGridBackground
import com.dadadadev.prototype_me.feature.board.core.ui.components.BoardSelectionMarquee

@Composable
internal fun ErdBoardCanvasContent(
    state: ErdBoardState,
    density: Float,
    renderData: ErdBoardRenderData,
    selectedNodeIds: Set<String>,
    showAddNodeDialog: Boolean,
    onShowAddNodeDialogChange: (Boolean) -> Unit,
    showJsonDialog: Boolean,
    onShowJsonDialogChange: (Boolean) -> Unit,
    showShareDialog: Boolean,
    onShowShareDialogChange: (Boolean) -> Unit,
    boardId: String,
    currentBoardJson: String?,
    onImportBoardJson: (String) -> Unit,
    marqueeStart: Offset?,
    marqueeCurrent: Offset?,
    onMarqueeStartChange: (Offset?) -> Unit,
    onMarqueeCurrentChange: (Offset?) -> Unit,
    onMarqueeSelectionChange: (Set<String>) -> Unit,
    multiSelectMenuPos: Offset?,
    onMultiSelectMenuPosChange: (Offset?) -> Unit,
    hasAppliedInitialViewportFit: Boolean,
    onInitialViewportFitApplied: () -> Unit,
    onIntent: (ErdBoardIntent) -> Unit,
) {
    val latestState by rememberUpdatedState(state)
    val latestRenderedNodes by rememberUpdatedState(renderData.renderedNodes)
    val latestSelectedNodeIds by rememberUpdatedState(selectedNodeIds)
    val latestPortPositions by rememberUpdatedState(renderData.portPositions)
    val latestEdgeHitPolylines by rememberUpdatedState(renderData.edgeHitPolylines)

    val portHitPx = ErdBoardConfig.HIT_RADIUS_DP * density
    val edgeHitPx = ErdBoardConfig.HIT_RADIUS_DP * density

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .boardMouseWheelZoom { centroid, zoom ->
                onIntent(
                    ErdBoardIntent.OnPanZoom(
                        centroid = centroid.toBoardVector(),
                        pan = ErdBoardVector.Zero,
                        zoom = zoom,
                    ),
                )
            }
            .boardSecondaryButtonPan(
                onPan = { pan ->
                    onMultiSelectMenuPosChange(null)
                    onIntent(ErdBoardIntent.OnPan(pan.toBoardVector()))
                },
                onClick = { position ->
                    onMultiSelectMenuPosChange(
                        if (latestSelectedNodeIds.isNotEmpty()) {
                            onIntent(ErdBoardIntent.OnNodeMenu(null))
                            onIntent(ErdBoardIntent.OnSelectEdge(null))
                            position
                        } else {
                            null
                        },
                    )
                },
            )
            .pointerInput(Unit) {
                canvasGestureHandler(
                    onPanZoom = { centroid, pan, zoom ->
                        onIntent(
                            ErdBoardIntent.OnPanZoom(
                                centroid = centroid.toBoardVector(),
                                pan = pan.toBoardVector(),
                                zoom = zoom,
                            ),
                        )
                    },
                    onTap = { tap ->
                        val tappedEdgeId = findTappedEdgeId(tap, latestEdgeHitPolylines, edgeHitPx)
                        val onPort = latestPortPositions.values.any { position ->
                            (tap - position).getDistance() < portHitPx
                        }
                        val onNode = findTopNodeAt(
                            pointer = tap,
                            nodes = latestRenderedNodes,
                            scale = latestState.scale,
                            panOffset = latestState.panOffset.toOffset(),
                            density = density,
                        ) != null

                        when {
                            tappedEdgeId != null -> {
                                onMarqueeSelectionChange(emptySet())
                                onMultiSelectMenuPosChange(null)
                                onIntent(ErdBoardIntent.OnSelectEdge(tappedEdgeId))
                            }

                            !onPort && !onNode -> {
                                onMarqueeSelectionChange(emptySet())
                                onMultiSelectMenuPosChange(null)
                                onIntent(ErdBoardIntent.OnSelectEdge(null))
                                onIntent(ErdBoardIntent.OnNodeMenu(null))
                                if (latestState.connectingFromNodeId != null) {
                                    onIntent(ErdBoardIntent.OnCancelConnect)
                                }
                                if (latestState.draggingEdgeFromNodeId != null) {
                                    onIntent(ErdBoardIntent.OnEdgeDragEnd(null, null))
                                }
                            }
                        }
                    },
                )
            },
    ) {
        val screenWidth = constraints.maxWidth.toFloat()
        val screenHeight = constraints.maxHeight.toFloat()
        val marqueeRect: Rect? = marqueeStart?.let { start ->
            marqueeCurrent?.let { current -> buildSelectionRect(start, current) }
        }

        BoardInitialViewportFitEffect(
            items = state.nodes.values,
            viewportWidth = screenWidth,
            viewportHeight = screenHeight,
            density = density,
            hasApplied = hasAppliedInitialViewportFit,
            minScale = ErdBoardConfig.MIN_SCALE,
            maxScale = ErdBoardConfig.MAX_SCALE,
            boundsOf = ::computeBoardEntityBounds,
            onTransformComputed = { viewportTransform ->
                onInitialViewportFitApplied()
                onIntent(
                    ErdBoardIntent.OnSetViewTransform(
                        scale = viewportTransform.scale,
                        panOffset = viewportTransform.panOffset.toBoardVector(),
                    ),
                )
            },
        )

        BoardGridBackground(
            scale = state.scale,
            density = density,
            panOffset = state.panOffset.toOffset(),
            dotColor = ErdBoardColors.gridDot,
        )

        EdgesLayer(
            edges = state.edges,
            nodes = renderData.renderedNodes,
            scale = state.scale,
            panOffset = state.panOffset.toOffset(),
            density = density,
            selectedEdgeId = state.selectedEdgeId,
            edgeSideOrientations = renderData.edgeSideOrientations,
            portPositions = renderData.portPositions,
            draggingEdgeFromNodeId = state.draggingEdgeFromNodeId,
            draggingEdgeFromFieldId = state.draggingEdgeFromFieldId,
            draggingEdgeCurrentPos = state.draggingEdgeCurrentPos.toOffset(),
            draggingEdgeSnapTargetNodeId = state.draggingEdgeSnapTargetNodeId,
            draggingEdgeSnapTargetFieldId = state.draggingEdgeSnapTargetFieldId,
            draggingEdgeSnapTargetIsRight = state.draggingEdgeSnapTargetIsRight,
        )

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        var primaryDownPosition: Offset? = null
                        var isSelecting = false
                        var skippingNodeGesture = false

                        while (true) {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull() ?: continue

                            if (event.buttons.isPrimaryPressed && !event.buttons.isSecondaryPressed) {
                                if (primaryDownPosition == null) {
                                    val downOnPort = isPointerOnPort(
                                        pointer = change.position,
                                        portPositions = latestPortPositions,
                                        portTargetRadiusPx = portHitPx,
                                    )
                                    val downOnNode = findTopNodeAt(
                                        pointer = change.position,
                                        nodes = latestRenderedNodes,
                                        scale = latestState.scale,
                                        panOffset = latestState.panOffset.toOffset(),
                                        density = density,
                                    ) != null
                                    if (downOnPort || downOnNode) {
                                        skippingNodeGesture = true
                                        continue
                                    }

                                    skippingNodeGesture = false
                                    primaryDownPosition = change.position
                                    onMarqueeStartChange(change.position)
                                    onMarqueeCurrentChange(change.position)
                                    isSelecting = false
                                    onMultiSelectMenuPosChange(null)
                                    continue
                                }

                                if (skippingNodeGesture) {
                                    continue
                                }

                                val start = primaryDownPosition
                                val current = change.position
                                if (!isSelecting && (current - start).getDistance() > viewConfiguration.touchSlop) {
                                    isSelecting = true
                                    onIntent(ErdBoardIntent.OnSelectEdge(null))
                                    onIntent(ErdBoardIntent.OnNodeMenu(null))
                                }

                                if (isSelecting) {
                                    change.consume()
                                    onMarqueeCurrentChange(current)
                                    val rect = buildSelectionRect(start, current)
                                    onMarqueeSelectionChange(
                                        findIntersectingBoardEntityIds(
                                            rect = rect,
                                            entities = latestState.nodes.values,
                                            scale = latestState.scale,
                                            panOffset = latestState.panOffset.toOffset(),
                                            density = density,
                                        ),
                                    )
                                }
                            } else {
                                if (primaryDownPosition != null) {
                                    onMarqueeStartChange(null)
                                    onMarqueeCurrentChange(null)
                                }
                                primaryDownPosition = null
                                isSelecting = false
                                skippingNodeGesture = false
                            }
                        }
                    }
                },
        ) {}

        BoardSelectionMarquee(marqueeRect = marqueeRect)

        NodesLayer(
            nodes = renderData.renderedNodes,
            scale = state.scale,
            panOffset = state.panOffset.toOffset(),
            density = density,
            portPositions = renderData.portPositions,
            connectingFromNodeId = state.connectingFromNodeId,
            connectingFromFieldId = state.connectingFromFieldId,
            draggingEdgeFromNodeId = state.draggingEdgeFromNodeId,
            draggingEdgeFromFieldId = state.draggingEdgeFromFieldId,
            nodeMenuNodeId = state.nodeMenuNodeId,
            selectedNodeIds = selectedNodeIds,
            highlightedFieldIds = renderData.highlightedFieldIds,
            connectedFieldKeys = renderData.connectedFieldKeys,
            isConnecting = renderData.isConnecting,
            portHitPx = portHitPx,
            onIntent = onIntent,
        )

        ErdBoardCanvasOverlays(
            state = state,
            density = density,
            screenWidth = screenWidth,
            screenHeight = screenHeight,
            selectedNodeIds = selectedNodeIds,
            renderData = renderData,
            showAddNodeDialog = showAddNodeDialog,
            onShowAddNodeDialogChange = onShowAddNodeDialogChange,
            showJsonDialog = showJsonDialog,
            onShowJsonDialogChange = onShowJsonDialogChange,
            showShareDialog = showShareDialog,
            onShowShareDialogChange = onShowShareDialogChange,
            boardId = boardId,
            currentBoardJson = currentBoardJson,
            onImportBoardJson = onImportBoardJson,
            multiSelectMenuPos = multiSelectMenuPos,
            onMultiSelectMenuPosChange = onMultiSelectMenuPosChange,
            onMarqueeSelectionChange = onMarqueeSelectionChange,
            onIntent = onIntent,
        )
    }
}
