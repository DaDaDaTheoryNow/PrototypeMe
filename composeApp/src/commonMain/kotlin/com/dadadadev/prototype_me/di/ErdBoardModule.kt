package com.dadadadev.prototype_me.di

import com.dadadadev.prototype_me.domains.auth.impl.di.authModule
import com.dadadadev.prototype_me.domains.erd.design.impl.data.di.erdDesignDataModule
import com.dadadadev.prototype_me.domains.erd.design.impl.domain.di.erdDesignDomainModule
import com.dadadadev.prototype_me.erd.board.di.erdBoardFeatureModule
import com.dadadadev.prototype_me.feature.home.di.homeFeatureModule
import org.koin.dsl.module

val erdBoardModule = module {
    includes(
        authModule,
        erdDesignDomainModule,
        erdDesignDataModule,
        erdBoardFeatureModule,
        homeFeatureModule,
    )
}
