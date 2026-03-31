package com.dadadadev.prototype_me.domains.erd.design.impl.data.di

import com.dadadadev.prototype_me.domains.erd.design.api.data.codec.ErdBoardJsonCodec
import com.dadadadev.prototype_me.domains.erd.design.impl.data.json.ErdBoardJsonCodecImpl
import com.dadadadev.prototype_me.domains.erd.design.impl.data.remote.di.erdBoardDataModule
import org.koin.dsl.module

val erdDesignDataModule = module {
    includes(erdBoardDataModule)
    single<ErdBoardJsonCodec> { ErdBoardJsonCodecImpl() }
}
