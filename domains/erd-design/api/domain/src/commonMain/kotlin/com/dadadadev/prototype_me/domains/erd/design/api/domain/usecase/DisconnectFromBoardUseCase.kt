package com.dadadadev.prototype_me.domains.erd.design.api.domain.usecase

/**
 * Terminates the realtime board connection and releases all server-side
 * locks held by the current user.
 *
 * Should be called from [ViewModel.onCleared] or equivalent lifecycle hook.
 */
interface DisconnectFromBoardUseCase {
    suspend operator fun invoke()
}
