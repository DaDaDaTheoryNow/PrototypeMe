package com.dadadadev.prototype_me.erd.board.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalDensity
import com.dadadadev.prototype_me.erd.board.presentation.ErdBoardSession
import com.dadadadev.prototype_me.erd.board.presentation.ErdBoardViewModel
import com.dadadadev.prototype_me.erd.board.presentation.contract.ErdBoardIntent
import com.dadadadev.prototype_me.erd.board.presentation.contract.ErdBoardSideEffect
import com.dadadadev.prototype_me.erd.board.ui.canvas.ErdBoardCanvasContent
import com.dadadadev.prototype_me.erd.board.ui.canvas.rememberErdBoardRenderData
import com.dadadadev.prototype_me.erd.board.ui.theme.ErdBoardStrings
import com.dadadadev.prototype_me.feature.board.core.ui.input.boardKeyboardShortcuts
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@Composable
fun ErdBoardScreen(
    boardId: String,
    userId: String,
    onNavigateBack: (() -> Unit)? = null,
    viewModel: ErdBoardViewModel = koinViewModel(
        key = boardId,
        parameters = { parametersOf(ErdBoardSession(boardId = boardId, currentUserId = userId)) },
    ),
) {
    val state by viewModel.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val density = LocalDensity.current.density

    var marqueeStart by remember { mutableStateOf<Offset?>(null) }
    var marqueeCurrent by remember { mutableStateOf<Offset?>(null) }
    var marqueeSelectedNodeIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    var multiSelectMenuPos by remember { mutableStateOf<Offset?>(null) }
    var showAddNodeDialog by remember { mutableStateOf(false) }
    var showJsonDialog by remember { mutableStateOf(false) }
    var showShareDialog by remember { mutableStateOf(false) }
    var hasAppliedInitialViewportFit by remember { mutableStateOf(false) }
    val currentBoardJson = remember(showJsonDialog, state.nodes, state.edges) {
        if (!showJsonDialog) {
            null
        } else {
            viewModel.exportBoardJson(
                nodes = state.nodes,
                edges = state.edges,
            )
        }
    }

    val selectedNodeIds = remember(marqueeSelectedNodeIds, state.nodes) {
        marqueeSelectedNodeIds.filterTo(mutableSetOf()) { nodeId -> state.nodes.containsKey(nodeId) }
    }
    val renderData = rememberErdBoardRenderData(state = state, density = density)
    val handleIntent = rememberErdBoardIntentHandler(
        viewModel = viewModel,
        selectedNodeIds = selectedNodeIds,
        onClearSelection = { marqueeSelectedNodeIds = emptySet() },
        onHideMultiSelectMenu = { multiSelectMenuPos = null },
    )

    viewModel.collectSideEffect { effect ->
        when (effect) {
            is ErdBoardSideEffect.ShowLockError -> snackbarHostState.showSnackbar(ErdBoardStrings.lockedByMessage(effect.lockedBy))
            is ErdBoardSideEffect.ShowConnectionError -> snackbarHostState.showSnackbar(effect.message)
            ErdBoardSideEffect.ShowConnectionLost -> snackbarHostState.showSnackbar(ErdBoardStrings.CONNECTION_LOST)
            is ErdBoardSideEffect.SelectPastedNodes -> {
                marqueeSelectedNodeIds = effect.nodeIds
                multiSelectMenuPos = null
            }
        }
    }

    val handleEscape = {
        handleBoardEscape(
            showAddNodeDialog = showAddNodeDialog,
            selectedNodeId = state.selectedNodeId,
            hasSelection = marqueeSelectedNodeIds.isNotEmpty(),
            hasMultiSelectMenu = multiSelectMenuPos != null,
            dismissAddNodeDialog = { showAddNodeDialog = false },
            dismissNodeDialog = { viewModel.onIntent(ErdBoardIntent.OnSelectNode(null)) },
            clearSelectionMenu = {
                marqueeSelectedNodeIds = emptySet()
                multiSelectMenuPos = null
            },
            onBoardEscape = { viewModel.onIntent(ErdBoardIntent.OnEscape) },
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .boardKeyboardShortcuts(
                isActive = !showAddNodeDialog && !showJsonDialog && !showShareDialog && state.selectedNodeId == null,
                focusRestoreKey = listOf(showAddNodeDialog, showJsonDialog, showShareDialog, state.selectedNodeId),
                onEscape = handleEscape,
                onUndo = { viewModel.onIntent(ErdBoardIntent.OnUndo) },
                onCopy = {
                    val nodeIdsToCopy = resolveCopySelection(selectedNodeIds, state)
                    if (nodeIdsToCopy.isNotEmpty()) {
                        viewModel.onIntent(ErdBoardIntent.OnCopy(nodeIdsToCopy))
                    }
                },
                onPaste = { viewModel.onIntent(ErdBoardIntent.OnPaste) },
                onDelete = {
                    val nodeIdsToDelete = resolveDeleteSelection(selectedNodeIds, state)
                    if (nodeIdsToDelete.isNotEmpty()) {
                        marqueeSelectedNodeIds = emptySet()
                        multiSelectMenuPos = null
                        if (nodeIdsToDelete.size == 1) {
                            viewModel.onIntent(ErdBoardIntent.OnDeleteNode(nodeIdsToDelete.first()))
                        } else {
                            viewModel.onIntent(ErdBoardIntent.OnDeleteNodes(nodeIdsToDelete))
                        }
                    }
                },
            ),
    ) {
        ErdBoardCanvasContent(
            state = state,
            density = density,
            renderData = renderData,
            selectedNodeIds = selectedNodeIds,
            showAddNodeDialog = showAddNodeDialog,
            onShowAddNodeDialogChange = { showAddNodeDialog = it },
            showJsonDialog = showJsonDialog,
            onShowJsonDialogChange = { showJsonDialog = it },
            showShareDialog = showShareDialog,
            onShowShareDialogChange = { showShareDialog = it },
            boardId = boardId,
            currentBoardJson = currentBoardJson,
            onImportBoardJson = { jsonText ->
                val importedBoard = viewModel.importBoardJson(jsonText).getOrNull()
                if (importedBoard != null) {
                    viewModel.onIntent(ErdBoardIntent.OnImportBoard(snapshot = importedBoard))
                    showJsonDialog = false
                }
            },
            marqueeStart = marqueeStart,
            marqueeCurrent = marqueeCurrent,
            onMarqueeStartChange = { marqueeStart = it },
            onMarqueeCurrentChange = { marqueeCurrent = it },
            onMarqueeSelectionChange = { marqueeSelectedNodeIds = it },
            multiSelectMenuPos = multiSelectMenuPos,
            onMultiSelectMenuPosChange = { multiSelectMenuPos = it },
            hasAppliedInitialViewportFit = hasAppliedInitialViewportFit,
            onInitialViewportFitApplied = { hasAppliedInitialViewportFit = true },
            onIntent = handleIntent,
        )

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter),
        ) { snackbarData ->
            Snackbar(snackbarData = snackbarData)
        }
    }
}
