package com.dadadadev.prototype_me.domains.erd.design.api.domain.model

enum class ErdBoardParticipantRole {
    OWNER,
    EDITOR,
    VIEWER,
}

data class ErdBoardCreationApproval(
    val approved: Boolean,
    val boardId: String,
    val inviteToken: String,
    val webUrl: String,
    val deepLink: String,
    val createdAt: String,
)

data class ErdBoardJoinSession(
    val boardId: String,
    val userId: String,
    val displayName: String,
    val role: ErdBoardParticipantRole,
    val sessionToken: String,
    val expiresAt: String,
)

data class ErdInviteResolution(
    val boardId: String,
    val webUrl: String,
    val deepLink: String,
    val canJoin: Boolean,
)

data class ErdRemoteBoardState(
    val boardId: String,
    val version: Long,
    val context: ErdBoardContext,
)
