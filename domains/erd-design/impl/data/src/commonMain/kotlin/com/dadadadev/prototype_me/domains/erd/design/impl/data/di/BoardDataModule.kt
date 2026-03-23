package com.dadadadev.prototype_me.domains.erd.design.impl.data.di

import com.dadadadev.prototype_me.domains.erd.design.api.data.repository.BoardRepository
import com.dadadadev.prototype_me.domains.erd.design.impl.data.mock.MockBoardRepositoryImpl
import com.dadadadev.prototype_me.domains.erd.design.impl.data.mock.MockBoardServer
import org.koin.dsl.module

val erdDesignDataModule = module {
    // Singleton server holds the authoritative in-memory board state.
    single { MockBoardServer() }

    // Repository implementation backed by the mock server.
    single<BoardRepository> {
        MockBoardRepositoryImpl(
            fakeServer = get(),
            currentUserId = "user_1",
        )
    }
}
