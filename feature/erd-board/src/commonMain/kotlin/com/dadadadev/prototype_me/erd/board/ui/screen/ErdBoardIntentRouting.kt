package com.dadadadev.prototype_me.erd.board.ui.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import com.dadadadev.prototype_me.erd.board.presentation.contract.ErdBoardIntent
import com.dadadadev.prototype_me.erd.board.presentation.contract.ErdBoardState
import com.dadadadev.prototype_me.erd.board.presentation.ErdBoardViewModel

@Composable
internal fun rememberErdBoardIntentHandler(
    viewModel: ErdBoardViewModel,
    selectedNodeIds: Set<String>,
    onClearSelection: () -> Unit,
    onHideMultiSelectMenu: () -> Unit,
): (ErdBoardIntent) -> Unit {
    val latestSelectedNodeIds by rememberUpdatedState(selectedNodeIds)
    val clearSelection by rememberUpdatedState(onClearSelection)
    val hideMultiSelectMenu by rememberUpdatedState(onHideMultiSelectMenu)
    var isDraggingSelection by remember { mutableStateOf(false) }

    return remember(viewModel) {
        { intent ->
            when (intent) {
                is ErdBoardIntent.OnDragStart if latestSelectedNodeIds.size > 1 && intent.nodeId in latestSelectedNodeIds -> {
                    isDraggingSelection = true
                    latestSelectedNodeIds.forEach { nodeId ->
                        viewModel.onIntent(ErdBoardIntent.OnDragStart(nodeId))
                    }
                }

                is ErdBoardIntent.OnDragStart if intent.nodeId !in latestSelectedNodeIds -> {
                    clearSelection()
                    hideMultiSelectMenu()
                    viewModel.onIntent(intent)
                }

                is ErdBoardIntent.OnDragNode if isDraggingSelection -> {
                    latestSelectedNodeIds.forEach { nodeId ->
                        viewModel.onIntent(ErdBoardIntent.OnDragNode(nodeId, intent.delta))
                    }
                }

                is ErdBoardIntent.OnDragEnd if isDraggingSelection -> {
                    isDraggingSelection = false
                    viewModel.onIntent(ErdBoardIntent.OnMultiDragEnd(latestSelectedNodeIds))
                }

                else -> viewModel.onIntent(intent)
            }
        }
    }
}

internal fun handleBoardEscape(
    showAddNodeDialog: Boolean,
    selectedNodeId: String?,
    hasSelection: Boolean,
    hasMultiSelectMenu: Boolean,
    dismissAddNodeDialog: () -> Unit,
    dismissNodeDialog: () -> Unit,
    clearSelectionMenu: () -> Unit,
    onBoardEscape: () -> Unit,
) {
    when {
        showAddNodeDialog -> dismissAddNodeDialog()
        selectedNodeId != null -> dismissNodeDialog()
        hasSelection || hasMultiSelectMenu -> clearSelectionMenu()
        else -> onBoardEscape()
    }
}

internal fun resolveCopySelection(
    selectedNodeIds: Set<String>,
    state: ErdBoardState,
): Set<String> = when {
    selectedNodeIds.isNotEmpty() -> selectedNodeIds
    state.nodeMenuNodeId != null -> setOf(state.nodeMenuNodeId)
    else -> emptySet()
}

internal fun resolveDeleteSelection(
    selectedNodeIds: Set<String>,
    state: ErdBoardState,
): Set<String> = when {
    selectedNodeIds.isNotEmpty() -> selectedNodeIds
    state.nodeMenuNodeId != null -> setOf(state.nodeMenuNodeId)
    else -> emptySet()
}

