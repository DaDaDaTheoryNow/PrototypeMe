package com.dadadadev.prototype_me.feature.board.core.ui.input

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.isSecondaryPressed
import androidx.compose.ui.input.pointer.pointerInput

@Composable
fun Modifier.boardMouseWheelZoom(
    zoomInFactor: Float = 1.12f,
    zoomOutFactor: Float = 0.88f,
    onZoom: (centroid: Offset, zoom: Float) -> Unit,
): Modifier {
    val currentOnZoom by rememberUpdatedState(onZoom)

    return composed {
        pointerInput(zoomInFactor, zoomOutFactor) {
            awaitPointerEventScope {
                while (true) {
                    val event = awaitPointerEvent()
                    if (event.type != PointerEventType.Scroll) continue

                    val change = event.changes.firstOrNull() ?: continue
                    val deltaY = change.scrollDelta.y
                    if (deltaY == 0f) continue

                    change.consume()
                    currentOnZoom(
                        change.position,
                        if (deltaY < 0f) zoomInFactor else zoomOutFactor,
                    )
                }
            }
        }
    }
}

@Composable
fun Modifier.boardSecondaryButtonPan(
    onPan: (Offset) -> Unit,
    onClick: (Offset) -> Unit = {},
): Modifier {
    val currentOnPan by rememberUpdatedState(onPan)
    val currentOnClick by rememberUpdatedState(onClick)

    return composed {
        pointerInput(Unit) {
            awaitPointerEventScope {
                var previousMousePosition: Offset? = null
                var secondaryDownPosition: Offset? = null
                var secondaryMoved = false

                while (true) {
                    val event = awaitPointerEvent()
                    val change = event.changes.firstOrNull() ?: continue

                    if (event.buttons.isSecondaryPressed) {
                        if (secondaryDownPosition == null) {
                            secondaryDownPosition = change.position
                            previousMousePosition = change.position
                            secondaryMoved = false
                            continue
                        }

                        val previousPosition = previousMousePosition ?: change.position
                        val pan = change.position - previousPosition
                        if (pan.getDistance() > 0f) {
                            if (!secondaryMoved) {
                                val start = secondaryDownPosition
                                if ((change.position - start).getDistance() > viewConfiguration.touchSlop) {
                                    secondaryMoved = true
                                }
                            }
                            if (secondaryMoved) {
                                change.consume()
                                currentOnPan(pan)
                            }
                        }
                        previousMousePosition = change.position
                    } else {
                        if (secondaryDownPosition != null && !secondaryMoved) {
                            currentOnClick(change.position)
                        }
                        previousMousePosition = null
                        secondaryDownPosition = null
                        secondaryMoved = false
                    }
                }
            }
        }
    }
}
