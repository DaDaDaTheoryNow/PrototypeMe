package com.dadadadev.prototype_me.domains.erd.design.impl.data.di

import com.dadadadev.prototype_me.domains.erd.design.api.data.codec.ErdBoardJsonCodec
import com.dadadadev.prototype_me.domains.erd.design.api.data.repository.ErdBoardRepository
import com.dadadadev.prototype_me.domains.erd.design.impl.data.json.ErdBoardJsonCodecImpl
import com.dadadadev.prototype_me.domains.erd.design.impl.data.mock.MockBoardRepositoryImpl
import com.dadadadev.prototype_me.domains.erd.design.impl.data.mock.MockBoardServer
import org.koin.dsl.module

val erdDesignDataModule = module {
    single { MockBoardServer() }
    single<ErdBoardJsonCodec> { ErdBoardJsonCodecImpl() }

    single<ErdBoardRepository> {
        MockBoardRepositoryImpl(
            fakeServer = get(),
            currentUserId = "user_1",
        )
    }
}
