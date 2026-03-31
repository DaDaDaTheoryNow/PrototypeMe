package com.dadadadev.prototype_me.domains.erd.design.impl.data.remote.network

import com.dadadadev.prototype_me.core.common.error.NetworkException
import com.dadadadev.prototype_me.core.common.result.AppResult
import com.dadadadev.prototype_me.core.network.client.decodeBodyOrThrow
import com.dadadadev.prototype_me.core.network.client.safeNetworkCall
import com.dadadadev.prototype_me.domains.erd.design.api.domain.model.ErdBoardCreationApproval
import com.dadadadev.prototype_me.domains.erd.design.api.domain.model.ErdBoardJoinSession
import com.dadadadev.prototype_me.domains.erd.design.api.domain.model.ErdInviteResolution
import com.dadadadev.prototype_me.domains.erd.design.api.domain.model.ErdRemoteBoardState
import com.dadadadev.prototype_me.domains.erd.design.impl.data.remote.dto.ActionRejectedResponseDto
import com.dadadadev.prototype_me.domains.erd.design.impl.data.remote.dto.CreateBoardRequestDto
import com.dadadadev.prototype_me.domains.erd.design.impl.data.remote.dto.CreateBoardResponseDto
import com.dadadadev.prototype_me.domains.erd.design.impl.data.remote.dto.ErdBoardExportDocumentDto
import com.dadadadev.prototype_me.domains.erd.design.impl.data.remote.dto.GetBoardResponseDto
import com.dadadadev.prototype_me.domains.erd.design.impl.data.remote.dto.ImportBoardResponseDto
import com.dadadadev.prototype_me.domains.erd.design.impl.data.remote.dto.JoinBoardRequestDto
import com.dadadadev.prototype_me.domains.erd.design.impl.data.remote.dto.JoinBoardResponseDto
import com.dadadadev.prototype_me.domains.erd.design.impl.data.remote.dto.LockGrantedResponseDto
import com.dadadadev.prototype_me.domains.erd.design.impl.data.remote.dto.LockRejectedResponseDto
import com.dadadadev.prototype_me.domains.erd.design.impl.data.remote.dto.LockRequestDto
import com.dadadadev.prototype_me.domains.erd.design.impl.data.remote.dto.ResolveInviteResponseDto
import com.dadadadev.prototype_me.domains.erd.design.impl.data.remote.dto.SubmitActionRequestDto
import com.dadadadev.prototype_me.domains.erd.design.impl.data.remote.dto.SubmitActionResponseDto
import com.dadadadev.prototype_me.domains.erd.design.impl.data.remote.mapper.toDomain
import com.dadadadev.prototype_me.domains.erd.design.impl.data.remote.mapper.toDto
import com.dadadadev.prototype_me.domains.erd.design.api.domain.model.ErdBoardAction
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode

internal class KtorErdBoardRemoteDataSource(
    private val client: HttpClient,
) : ErdBoardRemoteDataSource {

    override suspend fun createBoard(
        displayName: String,
        title: String?,
    ): AppResult<ErdBoardCreationApproval, NetworkException> = safeNetworkCall {
        client.post("/v1/boards") {
            setBody(CreateBoardRequestDto(displayName = displayName, title = title))
        }.decodeBodyOrThrow<CreateBoardResponseDto>().toDomain()
    }

    override suspend fun joinBoard(
        boardId: String,
        displayName: String,
        inviteToken: String?,
    ): AppResult<ErdBoardJoinSession, NetworkException> = safeNetworkCall {
        client.post("/v1/boards/$boardId/join") {
            setBody(JoinBoardRequestDto(displayName = displayName, inviteToken = inviteToken))
        }.decodeBodyOrThrow<JoinBoardResponseDto>().toDomain()
    }

    override suspend fun resolveInvite(inviteToken: String): AppResult<ErdInviteResolution, NetworkException> = safeNetworkCall {
        client.get("/v1/invites/$inviteToken/resolve")
            .decodeBodyOrThrow<ResolveInviteResponseDto>()
            .toDomain()
    }

    override suspend fun getBoard(boardId: String): AppResult<ErdRemoteBoardState, NetworkException> = safeNetworkCall {
        client.get("/v1/boards/$boardId")
            .decodeBodyOrThrow<GetBoardResponseDto>()
            .toDomain()
    }

    override suspend fun submitAction(
        boardId: String,
        action: ErdBoardAction,
    ): AppResult<ActionSubmitOutcome, NetworkException> = safeNetworkCall {
        val response = client.post("/v1/boards/$boardId/actions") {
            setBody(SubmitActionRequestDto(action = action.toDto()))
        }

        when (response.status) {
            HttpStatusCode.OK,
            HttpStatusCode.Accepted,
            HttpStatusCode.Created,
            -> {
                val body = response.decodeBodyOrThrow<SubmitActionResponseDto>()
                ActionSubmitOutcome.Accepted(
                    actionId = body.actionId,
                    serverVersion = body.serverVersion,
                )
            }

            HttpStatusCode.Conflict -> {
                val body = response.decodeBodyOrThrow<ActionRejectedResponseDto>()
                ActionSubmitOutcome.Rejected(
                    actionId = body.actionId,
                    reason = body.reason,
                    lockedBy = body.lockedBy,
                )
            }

            HttpStatusCode.Unauthorized -> throw NetworkException.Unauthorized
            else -> throw NetworkException.HttpError(
                statusCode = response.status.value,
                responseBody = response.bodyAsText(),
            )
        }
    }

    override suspend fun exportBoard(boardId: String): AppResult<ErdBoardExportDocumentDto, NetworkException> = safeNetworkCall {
        client.get("/v1/boards/$boardId/export")
            .decodeBodyOrThrow<ErdBoardExportDocumentDto>()
    }

    override suspend fun importBoard(
        boardId: String,
        document: ErdBoardExportDocumentDto,
    ): AppResult<Boolean, NetworkException> = safeNetworkCall {
        val response = client.post("/v1/boards/$boardId/import") {
            setBody(document)
        }

        when (response.status) {
            HttpStatusCode.OK,
            HttpStatusCode.Accepted,
            -> response.decodeBodyOrThrow<ImportBoardResponseDto>().accepted

            HttpStatusCode.Unauthorized -> throw NetworkException.Unauthorized
            else -> throw NetworkException.HttpError(
                statusCode = response.status.value,
                responseBody = response.bodyAsText(),
            )
        }
    }

    override suspend fun requestLock(
        boardId: String,
        nodeId: String,
    ): AppResult<LockRequestOutcome, NetworkException> = safeNetworkCall {
        val response = client.post("/v1/boards/$boardId/locks/request") {
            setBody(LockRequestDto(nodeId = nodeId))
        }

        when (response.status) {
            HttpStatusCode.OK -> {
                val body = response.decodeBodyOrThrow<LockGrantedResponseDto>()
                LockRequestOutcome.Granted(
                    nodeId = body.nodeId,
                    lockedBy = body.lockedBy,
                )
            }

            HttpStatusCode.Conflict -> {
                val body = response.decodeBodyOrThrow<LockRejectedResponseDto>()
                LockRequestOutcome.Rejected(
                    nodeId = body.nodeId,
                    lockedBy = body.lockedBy,
                )
            }

            HttpStatusCode.Unauthorized -> throw NetworkException.Unauthorized
            else -> throw NetworkException.HttpError(
                statusCode = response.status.value,
                responseBody = response.bodyAsText(),
            )
        }
    }

    override suspend fun releaseLock(
        boardId: String,
        nodeId: String,
    ): AppResult<Boolean, NetworkException> = safeNetworkCall {
        val response = client.post("/v1/boards/$boardId/locks/release") {
            setBody(LockRequestDto(nodeId = nodeId))
        }

        when (response.status) {
            HttpStatusCode.OK,
            HttpStatusCode.Accepted,
            HttpStatusCode.NoContent,
            -> true

            HttpStatusCode.Unauthorized -> throw NetworkException.Unauthorized
            else -> throw NetworkException.HttpError(
                statusCode = response.status.value,
                responseBody = response.bodyAsText(),
            )
        }
    }
}
