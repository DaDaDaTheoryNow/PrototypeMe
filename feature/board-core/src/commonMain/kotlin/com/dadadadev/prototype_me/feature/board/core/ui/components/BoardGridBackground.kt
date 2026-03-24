package com.dadadadev.prototype_me.feature.board.core.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.dadadadev.prototype_me.feature.board.core.ui.viewport.positiveMod
import kotlin.math.sqrt

@Composable
fun BoardGridBackground(
    scale: Float,
    density: Float,
    panOffset: Offset,
    modifier: Modifier = Modifier,
    dotColor: Color = Color(0xFFDDDDDD),
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val grid = computeBoardGridRenderConfig(scale, density, size.width, size.height) ?: return@Canvas
        val offsetX = panOffset.x.positiveMod(grid.stepPx)
        val offsetY = panOffset.y.positiveMod(grid.stepPx)

        var x = offsetX
        while (x < size.width) {
            var y = offsetY
            while (y < size.height) {
                drawCircle(dotColor, grid.dotRadiusPx, Offset(x, y))
                y += grid.stepPx
            }
            x += grid.stepPx
        }
    }
}

private data class BoardGridRenderConfig(
    val stepPx: Float,
    val dotRadiusPx: Float,
)

private fun computeBoardGridRenderConfig(
    scale: Float,
    density: Float,
    viewportWidth: Float,
    viewportHeight: Float,
): BoardGridRenderConfig? {
    if (scale <= 0f || density <= 0f || viewportWidth <= 0f || viewportHeight <= 0f) return null

    var step = GRID_BASE_STEP_PX * density * scale
    while (step < GRID_MIN_STEP_PX) {
        step *= 2f
    }

    val estimatedColumns = (viewportWidth / step).toInt() + 2
    val estimatedRows = (viewportHeight / step).toInt() + 2
    val estimatedDots = estimatedColumns * estimatedRows
    if (estimatedDots > GRID_MAX_DOTS_PER_FRAME) {
        val factor = sqrt(estimatedDots.toFloat() / GRID_MAX_DOTS_PER_FRAME.toFloat())
        step *= factor
    }

    val dotRadius = (1.5f * density * scale).coerceIn(1f * density, 3f * density)
    return BoardGridRenderConfig(stepPx = step, dotRadiusPx = dotRadius)
}

private const val GRID_BASE_STEP_PX = 32f
private const val GRID_MIN_STEP_PX = 14f
private const val GRID_MAX_DOTS_PER_FRAME = 4500
