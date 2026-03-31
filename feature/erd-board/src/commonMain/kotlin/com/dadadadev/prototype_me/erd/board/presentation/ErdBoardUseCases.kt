package com.dadadadev.prototype_me.erd.board.presentation

import com.dadadadev.prototype_me.domains.erd.design.api.domain.usecase.AddEdgeUseCase
import com.dadadadev.prototype_me.domains.erd.design.api.domain.usecase.AddFieldUseCase
import com.dadadadev.prototype_me.domains.erd.design.api.domain.usecase.AddNodeUseCase
import com.dadadadev.prototype_me.domains.erd.design.api.domain.usecase.ConnectToBoardUseCase
import com.dadadadev.prototype_me.domains.erd.design.api.domain.usecase.DeleteEdgeUseCase
import com.dadadadev.prototype_me.domains.erd.design.api.domain.usecase.DeleteNodeUseCase
import com.dadadadev.prototype_me.domains.erd.design.api.domain.usecase.DeleteNodesUseCase
import com.dadadadev.prototype_me.domains.erd.design.api.domain.usecase.DisconnectFromBoardUseCase
import com.dadadadev.prototype_me.domains.erd.design.api.domain.usecase.ExportBoardJsonUseCase
import com.dadadadev.prototype_me.domains.erd.design.api.domain.usecase.ImportBoardJsonUseCase
import com.dadadadev.prototype_me.domains.erd.design.api.domain.usecase.MoveNodeUseCase
import com.dadadadev.prototype_me.domains.erd.design.api.domain.usecase.MoveNodesUseCase
import com.dadadadev.prototype_me.domains.erd.design.api.domain.usecase.ObserveBoardStateUseCase
import com.dadadadev.prototype_me.domains.erd.design.api.domain.usecase.ObserveSyncEffectsUseCase
import com.dadadadev.prototype_me.domains.erd.design.api.domain.usecase.ReleaseNodeLockUseCase
import com.dadadadev.prototype_me.domains.erd.design.api.domain.usecase.RemoveFieldUseCase
import com.dadadadev.prototype_me.domains.erd.design.api.domain.usecase.RenameFieldUseCase
import com.dadadadev.prototype_me.domains.erd.design.api.domain.usecase.RequestNodeLockUseCase
import com.dadadadev.prototype_me.domains.erd.design.api.domain.usecase.SendBoardActionsUseCase
import com.dadadadev.prototype_me.domains.erd.design.api.domain.usecase.SendNodeDragUpdateUseCase

/**
 * Parameter-object that groups all ERD board use cases, keeping the
 * [ErdBoardViewModel] constructor manageable as the feature grows.
 */
data class ErdBoardUseCases(
    // ── Connection ────────────────────────────────────────────────────────────
    val connectToBoard: ConnectToBoardUseCase,
    val disconnectFromBoard: DisconnectFromBoardUseCase,
    // ── Observation ───────────────────────────────────────────────────────────
    val observeBoardState: ObserveBoardStateUseCase,
    val observeSyncEffects: ObserveSyncEffectsUseCase,
    // ── Node operations ───────────────────────────────────────────────────────
    val addNode: AddNodeUseCase,
    val deleteNode: DeleteNodeUseCase,
    val deleteNodes: DeleteNodesUseCase,
    val moveNode: MoveNodeUseCase,
    val moveNodes: MoveNodesUseCase,
    // ── Edge operations ───────────────────────────────────────────────────────
    val addEdge: AddEdgeUseCase,
    val deleteEdge: DeleteEdgeUseCase,
    // ── Field operations ──────────────────────────────────────────────────────
    val addField: AddFieldUseCase,
    val removeField: RemoveFieldUseCase,
    val renameField: RenameFieldUseCase,
    // ── Lock operations ───────────────────────────────────────────────────────
    val requestNodeLock: RequestNodeLockUseCase,
    val releaseNodeLock: ReleaseNodeLockUseCase,
    // ── JSON codec operations ─────────────────────────────────────────────────
    val exportBoardJson: ExportBoardJsonUseCase,
    val importBoardJson: ImportBoardJsonUseCase,
    // ── Batch / escape-hatch ──────────────────────────────────────────────────
    val sendBoardActions: SendBoardActionsUseCase,
    val sendNodeDragUpdate: SendNodeDragUpdateUseCase,
)
