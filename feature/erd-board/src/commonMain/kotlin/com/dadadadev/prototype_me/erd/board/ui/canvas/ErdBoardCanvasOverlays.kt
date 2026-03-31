package com.dadadadev.prototype_me.erd.board.ui.canvas

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp
import com.dadadadev.prototype_me.domains.erd.design.api.domain.model.Position
import com.dadadadev.prototype_me.erd.board.presentation.contract.ErdBoardIntent
import com.dadadadev.prototype_me.erd.board.presentation.contract.ErdBoardState
import com.dadadadev.prototype_me.erd.board.ui.components.AddEntityToolbar
import com.dadadadev.prototype_me.erd.board.ui.components.ConnectingHintBanner
import com.dadadadev.prototype_me.erd.board.ui.components.EdgeSelectionToolbar
import com.dadadadev.prototype_me.erd.board.ui.components.MultiSelectMenu
import com.dadadadev.prototype_me.erd.board.ui.components.NodeActionMenu
import com.dadadadev.prototype_me.erd.board.ui.dimens.ErdBoardDimens
import com.dadadadev.prototype_me.erd.board.ui.dialogs.AddEntityDialog
import com.dadadadev.prototype_me.erd.board.ui.dialogs.BoardJsonDialog
import com.dadadadev.prototype_me.erd.board.ui.dialogs.BoardShareDialog
import com.dadadadev.prototype_me.erd.board.ui.dialogs.NodeDetailDialog
import com.dadadadev.prototype_me.erd.board.ui.mappers.toOffset
import com.dadadadev.prototype_me.feature.board.core.ui.viewport.screenToBoardOffset

@Composable
internal fun BoxScope.ErdBoardCanvasOverlays(
    state: ErdBoardState,
    density: Float,
    screenWidth: Float,
    screenHeight: Float,
    selectedNodeIds: Set<String>,
    renderData: ErdBoardRenderData,
    showAddNodeDialog: Boolean,
    onShowAddNodeDialogChange: (Boolean) -> Unit,
    showJsonDialog: Boolean,
    onShowJsonDialogChange: (Boolean) -> Unit,
    showShareDialog: Boolean,
    onShowShareDialogChange: (Boolean) -> Unit,
    boardId: String,
    currentBoardJson: String?,
    onImportBoardJson: (String) -> Unit,
    multiSelectMenuPos: Offset?,
    onMultiSelectMenuPosChange: (Offset?) -> Unit,
    onMarqueeSelectionChange: (Set<String>) -> Unit,
    onIntent: (ErdBoardIntent) -> Unit,
) {
    ConnectingHintBanner(
        connectingFromNodeId = state.connectingFromNodeId,
        connectingFromFieldId = state.connectingFromFieldId,
        nodes = renderData.renderedNodes,
        modifier = Modifier
            .align(Alignment.TopCenter)
            .padding(top = ErdBoardDimens.OVERLAY_CONNECT_HINT_TOP_DP.dp),
    )

    state.edges[state.selectedEdgeId]?.let { selectedEdge ->
        EdgeSelectionToolbar(
            edge = selectedEdge,
            midpoint = renderData.edgeMidpoints[selectedEdge.id],
            screenW = screenWidth,
            screenH = screenHeight,
            nodes = renderData.renderedNodes,
            onDeleteEdge = { onIntent(ErdBoardIntent.OnDeleteEdge(selectedEdge.id)) },
        )
    }

    renderData.renderedNodes[state.nodeMenuNodeId]?.let { menuNode ->
        NodeActionMenu(
            node = menuNode,
            scale = state.scale,
            panOffset = state.panOffset.toOffset(),
            density = density,
            onEditFields = { onIntent(ErdBoardIntent.OnSelectNode(menuNode.id)) },
            onDelete = { onIntent(ErdBoardIntent.OnDeleteNode(menuNode.id)) },
        )
    }

    multiSelectMenuPos?.takeIf { selectedNodeIds.isNotEmpty() }?.let { anchor ->
        MultiSelectMenu(
            anchorPos = anchor,
            selectedCount = selectedNodeIds.size,
            screenW = screenWidth,
            screenH = screenHeight,
            onCopy = {
                onIntent(ErdBoardIntent.OnCopy(selectedNodeIds))
                onMultiSelectMenuPosChange(null)
            },
            onDeleteAll = {
                val idsToDelete = selectedNodeIds.toSet()
                onMarqueeSelectionChange(emptySet())
                onMultiSelectMenuPosChange(null)
                onIntent(ErdBoardIntent.OnDeleteNodes(idsToDelete))
            },
        )
    }

    AddEntityToolbar(
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .padding(bottom = ErdBoardDimens.OVERLAY_ADD_TOOLBAR_BOTTOM_DP.dp),
        canUndo = state.canUndo,
        onUndo = { onIntent(ErdBoardIntent.OnUndo) },
        onShowJson = { onShowJsonDialogChange(true) },
        onShare = { onShowShareDialogChange(true) },
        onAddEntity = { onShowAddNodeDialogChange(true) },
    )

    if (showAddNodeDialog) {
        AddEntityDialog(
            onConfirm = { name ->
                onShowAddNodeDialogChange(false)
                val boardCenter = screenToBoardOffset(
                    point = Offset(screenWidth / 2f, screenHeight / 2f),
                    scale = state.scale,
                    panOffset = state.panOffset.toOffset(),
                    density = density,
                )
                onIntent(
                    ErdBoardIntent.OnAddNode(
                        name = name,
                        position = Position(boardCenter.x, boardCenter.y),
                    ),
                )
            },
            onDismiss = { onShowAddNodeDialogChange(false) },
        )
    }

    state.selectedNodeId?.let { selectedNodeId ->
        state.nodes[selectedNodeId]?.let { selectedNode ->
            NodeDetailDialog(
                nodeName = selectedNode.name,
                fields = selectedNode.fields,
                onAddField = { name, type ->
                    onIntent(ErdBoardIntent.OnAddField(selectedNodeId, name, type))
                },
                onRemoveField = { fieldId ->
                    onIntent(ErdBoardIntent.OnRemoveField(selectedNodeId, fieldId))
                },
                onDismiss = { onIntent(ErdBoardIntent.OnSelectNode(null)) },
            )
        }
    }

    if (showJsonDialog) {
        BoardJsonDialog(
            currentJson = checkNotNull(currentBoardJson),
            onImport = onImportBoardJson,
            onDismiss = { onShowJsonDialogChange(false) },
        )
    }

    if (showShareDialog) {
        BoardShareDialog(
            boardId = boardId,
            onDismiss = { onShowShareDialogChange(false) },
        )
    }
}
