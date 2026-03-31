package com.dadadadev.prototype_me.domains.erd.design.impl.data.remote.di

import com.dadadadev.prototype_me.domains.erd.design.api.data.repository.ErdBoardRemoteRepository
import com.dadadadev.prototype_me.domains.erd.design.impl.data.remote.network.ErdBoardHttpClientProvider
import com.dadadadev.prototype_me.domains.erd.design.impl.data.remote.network.ErdBoardRemoteDataSource
import com.dadadadev.prototype_me.domains.erd.design.impl.data.remote.network.KtorErdBoardRemoteDataSource
import com.dadadadev.prototype_me.domains.erd.design.impl.data.remote.realtime.ErdRealtimeBoardClient
import com.dadadadev.prototype_me.domains.erd.design.impl.data.remote.realtime.KtorErdRealtimeBoardClient
import com.dadadadev.prototype_me.domains.erd.design.impl.data.remote.repository.RemoteErdBoardRepositoryImpl
import com.dadadadev.prototype_me.domains.erd.design.api.data.repository.ErdBoardRepository
import org.koin.core.qualifier.named
import org.koin.dsl.module

private const val ERD_BOARD_HTTP_CLIENT_QUALIFIER = "erd_board_http_client"

val erdBoardDataModule = module {
    single(named(ERD_BOARD_HTTP_CLIENT_QUALIFIER)) { ErdBoardHttpClientProvider(accessTokenProvider = get()).createClient() }
    single<ErdBoardRemoteDataSource> { KtorErdBoardRemoteDataSource(client = get(named(ERD_BOARD_HTTP_CLIENT_QUALIFIER))) }
    single<ErdRealtimeBoardClient> { KtorErdRealtimeBoardClient(client = get(named(ERD_BOARD_HTTP_CLIENT_QUALIFIER))) }

    single { RemoteErdBoardRepositoryImpl(dataSource = get(), realtimeClient = get()) }
    single<ErdBoardRepository> { get<RemoteErdBoardRepositoryImpl>() }
    single<ErdBoardRemoteRepository> { get<RemoteErdBoardRepositoryImpl>() }
}
