package com.dadadadev.prototype_me.feature.board.core.ui.viewport

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import com.dadadadev.prototype_me.domains.board.core.api.domain.model.BoardEntity
import com.dadadadev.prototype_me.domains.board.core.api.domain.model.BoardPoint
import kotlin.math.max
import kotlin.math.min

data class BoardViewportTransform(
    val scale: Float,
    val panOffset: Offset,
)

fun boardToScreenX(boardX: Float, scale: Float, panX: Float, density: Float): Float =
    boardX * density * scale + panX

fun boardToScreenY(boardY: Float, scale: Float, panY: Float, density: Float): Float =
    boardY * density * scale + panY

fun boardToScreenOffset(
    point: BoardPoint,
    scale: Float,
    panOffset: Offset,
    density: Float,
): Offset = Offset(
    x = boardToScreenX(point.x, scale, panOffset.x, density),
    y = boardToScreenY(point.y, scale, panOffset.y, density),
)

fun screenToBoardOffset(
    point: Offset,
    scale: Float,
    panOffset: Offset,
    density: Float,
): Offset {
    val safeFactor = (scale * density).coerceAtLeast(MIN_TRANSFORM_FACTOR)
    return Offset(
        x = (point.x - panOffset.x) / safeFactor,
        y = (point.y - panOffset.y) / safeFactor,
    )
}

fun screenDeltaToBoardDelta(
    delta: Offset,
    scale: Float,
    density: Float,
): Offset {
    val safeFactor = (scale * density).coerceAtLeast(MIN_TRANSFORM_FACTOR)
    return Offset(
        x = delta.x / safeFactor,
        y = delta.y / safeFactor,
    )
}

fun computeBoardEntityBounds(entity: BoardEntity): Rect = Rect(
    left = entity.position.x,
    top = entity.position.y,
    right = entity.position.x + entity.size.width,
    bottom = entity.position.y + entity.size.height,
)

fun applyBoardPanZoom(
    scale: Float,
    panOffset: Offset,
    centroid: Offset,
    pan: Offset,
    zoom: Float,
    minScale: Float,
    maxScale: Float,
): BoardViewportTransform {
    val newScale = (scale * zoom).coerceIn(minScale, maxScale)
    val factor = newScale / scale
    return BoardViewportTransform(
        scale = newScale,
        panOffset = Offset(
            x = centroid.x - factor * (centroid.x - panOffset.x) + pan.x,
            y = centroid.y - factor * (centroid.y - panOffset.y) + pan.y,
        ),
    )
}

fun fitBoardContentToViewport(
    bounds: Rect,
    viewportWidth: Float,
    viewportHeight: Float,
    density: Float,
    paddingPx: Float,
    minScale: Float,
    maxScale: Float,
    maxInitialScale: Float = 1f,
): BoardViewportTransform? {
    if (viewportWidth <= 0f || viewportHeight <= 0f || density <= 0f) return null

    val availableWidthPx = (viewportWidth - paddingPx * 2f).coerceAtLeast(1f)
    val availableHeightPx = (viewportHeight - paddingPx * 2f).coerceAtLeast(1f)
    val contentWidthPx = bounds.width * density
    val contentHeightPx = bounds.height * density

    if (contentWidthPx <= availableWidthPx && contentHeightPx <= availableHeightPx) return null

    val fitScale = min(
        availableWidthPx / contentWidthPx,
        availableHeightPx / contentHeightPx,
    ).coerceIn(minScale, min(maxScale, maxInitialScale))

    return BoardViewportTransform(
        scale = fitScale,
        panOffset = Offset(
            x = (viewportWidth - bounds.width * density * fitScale) / 2f - bounds.left * density * fitScale,
            y = (viewportHeight - bounds.height * density * fitScale) / 2f - bounds.top * density * fitScale,
        ),
    )
}

fun <T> computeBoardContentBounds(
    items: Collection<T>,
    boundsOf: (T) -> Rect,
): Rect? {
    if (items.isEmpty()) return null

    var left = Float.POSITIVE_INFINITY
    var top = Float.POSITIVE_INFINITY
    var right = Float.NEGATIVE_INFINITY
    var bottom = Float.NEGATIVE_INFINITY

    items.forEach { item ->
        val bounds = boundsOf(item)
        left = min(left, bounds.left)
        top = min(top, bounds.top)
        right = max(right, bounds.right)
        bottom = max(bottom, bounds.bottom)
    }

    return Rect(left, top, right, bottom)
}

fun buildSelectionRect(start: Offset, end: Offset): Rect = Rect(
    left = min(start.x, end.x),
    top = min(start.y, end.y),
    right = max(start.x, end.x),
    bottom = max(start.y, end.y),
)

fun Float.positiveMod(base: Float): Float {
    if (base == 0f) return this
    val value = this % base
    return if (value < 0f) value + base else value
}

private const val MIN_TRANSFORM_FACTOR = 0.0001f
