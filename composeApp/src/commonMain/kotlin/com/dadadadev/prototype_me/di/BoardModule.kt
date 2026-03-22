package com.dadadadev.prototype_me.di

import com.dadadadev.prototype_me.board.presentation.BoardViewModel
import com.dadadadev.prototype_me.domains.board.impl.data.di.boardDataModule
import org.koin.dsl.module

val boardModule = module {
    includes(boardDataModule)

    // ViewModel created fresh per screen instance.
    factory { BoardViewModel(repository = get()) }
}
