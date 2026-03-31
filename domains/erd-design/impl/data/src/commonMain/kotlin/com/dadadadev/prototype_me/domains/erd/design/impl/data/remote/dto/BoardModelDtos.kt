package com.dadadadev.prototype_me.domains.erd.design.impl.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BoardPointDto(
    val x: Float,
    val y: Float,
)

@Serializable
data class BoardSizeDto(
    val width: Float,
    val height: Float,
)

@Serializable
enum class BoardRoleDto {
    @SerialName("owner")
    OWNER,

    @SerialName("editor")
    EDITOR,

    @SerialName("viewer")
    VIEWER,
}

@Serializable
enum class FieldTypeDto {
    TEXT,
    NUMBER,
    BOOLEAN,
    DATE,
}

@Serializable
data class ErdNodeFieldDto(
    val id: String,
    val name: String,
    val type: FieldTypeDto,
)

@Serializable
data class ErdEntityNodeDto(
    val id: String,
    val name: String,
    val position: BoardPointDto,
    val fields: List<ErdNodeFieldDto>,
    val size: BoardSizeDto,
    val lockedBy: String? = null,
)

@Serializable
data class ErdRelationEdgeDto(
    val id: String,
    val sourceNodeId: String,
    val sourceFieldId: String? = null,
    val targetNodeId: String,
    val targetFieldId: String? = null,
    val label: String? = null,
)

@Serializable
data class ErdBoardContextDto(
    val boardId: String,
    val nodes: Map<String, ErdEntityNodeDto>,
    val edges: Map<String, ErdRelationEdgeDto>,
)

@Serializable
data class ErdExportFieldPayloadDto(
    val id: String,
    val name: String,
    val type: FieldTypeDto,
)

@Serializable
data class ErdExportNodePayloadDto(
    val name: String,
    val fields: List<ErdExportFieldPayloadDto>,
)

@Serializable
data class ErdExportNodeItemDto(
    val id: String,
    val position: BoardPointDto,
    val payload: ErdExportNodePayloadDto,
)

@Serializable
data class ErdExportEdgePayloadDto(
    val sourceFieldId: String? = null,
    val targetFieldId: String? = null,
    val label: String? = null,
)

@Serializable
data class ErdExportEdgeItemDto(
    val id: String,
    val sourceId: String,
    val targetId: String,
    val payload: ErdExportEdgePayloadDto,
)

@Serializable
data class ErdBoardExportDocumentDto(
    val format: String,
    val version: Int,
    val boardType: String,
    val nodes: List<ErdExportNodeItemDto>,
    val edges: List<ErdExportEdgeItemDto>,
)
