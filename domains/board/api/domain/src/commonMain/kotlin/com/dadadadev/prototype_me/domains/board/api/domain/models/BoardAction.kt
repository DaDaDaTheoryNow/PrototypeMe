package com.dadadadev.prototype_me.domain.models

sealed class BoardAction {
    abstract val actionId: String

    data class MoveNode(
        val nodeId: String,
        val newPosition: Position,
        override val actionId: String
    ) : BoardAction()

    data class AddNode(
        val node: EntityNode,
        override val actionId: String
    ) : BoardAction()

    data class AddEdge(
        val edge: RelationEdge,
        override val actionId: String
    ) : BoardAction()

    data class AddField(
        val nodeId: String,
        val field: NodeField,
        override val actionId: String
    ) : BoardAction()

    data class RemoveField(
        val nodeId: String,
        val fieldId: String,
        override val actionId: String
    ) : BoardAction()

    data class RenameField(
        val nodeId: String,
        val fieldId: String,
        val newName: String,
        val newType: FieldType,
        override val actionId: String
    ) : BoardAction()

    data class DeleteEdge(
        val edgeId: String,
        override val actionId: String
    ) : BoardAction()

    data class ChangeEdgeType(
        val edgeId: String,
        val newType: RelationType,
        override val actionId: String
    ) : BoardAction()
}
