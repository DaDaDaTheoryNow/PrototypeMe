package com.dadadadev.prototype_me.erd.board.presentation.viewmodel.undo

import com.dadadadev.prototype_me.domains.erd.design.api.domain.model.EntityNode
import com.dadadadev.prototype_me.domains.erd.design.api.domain.model.FieldType
import com.dadadadev.prototype_me.domains.erd.design.api.domain.model.NodeField
import com.dadadadev.prototype_me.domains.erd.design.api.domain.model.Position
import com.dadadadev.prototype_me.domains.erd.design.api.domain.model.RelationEdge

/**
 * Describes a single reversible board operation stored on the undo stack.
 *
 * Each variant captures exactly the data needed to reconstruct the previous
 * state so the presentation layer can send compensating repository actions.
 */
sealed class ErdUndoAction {
    data class NodeAdded(val nodeId: String) : ErdUndoAction()

    data class NodeDeleted(
        val node: EntityNode,
        val edges: List<RelationEdge>,
    ) : ErdUndoAction()

    data class NodeMoved(
        val nodeId: String,
        val previousPosition: Position,
    ) : ErdUndoAction()

    data class BatchAdded(
        val nodeIds: List<String>,
        val edgeIds: List<String>,
    ) : ErdUndoAction()

    data class NodesDeleted(
        val nodes: List<EntityNode>,
        val edges: List<RelationEdge>,
    ) : ErdUndoAction()

    data class EdgeAdded(val edgeId: String) : ErdUndoAction()
    data class EdgeDeleted(val edge: RelationEdge) : ErdUndoAction()
    data class FieldAdded(val nodeId: String, val fieldId: String) : ErdUndoAction()
    data class FieldRemoved(val nodeId: String, val field: NodeField) : ErdUndoAction()

    data class FieldRenamed(
        val nodeId: String,
        val fieldId: String,
        val previousName: String,
        val previousType: FieldType,
    ) : ErdUndoAction()

    data class NodesMoved(val previousPositions: Map<String, Position>) : ErdUndoAction()
}
