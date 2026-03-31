package com.dadadadev.prototype_me.domains.erd.design.impl.data.remote.dto

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.JsonNames
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateBoardRequestDto(
    val displayName: String,
    val title: String? = null,
)

@Serializable
data class CreateBoardResponseDto(
    val approved: Boolean,
    val boardId: String,
    val inviteToken: String,
    val webUrl: String,
    val deepLink: String,
    val createdAt: String,
)

@Serializable
data class JoinBoardRequestDto(
    val displayName: String,
    val inviteToken: String? = null,
)

@Serializable
data class JoinBoardResponseDto(
    val boardId: String,
    val userId: String,
    val displayName: String,
    val role: BoardRoleDto,
    val sessionToken: String,
    val expiresAt: String,
)

@Serializable
data class ResolveInviteResponseDto(
    val boardId: String,
    val webUrl: String,
    val deepLink: String,
    val canJoin: Boolean,
)

@Serializable
@OptIn(ExperimentalSerializationApi::class)
data class GetBoardResponseDto(
    @JsonNames("boardId", "board_id")
    val boardId: String,
    val version: Long,
    val context: ErdBoardContextDto,
)

@Serializable
data class SubmitActionRequestDto(
    val action: ErdBoardActionDto,
)

@Serializable
data class SubmitActionResponseDto(
    val accepted: Boolean,
    val actionId: String,
    val serverVersion: Long,
)

@Serializable
data class ActionRejectedResponseDto(
    val accepted: Boolean,
    val actionId: String,
    val reason: String,
    val lockedBy: String? = null,
)

@Serializable
data class ImportBoardResponseDto(
    val accepted: Boolean,
)

@Serializable
data class LockRequestDto(
    val nodeId: String,
)

@Serializable
data class LockGrantedResponseDto(
    val granted: Boolean,
    val nodeId: String,
    val lockedBy: String,
)

@Serializable
data class LockRejectedResponseDto(
    val granted: Boolean,
    val nodeId: String,
    val lockedBy: String,
)
