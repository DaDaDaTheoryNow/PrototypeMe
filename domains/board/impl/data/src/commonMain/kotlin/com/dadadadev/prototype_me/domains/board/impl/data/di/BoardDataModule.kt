package com.dadadadev.prototype_me.domains.board.impl.data.di

import com.dadadadev.prototype_me.domain.repository.BoardRepository
import com.dadadadev.prototype_me.domains.board.impl.data.mock.MockBoardRepositoryImpl
import com.dadadadev.prototype_me.domains.board.impl.data.mock.MockBoardServer
import org.koin.dsl.module

val boardDataModule = module {
    // Singleton server holds the authoritative in-memory board state.
    single { MockBoardServer() }

    // Repository implementation backed by the mock server.
    single<BoardRepository> {
        MockBoardRepositoryImpl(
            fakeServer = get(),
            currentUserId = "user_1"
        )
    }
}
