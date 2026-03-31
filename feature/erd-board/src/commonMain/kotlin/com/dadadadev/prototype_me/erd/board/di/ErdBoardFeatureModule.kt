package com.dadadadev.prototype_me.erd.board.di

import com.dadadadev.prototype_me.erd.board.presentation.ErdBoardSession
import com.dadadadev.prototype_me.erd.board.presentation.ErdBoardUseCases
import com.dadadadev.prototype_me.erd.board.presentation.ErdBoardViewModel
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
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val erdBoardFeatureModule = module {
    // ErdBoardUseCases assembled from the domain use-case bindings.
    factory {
        ErdBoardUseCases(
            connectToBoard = get<ConnectToBoardUseCase>(),
            disconnectFromBoard = get<DisconnectFromBoardUseCase>(),
            observeBoardState = get<ObserveBoardStateUseCase>(),
            observeSyncEffects = get<ObserveSyncEffectsUseCase>(),
            addNode = get<AddNodeUseCase>(),
            deleteNode = get<DeleteNodeUseCase>(),
            deleteNodes = get<DeleteNodesUseCase>(),
            moveNode = get<MoveNodeUseCase>(),
            moveNodes = get<MoveNodesUseCase>(),
            addEdge = get<AddEdgeUseCase>(),
            deleteEdge = get<DeleteEdgeUseCase>(),
            addField = get<AddFieldUseCase>(),
            removeField = get<RemoveFieldUseCase>(),
            renameField = get<RenameFieldUseCase>(),
            requestNodeLock = get<RequestNodeLockUseCase>(),
            releaseNodeLock = get<ReleaseNodeLockUseCase>(),
            exportBoardJson = get<ExportBoardJsonUseCase>(),
            importBoardJson = get<ImportBoardJsonUseCase>(),
            sendBoardActions = get<SendBoardActionsUseCase>(),
            sendNodeDragUpdate = get<SendNodeDragUpdateUseCase>(),
        )
    }

    // ViewModel receives ErdBoardSession as a Koin parameter so the
    // caller (ErdBoardScreen) can supply the correct boardId/userId at
    // navigation time without hardcoding them in the DI graph.
    viewModel { params ->
        ErdBoardViewModel(
            useCases = get(),
            boardSession = params.get<ErdBoardSession>(),
        )
    }
}
