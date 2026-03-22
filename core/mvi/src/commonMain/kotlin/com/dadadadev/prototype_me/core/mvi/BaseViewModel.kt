package com.dadadadev.prototype_me.core.mvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.container

/**
 * KMP-compatible base ViewModel that integrates Orbit MVI with the
 * JetBrains Multiplatform lifecycle ViewModel.
 *
 * All subclasses automatically get:
 * - [container] scoped to [viewModelScope] (cancelled on ViewModel destruction)
 * - Full [ContainerHost] API: [intent], [reduce], [postSideEffect]
 */
abstract class BaseViewModel<STATE : Any, SIDE_EFFECT : Any>(
    initialState: STATE
) : ViewModel(), ContainerHost<STATE, SIDE_EFFECT> {

    override val container: Container<STATE, SIDE_EFFECT> =
        viewModelScope.container(initialState)
}
