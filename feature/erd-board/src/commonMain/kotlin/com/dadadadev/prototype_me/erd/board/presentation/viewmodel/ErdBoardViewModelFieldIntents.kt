package com.dadadadev.prototype_me.erd.board.presentation.viewmodel

import com.dadadadev.prototype_me.erd.board.layout.withFields
import com.dadadadev.prototype_me.erd.board.presentation.ErdBoardViewModel
import com.dadadadev.prototype_me.erd.board.presentation.contract.ErdBoardIntent
import com.dadadadev.prototype_me.erd.board.presentation.viewmodel.undo.ErdUndoAction

internal fun ErdBoardViewModel.handleFieldIntent(boardIntent: ErdBoardIntent) = intent {
    when (boardIntent) {
        is ErdBoardIntent.OnSelectNode -> reduce {
            state.copy(selectedNodeId = boardIntent.nodeId, nodeMenuNodeId = null)
        }

        is ErdBoardIntent.OnAddField -> {
            val node = state.nodes[boardIntent.nodeId] ?: return@intent
            val field = useCases.addField(boardIntent.nodeId, boardIntent.name, boardIntent.type)
            val updatedNode = node.withFields(node.fields + field)
            runtimeState = runtimeState.pushUndo(ErdUndoAction.FieldAdded(boardIntent.nodeId, field.id))
            reduce {
                state.copy(
                    nodes = state.nodes + (boardIntent.nodeId to updatedNode),
                    canUndo = runtimeState.canUndo,
                )
            }
        }

        is ErdBoardIntent.OnRemoveField -> {
            val node = state.nodes[boardIntent.nodeId] ?: return@intent
            val field = node.fields.firstOrNull { it.id == boardIntent.fieldId } ?: return@intent
            val updatedNode = node.withFields(node.fields.filter { it.id != boardIntent.fieldId })
            runtimeState = runtimeState.pushUndo(ErdUndoAction.FieldRemoved(boardIntent.nodeId, field))
            reduce {
                state.copy(
                    nodes = state.nodes + (boardIntent.nodeId to updatedNode),
                    canUndo = runtimeState.canUndo,
                )
            }
            useCases.removeField(boardIntent.nodeId, boardIntent.fieldId)
        }

        is ErdBoardIntent.OnRenameField -> {
            val node = state.nodes[boardIntent.nodeId] ?: return@intent
            val field = node.fields.firstOrNull { it.id == boardIntent.fieldId } ?: return@intent
            val updatedFields = node.fields.map { currentField ->
                if (currentField.id == boardIntent.fieldId) {
                    currentField.copy(name = boardIntent.newName, type = boardIntent.newType)
                } else {
                    currentField
                }
            }
            runtimeState = runtimeState.pushUndo(
                ErdUndoAction.FieldRenamed(
                    boardIntent.nodeId,
                    boardIntent.fieldId,
                    field.name,
                    field.type,
                ),
            )
            reduce {
                state.copy(
                    nodes = state.nodes + (boardIntent.nodeId to node.withFields(updatedFields)),
                    canUndo = runtimeState.canUndo,
                )
            }
            useCases.renameField(
                nodeId = boardIntent.nodeId,
                fieldId = boardIntent.fieldId,
                newName = boardIntent.newName,
                newType = boardIntent.newType,
            )
        }

        else -> Unit
    }
}
