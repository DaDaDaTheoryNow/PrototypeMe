package com.dadadadev.prototype_me.di

import com.dadadadev.prototype_me.erd.board.presentation.BoardViewModel
import com.dadadadev.prototype_me.domains.erd.design.impl.data.di.erdDesignDataModule
import org.koin.dsl.module

val boardModule = module {
    includes(erdDesignDataModule)

    // ViewModel created fresh per screen instance.
    factory { BoardViewModel(repository = get()) }
}
