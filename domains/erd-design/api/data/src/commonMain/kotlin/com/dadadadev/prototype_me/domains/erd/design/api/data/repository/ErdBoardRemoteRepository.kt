package com.dadadadev.prototype_me.domains.erd.design.api.data.repository

import com.dadadadev.prototype_me.core.common.error.NetworkException
import com.dadadadev.prototype_me.core.common.result.AppResult
import com.dadadadev.prototype_me.domains.erd.design.api.domain.model.ErdBoardCreationApproval
import com.dadadadev.prototype_me.domains.erd.design.api.domain.model.ErdBoardJoinSession
import com.dadadadev.prototype_me.domains.erd.design.api.domain.model.ErdInviteResolution

interface ErdBoardRemoteRepository : ErdBoardRepository {
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
}
