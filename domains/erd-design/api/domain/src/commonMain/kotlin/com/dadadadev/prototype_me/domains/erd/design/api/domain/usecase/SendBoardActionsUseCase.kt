package com.dadadadev.prototype_me.domains.erd.design.api.domain.usecase

import com.dadadadev.prototype_me.domains.erd.design.api.domain.model.ErdBoardAction

/**
 * Sends a batch of raw [ErdBoardAction]s directly to the board server.
 *
 * This is an escape-hatch use case intended for operations that need to compose
 * multiple low-level actions in a single call — primarily undo operations and
 * paste/import flows — where the higher-level semantic use cases don't map cleanly.
 *
 * Prefer the purpose-built use cases ([AddNodeUseCase], [DeleteEdgeUseCase], etc.)
 * for all regular intent handling.
 */
interface SendBoardActionsUseCase {
    suspend operator fun invoke(actions: List<ErdBoardAction>)
}
