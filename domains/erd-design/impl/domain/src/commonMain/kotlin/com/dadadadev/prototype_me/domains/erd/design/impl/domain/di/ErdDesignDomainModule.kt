package com.dadadadev.prototype_me.domains.erd.design.impl.domain.di

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
import com.dadadadev.prototype_me.domains.erd.design.impl.domain.usecase.AddEdgeUseCaseImpl
import com.dadadadev.prototype_me.domains.erd.design.impl.domain.usecase.AddFieldUseCaseImpl
import com.dadadadev.prototype_me.domains.erd.design.impl.domain.usecase.AddNodeUseCaseImpl
import com.dadadadev.prototype_me.domains.erd.design.impl.domain.usecase.ConnectToBoardUseCaseImpl
import com.dadadadev.prototype_me.domains.erd.design.impl.domain.usecase.DeleteEdgeUseCaseImpl
import com.dadadadev.prototype_me.domains.erd.design.impl.domain.usecase.DeleteNodeUseCaseImpl
import com.dadadadev.prototype_me.domains.erd.design.impl.domain.usecase.DeleteNodesUseCaseImpl
import com.dadadadev.prototype_me.domains.erd.design.impl.domain.usecase.DisconnectFromBoardUseCaseImpl
import com.dadadadev.prototype_me.domains.erd.design.impl.domain.usecase.ExportBoardJsonUseCaseImpl
import com.dadadadev.prototype_me.domains.erd.design.impl.domain.usecase.ImportBoardJsonUseCaseImpl
import com.dadadadev.prototype_me.domains.erd.design.impl.domain.usecase.MoveNodeUseCaseImpl
import com.dadadadev.prototype_me.domains.erd.design.impl.domain.usecase.MoveNodesUseCaseImpl
import com.dadadadev.prototype_me.domains.erd.design.impl.domain.usecase.ObserveBoardStateUseCaseImpl
import com.dadadadev.prototype_me.domains.erd.design.impl.domain.usecase.ObserveSyncEffectsUseCaseImpl
import com.dadadadev.prototype_me.domains.erd.design.impl.domain.usecase.ReleaseNodeLockUseCaseImpl
import com.dadadadev.prototype_me.domains.erd.design.impl.domain.usecase.RemoveFieldUseCaseImpl
import com.dadadadev.prototype_me.domains.erd.design.impl.domain.usecase.RenameFieldUseCaseImpl
import com.dadadadev.prototype_me.domains.erd.design.impl.domain.usecase.RequestNodeLockUseCaseImpl
import com.dadadadev.prototype_me.domains.erd.design.impl.domain.usecase.SendBoardActionsUseCaseImpl
import com.dadadadev.prototype_me.domains.erd.design.impl.domain.usecase.SendNodeDragUpdateUseCaseImpl
import org.koin.dsl.module

val erdDesignDomainModule = module {
    // ── Connection ────────────────────────────────────────────────────────────
    factory<ConnectToBoardUseCase> { ConnectToBoardUseCaseImpl(get()) }
    factory<DisconnectFromBoardUseCase> { DisconnectFromBoardUseCaseImpl(get()) }

    // ── Observation ───────────────────────────────────────────────────────────
    factory<ObserveBoardStateUseCase> { ObserveBoardStateUseCaseImpl(get()) }
    factory<ObserveSyncEffectsUseCase> { ObserveSyncEffectsUseCaseImpl(get()) }

    // ── Node operations ───────────────────────────────────────────────────────
    factory<AddNodeUseCase> { AddNodeUseCaseImpl(get()) }
    factory<DeleteNodeUseCase> { DeleteNodeUseCaseImpl(get()) }
    factory<DeleteNodesUseCase> { DeleteNodesUseCaseImpl(get()) }
    factory<MoveNodeUseCase> { MoveNodeUseCaseImpl(get()) }
    factory<MoveNodesUseCase> { MoveNodesUseCaseImpl(get()) }

    // ── Edge operations ───────────────────────────────────────────────────────
    factory<AddEdgeUseCase> { AddEdgeUseCaseImpl(get()) }
    factory<DeleteEdgeUseCase> { DeleteEdgeUseCaseImpl(get()) }

    // ── Field operations ──────────────────────────────────────────────────────
    factory<AddFieldUseCase> { AddFieldUseCaseImpl(get()) }
    factory<RemoveFieldUseCase> { RemoveFieldUseCaseImpl(get()) }
    factory<RenameFieldUseCase> { RenameFieldUseCaseImpl(get()) }

    // ── Lock operations ───────────────────────────────────────────────────────
    factory<RequestNodeLockUseCase> { RequestNodeLockUseCaseImpl(get()) }
    factory<ReleaseNodeLockUseCase> { ReleaseNodeLockUseCaseImpl(get()) }

    // ── JSON codec operations ─────────────────────────────────────────────────
    factory<ExportBoardJsonUseCase> { ExportBoardJsonUseCaseImpl(get()) }
    factory<ImportBoardJsonUseCase> { ImportBoardJsonUseCaseImpl(get()) }

    // ── Batch / escape-hatch ──────────────────────────────────────────────────
    factory<SendBoardActionsUseCase> { SendBoardActionsUseCaseImpl(get()) }
    factory<SendNodeDragUpdateUseCase> { SendNodeDragUpdateUseCaseImpl(get()) }
}
