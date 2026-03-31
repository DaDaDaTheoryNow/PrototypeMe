package com.dadadadev.prototype_me.domains.erd.design.impl.data.remote.mapper

import com.dadadadev.prototype_me.domains.board.core.api.domain.model.BoardPoint
import com.dadadadev.prototype_me.domains.board.core.api.domain.model.BoardSize
import com.dadadadev.prototype_me.domains.erd.design.api.domain.model.ErdBoardCreationApproval
import com.dadadadev.prototype_me.domains.erd.design.api.domain.model.ErdBoardJoinSession
import com.dadadadev.prototype_me.domains.erd.design.api.domain.model.ErdBoardParticipantRole
import com.dadadadev.prototype_me.domains.erd.design.api.domain.model.ErdInviteResolution
import com.dadadadev.prototype_me.domains.erd.design.api.domain.model.ErdRemoteBoardState
import com.dadadadev.prototype_me.domains.erd.design.impl.data.remote.dto.BoardPointDto
import com.dadadadev.prototype_me.domains.erd.design.impl.data.remote.dto.BoardRoleDto
import com.dadadadev.prototype_me.domains.erd.design.impl.data.remote.dto.BoardSizeDto
import com.dadadadev.prototype_me.domains.erd.design.impl.data.remote.dto.CreateBoardResponseDto
import com.dadadadev.prototype_me.domains.erd.design.impl.data.remote.dto.ErdBoardActionDto
import com.dadadadev.prototype_me.domains.erd.design.impl.data.remote.dto.ErdBoardContextDto
import com.dadadadev.prototype_me.domains.erd.design.impl.data.remote.dto.ErdEntityNodeDto
import com.dadadadev.prototype_me.domains.erd.design.impl.data.remote.dto.ErdNodeFieldDto
import com.dadadadev.prototype_me.domains.erd.design.impl.data.remote.dto.ErdRelationEdgeDto
import com.dadadadev.prototype_me.domains.erd.design.impl.data.remote.dto.FieldTypeDto
import com.dadadadev.prototype_me.domains.erd.design.impl.data.remote.dto.GetBoardResponseDto
import com.dadadadev.prototype_me.domains.erd.design.impl.data.remote.dto.JoinBoardResponseDto
import com.dadadadev.prototype_me.domains.erd.design.impl.data.remote.dto.ResolveInviteResponseDto
import com.dadadadev.prototype_me.domains.erd.design.api.domain.model.ErdBoardAction
import com.dadadadev.prototype_me.domains.erd.design.api.domain.model.ErdBoardContext
import com.dadadadev.prototype_me.domains.erd.design.api.domain.model.ErdEntityNode
import com.dadadadev.prototype_me.domains.erd.design.api.domain.model.ErdNodeField
import com.dadadadev.prototype_me.domains.erd.design.api.domain.model.ErdRelationEdge
import com.dadadadev.prototype_me.domains.erd.design.api.domain.model.FieldType

internal fun CreateBoardResponseDto.toDomain(): ErdBoardCreationApproval = ErdBoardCreationApproval(
    approved = approved,
    boardId = boardId,
    inviteToken = inviteToken,
    webUrl = webUrl,
    deepLink = deepLink,
    createdAt = createdAt,
)

internal fun JoinBoardResponseDto.toDomain(): ErdBoardJoinSession = ErdBoardJoinSession(
    boardId = boardId,
    userId = userId,
    displayName = displayName,
    role = role.toDomain(),
    sessionToken = sessionToken,
    expiresAt = expiresAt,
)

internal fun ResolveInviteResponseDto.toDomain(): ErdInviteResolution = ErdInviteResolution(
    boardId = boardId,
    webUrl = webUrl,
    deepLink = deepLink,
    canJoin = canJoin,
)

internal fun GetBoardResponseDto.toDomain(): ErdRemoteBoardState = ErdRemoteBoardState(
    boardId = boardId,
    version = version,
    context = context.toDomain(),
)

internal fun BoardRoleDto.toDomain(): ErdBoardParticipantRole = when (this) {
    BoardRoleDto.OWNER -> ErdBoardParticipantRole.OWNER
    BoardRoleDto.EDITOR -> ErdBoardParticipantRole.EDITOR
    BoardRoleDto.VIEWER -> ErdBoardParticipantRole.VIEWER
}

internal fun ErdBoardContextDto.toDomain(): ErdBoardContext = ErdBoardContext(
    boardId = boardId,
    nodes = nodes.mapValues { (_, node) -> node.toDomain() },
    edges = edges.mapValues { (_, edge) -> edge.toDomain() },
)

internal fun ErdEntityNodeDto.toDomain(): ErdEntityNode = ErdEntityNode(
    id = id,
    name = name,
    position = position.toDomain(),
    fields = fields.map(ErdNodeFieldDto::toDomain),
    size = size.toDomain(),
    lockedBy = lockedBy,
)

internal fun ErdRelationEdgeDto.toDomain(): ErdRelationEdge = ErdRelationEdge(
    id = id,
    sourceNodeId = sourceNodeId,
    sourceFieldId = sourceFieldId,
    targetNodeId = targetNodeId,
    targetFieldId = targetFieldId,
    label = label,
)

internal fun ErdNodeFieldDto.toDomain(): ErdNodeField = ErdNodeField(
    id = id,
    name = name,
    type = type.toDomain(),
)

internal fun BoardPointDto.toDomain(): BoardPoint = BoardPoint(x = x, y = y)

internal fun BoardSizeDto.toDomain(): BoardSize = BoardSize(width = width, height = height)

internal fun FieldTypeDto.toDomain(): FieldType = when (this) {
    FieldTypeDto.TEXT -> FieldType.TEXT
    FieldTypeDto.NUMBER -> FieldType.NUMBER
    FieldTypeDto.BOOLEAN -> FieldType.BOOLEAN
    FieldTypeDto.DATE -> FieldType.DATE
}

internal fun FieldType.toDto(): FieldTypeDto = when (this) {
    FieldType.TEXT -> FieldTypeDto.TEXT
    FieldType.NUMBER -> FieldTypeDto.NUMBER
    FieldType.BOOLEAN -> FieldTypeDto.BOOLEAN
    FieldType.DATE -> FieldTypeDto.DATE
}

internal fun BoardPoint.toDto(): BoardPointDto = BoardPointDto(x = x, y = y)

internal fun BoardSize.toDto(): BoardSizeDto = BoardSizeDto(width = width, height = height)

internal fun ErdNodeField.toDto(): ErdNodeFieldDto = ErdNodeFieldDto(
    id = id,
    name = name,
    type = type.toDto(),
)

internal fun ErdEntityNode.toDto(): ErdEntityNodeDto = ErdEntityNodeDto(
    id = id,
    name = name,
    position = position.toDto(),
    fields = fields.map(ErdNodeField::toDto),
    size = size.toDto(),
    lockedBy = lockedBy,
)

internal fun ErdRelationEdge.toDto(): ErdRelationEdgeDto = ErdRelationEdgeDto(
    id = id,
    sourceNodeId = sourceNodeId,
    sourceFieldId = sourceFieldId,
    targetNodeId = targetNodeId,
    targetFieldId = targetFieldId,
    label = label,
)

internal fun ErdBoardAction.toDto(): ErdBoardActionDto = when (this) {
    is ErdBoardAction.MoveNode -> ErdBoardActionDto.MoveNodeDto(
        actionId = actionId,
        nodeId = nodeId,
        newPosition = newPosition.toDto(),
    )

    is ErdBoardAction.AddNode -> ErdBoardActionDto.AddNodeDto(
        actionId = actionId,
        node = node.toDto(),
    )

    is ErdBoardAction.DeleteNode -> ErdBoardActionDto.DeleteNodeDto(
        actionId = actionId,
        nodeId = nodeId,
    )

    is ErdBoardAction.AddEdge -> ErdBoardActionDto.AddEdgeDto(
        actionId = actionId,
        edge = edge.toDto(),
    )

    is ErdBoardAction.DeleteEdge -> ErdBoardActionDto.DeleteEdgeDto(
        actionId = actionId,
        edgeId = edgeId,
    )

    is ErdBoardAction.AddField -> ErdBoardActionDto.AddFieldDto(
        actionId = actionId,
        nodeId = nodeId,
        field = field.toDto(),
    )

    is ErdBoardAction.RemoveField -> ErdBoardActionDto.RemoveFieldDto(
        actionId = actionId,
        nodeId = nodeId,
        fieldId = fieldId,
    )

    is ErdBoardAction.RenameField -> ErdBoardActionDto.RenameFieldDto(
        actionId = actionId,
        nodeId = nodeId,
        fieldId = fieldId,
        newName = newName,
        newType = newType.toDto(),
    )
}

internal fun ErdBoardActionDto.toDomain(): ErdBoardAction = when (this) {
    is ErdBoardActionDto.MoveNodeDto -> ErdBoardAction.MoveNode(
        actionId = actionId,
        nodeId = nodeId,
        newPosition = newPosition.toDomain(),
    )

    is ErdBoardActionDto.AddNodeDto -> ErdBoardAction.AddNode(
        actionId = actionId,
        node = node.toDomain(),
    )

    is ErdBoardActionDto.DeleteNodeDto -> ErdBoardAction.DeleteNode(
        actionId = actionId,
        nodeId = nodeId,
    )

    is ErdBoardActionDto.AddEdgeDto -> ErdBoardAction.AddEdge(
        actionId = actionId,
        edge = edge.toDomain(),
    )

    is ErdBoardActionDto.DeleteEdgeDto -> ErdBoardAction.DeleteEdge(
        actionId = actionId,
        edgeId = edgeId,
    )

    is ErdBoardActionDto.AddFieldDto -> ErdBoardAction.AddField(
        actionId = actionId,
        nodeId = nodeId,
        field = field.toDomain(),
    )

    is ErdBoardActionDto.RemoveFieldDto -> ErdBoardAction.RemoveField(
        actionId = actionId,
        nodeId = nodeId,
        fieldId = fieldId,
    )

    is ErdBoardActionDto.RenameFieldDto -> ErdBoardAction.RenameField(
        actionId = actionId,
        nodeId = nodeId,
        fieldId = fieldId,
        newName = newName,
        newType = newType.toDomain(),
    )
}
