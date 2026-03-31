package com.dadadadev.prototype_me.domains.erd.design.impl.data.remote.network

import com.dadadadev.prototype_me.core.common.error.NetworkException
import com.dadadadev.prototype_me.core.common.result.AppResult
import com.dadadadev.prototype_me.domains.erd.design.api.domain.model.ErdBoardCreationApproval
import com.dadadadev.prototype_me.domains.erd.design.api.domain.model.ErdBoardJoinSession
import com.dadadadev.prototype_me.domains.erd.design.api.domain.model.ErdInviteResolution
import com.dadadadev.prototype_me.domains.erd.design.api.domain.model.ErdRemoteBoardState
import com.dadadadev.prototype_me.domains.erd.design.impl.data.remote.dto.ErdBoardExportDocumentDto
import com.dadadadev.prototype_me.domains.erd.design.api.domain.model.ErdBoardAction

internal interface ErdBoardRemoteDataSource {
    suspend fun createBoard(
        displayName: String,
        title: String? = null,
    ): AppResult<ErdBoardCreationApproval, NetworkException>

    suspend fun joinBoard(
        boardId: String,
        displayName: String,
        inviteToken: String? = null,
    ): AppResult<ErdBoardJoinSession, NetworkException>

    suspend fun resolveInvite(
        inviteToken: String,
    ): AppResult<ErdInviteResolution, NetworkException>

    suspend fun getBoard(
        boardId: String,
    ): AppResult<ErdRemoteBoardState, NetworkException>

    suspend fun submitAction(
        boardId: String,
        action: ErdBoardAction,
    ): AppResult<ActionSubmitOutcome, NetworkException>

    suspend fun exportBoard(
        boardId: String,
    ): AppResult<ErdBoardExportDocumentDto, NetworkException>

    suspend fun importBoard(
        boardId: String,
        document: ErdBoardExportDocumentDto,
    ): AppResult<Boolean, NetworkException>

    suspend fun requestLock(
        boardId: String,
        nodeId: String,
    ): AppResult<LockRequestOutcome, NetworkException>

    suspend fun releaseLock(
        boardId: String,
        nodeId: String,
    ): AppResult<Boolean, NetworkException>
}
