package com.dadadadev.prototype_me.domains.erd.design.impl.data.remote.realtime

import com.dadadadev.prototype_me.core.common.error.NetworkException
import com.dadadadev.prototype_me.core.common.result.AppResult
import com.dadadadev.prototype_me.domains.erd.design.api.domain.model.Position
import com.dadadadev.prototype_me.domains.erd.design.impl.data.remote.network.RealtimeTransportEvent
import com.dadadadev.prototype_me.domains.erd.design.api.domain.model.ErdBoardAction
import kotlinx.coroutines.flow.SharedFlow

internal interface ErdRealtimeBoardClient {
    val events: SharedFlow<RealtimeTransportEvent>

    suspend fun connect(
        boardId: String,
        sessionToken: String,
        lastSeenVersion: Long? = null,
    ): AppResult<Unit, NetworkException>

    suspend fun disconnect()

    suspend fun sendAction(
        action: ErdBoardAction,
        requestId: String? = null,
    ): AppResult<Unit, NetworkException>

    suspend fun requestLock(
        nodeId: String,
        requestId: String? = null,
    ): AppResult<Unit, NetworkException>

    suspend fun releaseLock(
        nodeId: String,
        requestId: String? = null,
    ): AppResult<Unit, NetworkException>

    suspend fun ping(
        nonce: String,
        requestId: String? = null,
    ): AppResult<Unit, NetworkException>

    suspend fun sendNodeDragUpdate(
        nodeId: String,
        position: Position,
        requestId: String? = null,
    ): AppResult<Unit, NetworkException>
}
