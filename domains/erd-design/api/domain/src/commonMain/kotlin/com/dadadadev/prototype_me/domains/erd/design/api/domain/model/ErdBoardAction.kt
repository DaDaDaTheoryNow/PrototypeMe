package com.dadadadev.prototype_me.domains.erd.design.api.domain.model

import com.dadadadev.prototype_me.domains.board.core.api.domain.model.BoardMutation
import com.dadadadev.prototype_me.domains.board.core.api.domain.model.DeleteEdgeMutation
import com.dadadadev.prototype_me.domains.board.core.api.domain.model.DeleteEntityMutation
import com.dadadadev.prototype_me.domains.board.core.api.domain.model.MoveEntityMutation
import com.dadadadev.prototype_me.domains.board.core.api.domain.model.UpsertEdgeMutation
import com.dadadadev.prototype_me.domains.board.core.api.domain.model.UpsertEntityMutation

sealed class ErdBoardAction : BoardMutation {
    abstract override val actionId: String

    data class MoveNode(
        val nodeId: String,
        override val newPosition: Position,
        override val actionId: String,
    ) : ErdBoardAction(), MoveEntityMutation {
        override val entityId: String = nodeId
    }

    data class AddNode(
        val node: ErdEntityNode,
        override val actionId: String,
    ) : ErdBoardAction(), UpsertEntityMutation<ErdEntityNode> {
        override val entity: ErdEntityNode = node
    }

    data class DeleteNode(
        val nodeId: String,
        override val actionId: String,
    ) : ErdBoardAction(), DeleteEntityMutation {
        override val entityId: String = nodeId
    }

    data class AddEdge(
        override val edge: ErdRelationEdge,
        override val actionId: String,
    ) : ErdBoardAction(), UpsertEdgeMutation<ErdRelationEdge>

    data class AddField(
        val nodeId: String,
        val field: ErdNodeField,
        override val actionId: String,
    ) : ErdBoardAction()

    data class RemoveField(
        val nodeId: String,
        val fieldId: String,
        override val actionId: String,
    ) : ErdBoardAction()

    data class RenameField(
        val nodeId: String,
        val fieldId: String,
        val newName: String,
        val newType: FieldType,
        override val actionId: String,
    ) : ErdBoardAction()

    data class DeleteEdge(
        override val edgeId: String,
        override val actionId: String,
    ) : ErdBoardAction(), DeleteEdgeMutation
}
