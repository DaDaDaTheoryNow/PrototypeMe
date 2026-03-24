package com.dadadadev.prototype_me.feature.board.core.ui.input

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.PointerType
import androidx.compose.ui.input.pointer.isSecondaryPressed
import kotlinx.coroutines.withTimeoutOrNull

suspend fun PointerInputScope.canvasGestureHandler(
    onPanZoom: (centroid: Offset, pan: Offset, zoom: Float) -> Unit,
    onTap: (Offset) -> Unit,
) {
    awaitEachGesture {
        val first = awaitFirstDown(requireUnconsumed = true)
        val downPosition = first.position
        var primaryPosition = first.position
        var secondaryPointerId = Long.MIN_VALUE
        var secondaryPosition: Offset? = null
        var movedPastSlop = false
        var hadSecondaryButton = false

        while (true) {
            val event = awaitPointerEvent()
            if (event.buttons.isSecondaryPressed) hadSecondaryButton = true

            for (change in event.changes) {
                if (
                    change.pressed &&
                    !change.previousPressed &&
                    change.id.value != first.id.value &&
                    secondaryPointerId == Long.MIN_VALUE
                ) {
                    secondaryPointerId = change.id.value
                    secondaryPosition = change.position
                    movedPastSlop = true
                }
            }

            val primaryChange = event.changes.firstOrNull { it.id == first.id }
            if (primaryChange == null || !primaryChange.pressed) {
                if (!movedPastSlop && !hadSecondaryButton) {
                    onTap(downPosition)
                }
                break
            }

            val secondaryChange = if (secondaryPointerId != Long.MIN_VALUE) {
                event.changes.firstOrNull { it.id.value == secondaryPointerId }
            } else {
                null
            }

            val newPrimaryPosition = primaryChange.position
            val newSecondaryPosition = if (secondaryChange?.pressed == true) secondaryChange.position else null

            if (!movedPastSlop && (newPrimaryPosition - downPosition).getDistance() > viewConfiguration.touchSlop) {
                movedPastSlop = true
            }

            if (movedPastSlop) {
                if (newSecondaryPosition != null && secondaryPosition != null) {
                    val previousSecondaryPosition = secondaryPosition
                    val centroid = (newPrimaryPosition + newSecondaryPosition) / 2f
                    val previousCentroid = (primaryPosition + previousSecondaryPosition) / 2f
                    val previousDistance = (primaryPosition - previousSecondaryPosition).getDistance()
                    val newDistance = (newPrimaryPosition - newSecondaryPosition).getDistance()
                    val zoom = if (previousDistance > 1f) newDistance / previousDistance else 1f

                    event.changes.forEach { it.consume() }
                    onPanZoom(centroid, centroid - previousCentroid, zoom)
                    secondaryPosition = newSecondaryPosition
                } else {
                    val pan = newPrimaryPosition - primaryPosition
                    if (pan.getDistance() > 0f && primaryChange.type != PointerType.Mouse) {
                        primaryChange.consume()
                        onPanZoom(newPrimaryPosition, pan, 1f)
                    }
                }
            }

            primaryPosition = newPrimaryPosition
            if (secondaryChange?.pressed == false) {
                secondaryPointerId = Long.MIN_VALUE
                secondaryPosition = null
            }
        }
    }
}

suspend fun PointerInputScope.nodeGestureHandler(
    scale: Float,
    onDragStart: () -> Unit,
    onDrag: (Offset) -> Unit,
    onDragEnd: () -> Unit,
    onTap: () -> Unit,
    onLongPress: () -> Unit,
) {
    awaitEachGesture {
        val down = awaitFirstDown(requireUnconsumed = true)
        down.consume()

        val downPosition = down.position
        var isDragging = false
        var wasReleased = false

        val completedBeforeLongPress = withTimeoutOrNull(viewConfiguration.longPressTimeoutMillis) {
            while (true) {
                val event = awaitPointerEvent()
                val change = event.changes.find { it.id == down.id } ?: break

                if (!change.pressed) {
                    wasReleased = true
                    change.consume()
                    break
                }

                val distance = (change.position - downPosition).getDistance()
                if (!isDragging && distance > viewConfiguration.touchSlop) {
                    isDragging = true
                    onDragStart()
                }

                if (isDragging) {
                    change.consume()
                    onDrag(
                        Offset(
                            x = (change.position.x - change.previousPosition.x) * scale,
                            y = (change.position.y - change.previousPosition.y) * scale,
                        ),
                    )
                }
            }
        }

        if (isDragging) {
            if (!wasReleased) {
                while (true) {
                    val event = awaitPointerEvent()
                    val change = event.changes.find { it.id == down.id } ?: break
                    if (!change.pressed) {
                        change.consume()
                        break
                    }
                    change.consume()
                    onDrag(
                        Offset(
                            x = (change.position.x - change.previousPosition.x) * scale,
                            y = (change.position.y - change.previousPosition.y) * scale,
                        ),
                    )
                }
            }
            onDragEnd()
        } else if (completedBeforeLongPress == null) {
            onLongPress()
        } else if (wasReleased) {
            onTap()
        }
    }
}
