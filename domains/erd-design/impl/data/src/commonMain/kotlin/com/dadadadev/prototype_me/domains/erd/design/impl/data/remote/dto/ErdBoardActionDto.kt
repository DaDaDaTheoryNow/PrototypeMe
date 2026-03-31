package com.dadadadev.prototype_me.domains.erd.design.impl.data.remote.dto

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator

@Serializable
@OptIn(ExperimentalSerializationApi::class)
@JsonClassDiscriminator("type")
sealed class ErdBoardActionDto {
    abstract val actionId: String

    @Serializable
    @SerialName("MoveNode")
    data class MoveNodeDto(
        override val actionId: String,
        val nodeId: String,
        val newPosition: BoardPointDto,
    ) : ErdBoardActionDto()

    @Serializable
    @SerialName("AddNode")
    data class AddNodeDto(
        override val actionId: String,
        val node: ErdEntityNodeDto,
    ) : ErdBoardActionDto()

    @Serializable
    @SerialName("DeleteNode")
    data class DeleteNodeDto(
        override val actionId: String,
        val nodeId: String,
    ) : ErdBoardActionDto()

    @Serializable
    @SerialName("AddEdge")
    data class AddEdgeDto(
        override val actionId: String,
        val edge: ErdRelationEdgeDto,
    ) : ErdBoardActionDto()

    @Serializable
    @SerialName("DeleteEdge")
    data class DeleteEdgeDto(
        override val actionId: String,
        val edgeId: String,
    ) : ErdBoardActionDto()

    @Serializable
    @SerialName("AddField")
    data class AddFieldDto(
        override val actionId: String,
        val nodeId: String,
        val field: ErdNodeFieldDto,
    ) : ErdBoardActionDto()

    @Serializable
    @SerialName("RemoveField")
    data class RemoveFieldDto(
        override val actionId: String,
        val nodeId: String,
        val fieldId: String,
    ) : ErdBoardActionDto()

    @Serializable
    @SerialName("RenameField")
    data class RenameFieldDto(
        override val actionId: String,
        val nodeId: String,
        val fieldId: String,
        val newName: String,
        val newType: FieldTypeDto,
    ) : ErdBoardActionDto()
}
