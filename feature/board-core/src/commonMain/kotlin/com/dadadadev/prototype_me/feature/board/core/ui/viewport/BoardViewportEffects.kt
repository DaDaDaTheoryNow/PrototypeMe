package com.dadadadev.prototype_me.feature.board.core.ui.viewport

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.geometry.Rect

@Composable
fun <T> BoardInitialViewportFitEffect(
    items: Collection<T>,
    viewportWidth: Float,
    viewportHeight: Float,
    density: Float,
    hasApplied: Boolean,
    minScale: Float,
    maxScale: Float,
    boundsOf: (T) -> Rect,
    paddingPx: Float = 24f * density,
    onTransformComputed: (BoardViewportTransform) -> Unit,
) {
    LaunchedEffect(items, viewportWidth, viewportHeight, density, hasApplied) {
        if (hasApplied) return@LaunchedEffect
        if (items.isEmpty() || viewportWidth <= 0f || viewportHeight <= 0f) return@LaunchedEffect

        val contentBounds = computeBoardContentBounds(items, boundsOf) ?: return@LaunchedEffect
        val viewportTransform = fitBoardContentToViewport(
            bounds = contentBounds,
            viewportWidth = viewportWidth,
            viewportHeight = viewportHeight,
            density = density,
            paddingPx = paddingPx,
            minScale = minScale,
            maxScale = maxScale,
        ) ?: return@LaunchedEffect

        onTransformComputed(viewportTransform)
    }
}
