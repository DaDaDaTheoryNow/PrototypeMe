package com.dadadadev.prototype_me.erd.board.di

import com.dadadadev.prototype_me.erd.board.presentation.ErdBoardViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val erdBoardFeatureModule = module {
    viewModelOf(::ErdBoardViewModel)
}
