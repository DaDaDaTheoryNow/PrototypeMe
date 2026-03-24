package com.dadadadev.prototype_me.di

import com.dadadadev.prototype_me.domains.erd.design.impl.data.di.erdDesignDataModule
import com.dadadadev.prototype_me.erd.board.di.erdBoardFeatureModule
import org.koin.dsl.module

val erdBoardModule = module {
    includes(
        erdDesignDataModule,
        erdBoardFeatureModule
    )
}
