package com.dadadadev.prototype_me.feature.board.core.ui.input

import androidx.compose.foundation.focusable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.pointerInput

/**
 * Attaches board-wide keyboard shortcut handling to any composable:
 * - Escape  -> [onEscape]
 * - Ctrl+Z  -> [onUndo]
 * - Ctrl+C  -> [onCopy]   (desktop only in practice)
 * - Ctrl+V  -> [onPaste]  (desktop only in practice)
 * - Delete   -> [onDelete]
 *
 * Uses [onPreviewKeyEvent] (top-down interception) so shortcuts fire reliably
 * even when a child element (toolbar button, node card) currently holds focus.
 * Dialog text fields are in a separate focus scope and are not affected.
 *
 * Place this on the root composable of any board screen.
 */
@Composable
fun Modifier.boardKeyboardShortcuts(
    isActive: Boolean = true,
    focusRestoreKey: Any? = Unit,
    onEscape: () -> Unit = {},
    onUndo: () -> Unit = {},
    onCopy: () -> Unit = {},
    onPaste: () -> Unit = {},
    onDelete: () -> Unit = {},
): Modifier {
    val currentOnEscape by rememberUpdatedState(onEscape)
    val currentOnUndo by rememberUpdatedState(onUndo)
    val currentOnCopy by rememberUpdatedState(onCopy)
    val currentOnPaste by rememberUpdatedState(onPaste)
    val currentOnDelete by rememberUpdatedState(onDelete)

    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(isActive, focusRestoreKey) {
        if (isActive) {
            focusRequester.requestFocus()
        }
    }

    return this
        .focusRequester(focusRequester)
        .focusable(enabled = isActive)
        .pointerInput(isActive) {
            if (!isActive) return@pointerInput

            awaitPointerEventScope {
                var wasPressed = false
                while (true) {
                    val event = awaitPointerEvent()
                    val isPressed = event.changes.any { it.pressed }
                    if (isPressed && !wasPressed) {
                        focusRequester.requestFocus()
                    }
                    wasPressed = isPressed
                }
            }
        }
        .onPreviewKeyEvent { event ->
            if (!isActive) return@onPreviewKeyEvent false
            if (event.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false
            when {
                event.key == Key.Escape -> { currentOnEscape(); true }
                event.key == Key.Delete || event.key == Key.Backspace -> { currentOnDelete(); true }
                event.isCtrlPressed && event.key == Key.Z -> { currentOnUndo(); true }
                event.isCtrlPressed && event.key == Key.C -> { currentOnCopy(); true }
                event.isCtrlPressed && event.key == Key.V -> { currentOnPaste(); true }
                else -> false
            }
        }
}

