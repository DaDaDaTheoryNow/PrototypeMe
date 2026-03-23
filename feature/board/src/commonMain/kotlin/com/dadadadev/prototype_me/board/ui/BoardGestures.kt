package com.dadadadev.prototype_me.board.ui

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.PointerType
import androidx.compose.ui.input.pointer.isSecondaryPressed
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Combined canvas gesture handler for LMB pan, two-finger pinch+pan, and tap.
 * A single handler avoids race conditions between separate gesture detectors.
 */
internal suspend fun PointerInputScope.canvasGestureHandler(
    onPanZoom: (centroid: Offset, pan: Offset, zoom: Float) -> Unit,
    onTap: (Offset) -> Unit,
) {
    awaitEachGesture {
        val first = awaitFirstDown(requireUnconsumed = true)
        val downPos = first.position
        var pos1 = first.position
        var secondId = Long.MIN_VALUE
        var pos2: Offset? = null
        var movedPastSlop = false
        var hadSecondaryButton = false
        while (true) {
            val event = awaitPointerEvent()
            if (event.buttons.isSecondaryPressed) hadSecondaryButton = true
            // Detect second finger
            for (c in event.changes) {
                if (c.pressed && !c.previousPressed &&
                    c.id.value != first.id.value && secondId == Long.MIN_VALUE
                ) {
                    secondId = c.id.value
                    pos2 = c.position
                    movedPastSlop = true // two-finger always counts as gesture
                }
            }
            val c1 = event.changes.firstOrNull { it.id == first.id }
            if (c1 == null || !c1.pressed) {
                // Released: if never moved past slop and not RMB, it's a tap.
                if (!movedPastSlop && !hadSecondaryButton) onTap(downPos)
                break
            }
            val c2 = if (secondId != Long.MIN_VALUE)
                event.changes.firstOrNull { it.id.value == secondId } else null
            val newPos1 = c1.position
            val newPos2 = if (c2?.pressed == true) c2.position else null
            if (!movedPastSlop) {
                if ((newPos1 - downPos).getDistance() > viewConfiguration.touchSlop) {
                    movedPastSlop = true
                }
            }
            if (movedPastSlop) {
                if (newPos2 != null && pos2 != null) {
                    // Two-finger: pinch zoom + pan
                    val prevSecond = pos2
                    val centroid = (newPos1 + newPos2) / 2f
                    val prevCentroid = (pos1 + prevSecond) / 2f
                    val prevDist = (pos1 - prevSecond).getDistance()
                    val newDist = (newPos1 - newPos2).getDistance()
                    val zoom = if (prevDist > 1f) newDist / prevDist else 1f
                    event.changes.forEach { it.consume() }
                    onPanZoom(centroid, centroid - prevCentroid, zoom)
                    pos2 = newPos2
                } else {
                    // Single pointer pan: touch/pen only (mouse pans via RMB handler)
                    val pan = newPos1 - pos1
                    if (pan.getDistance() > 0f && c1.type != PointerType.Mouse) {
                        c1.consume()
                        onPanZoom(newPos1, pan, 1f)
                    }
                }
            }
            pos1 = newPos1
            if (c2?.pressed == false) {
                secondId = Long.MIN_VALUE; pos2 = null
            }
        }
    }
}

/**
 * Handles node card gestures: tap, long-press, and drag.
 * Consumes only the primary pointer so RMB events reach the board-level handler.
 *
 * [scale] is needed to convert local card coordinates to screen delta.
 */
internal suspend fun PointerInputScope.nodeGestureHandler(
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

        val downPos = down.position
        var isDragging = false
        var released = false

        val completed = withTimeoutOrNull(viewConfiguration.longPressTimeoutMillis) {
            while (true) {
                val event = awaitPointerEvent()
                val change = event.changes.find { it.id == down.id } ?: break
                if (!change.pressed) {
                    released = true
                    change.consume()
                    break
                }
                val dist = (change.position - downPos).getDistance()
                if (!isDragging && dist > viewConfiguration.touchSlop) {
                    isDragging = true
                    onDragStart()
                }
                if (isDragging) {
                    change.consume()
                    onDrag(
                        Offset(
                            (change.position.x - change.previousPosition.x) * scale,
                            (change.position.y - change.previousPosition.y) * scale,
                        )
                    )
                }
            }
        }

        if (isDragging) {
            if (!released) {
                // Continue drag after long-press timeout fires
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
                            (change.position.x - change.previousPosition.x) * scale,
                            (change.position.y - change.previousPosition.y) * scale,
                        )
                    )
                }
            }
            onDragEnd()
        } else if (completed == null) {
            onLongPress()
        } else if (released) {
            onTap()
        }
    }
}
