package com.dadadadev.prototype_me.erd.board.presentation

import com.dadadadev.prototype_me.core.mvi.BaseViewModel
import com.dadadadev.prototype_me.domains.board.core.api.domain.model.BoardSnapshot
import com.dadadadev.prototype_me.domains.erd.design.api.domain.model.ErdEntityNode
import com.dadadadev.prototype_me.domains.erd.design.api.domain.model.ErdRelationEdge
import com.dadadadev.prototype_me.domains.erd.design.api.domain.model.Position
import com.dadadadev.prototype_me.erd.board.presentation.contract.ErdBoardIntent
import com.dadadadev.prototype_me.erd.board.presentation.contract.ErdBoardSideEffect
import com.dadadadev.prototype_me.erd.board.presentation.contract.ErdBoardState
import com.dadadadev.prototype_me.erd.board.presentation.viewmodel.ErdBoardRuntimeState
import com.dadadadev.prototype_me.erd.board.presentation.viewmodel.connectBoard
import com.dadadadev.prototype_me.erd.board.presentation.viewmodel.handleCanvasIntent
import com.dadadadev.prototype_me.erd.board.presentation.viewmodel.handleEdgeIntent
import com.dadadadev.prototype_me.erd.board.presentation.viewmodel.handleFieldIntent
import com.dadadadev.prototype_me.erd.board.presentation.viewmodel.handleGlobalIntent
import com.dadadadev.prototype_me.erd.board.presentation.viewmodel.handleNodeIntent
import com.dadadadev.prototype_me.erd.board.presentation.viewmodel.observeBoardSideEffects
import com.dadadadev.prototype_me.erd.board.presentation.viewmodel.observeBoardState

class ErdBoardViewModel(
    internal val useCases: ErdBoardUseCases,
    internal val boardSession: ErdBoardSession,
) : BaseViewModel<ErdBoardState, ErdBoardSideEffect>(ErdBoardState()) {

    internal val dragOrigins = mutableMapOf<String, Position>()
    internal var runtimeState = ErdBoardRuntimeState()

    init {
        connectBoard()
        observeBoardState()
        observeBoardSideEffects()
    }

    override fun onCleared() {
        super.onCleared()
        intent { useCases.disconnectFromBoard() }
    }

    fun onIntent(boardIntent: ErdBoardIntent) {
        when (boardIntent) {
            is ErdBoardIntent.OnPanZoom,
            is ErdBoardIntent.OnPan,
            is ErdBoardIntent.OnSetViewTransform,
            is ErdBoardIntent.OnDragStart,
            is ErdBoardIntent.OnDragNode,
            is ErdBoardIntent.OnDragEnd,
            is ErdBoardIntent.OnMultiDragEnd,
            -> handleCanvasIntent(boardIntent)

            is ErdBoardIntent.OnAddNode,
            is ErdBoardIntent.OnDeleteNode,
            is ErdBoardIntent.OnDeleteNodes,
            -> handleNodeIntent(boardIntent)

            is ErdBoardIntent.OnNodeFieldTap,
            ErdBoardIntent.OnCancelConnect,
            is ErdBoardIntent.OnEdgeDragStart,
            is ErdBoardIntent.OnEdgeDragMove,
            is ErdBoardIntent.OnEdgeDragEnd,
            is ErdBoardIntent.OnSelectEdge,
            is ErdBoardIntent.OnDeleteEdge,
            is ErdBoardIntent.OnNodeMenu,
            -> handleEdgeIntent(boardIntent)

            is ErdBoardIntent.OnSelectNode,
            is ErdBoardIntent.OnAddField,
            is ErdBoardIntent.OnRemoveField,
            is ErdBoardIntent.OnRenameField,
            -> handleFieldIntent(boardIntent)

            ErdBoardIntent.OnEscape,
            ErdBoardIntent.OnUndo,
            is ErdBoardIntent.OnCopy,
            ErdBoardIntent.OnPaste,
            is ErdBoardIntent.OnImportBoard,
            -> handleGlobalIntent(boardIntent)
        }
    }

    fun exportBoardJson(
        nodes: Map<String, ErdEntityNode>,
        edges: Map<String, ErdRelationEdge>,
    ): String = useCases.exportBoardJson(
        BoardSnapshot(entities = nodes, edges = edges),
    )

    fun importBoardJson(rawJson: String): Result<BoardSnapshot<ErdEntityNode, ErdRelationEdge>> =
        useCases.importBoardJson(rawJson)
}
