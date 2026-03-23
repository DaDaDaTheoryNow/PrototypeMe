package com.dadadadev.prototype_me.board.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.dadadadev.prototype_me.board.ui.computeGridRenderConfig
import com.dadadadev.prototype_me.board.ui.positiveMod

@Composable
internal fun GridBackground(
    scale: Float,
    panOffset: Offset,
) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val grid = computeGridRenderConfig(scale, size.width, size.height) ?: return@Canvas
        val offX = panOffset.x.positiveMod(grid.stepPx)
        val offY = panOffset.y.positiveMod(grid.stepPx)
        var gx = offX
        while (gx < size.width) {
            var gy = offY
            while (gy < size.height) {
                drawCircle(Color(0xFFDDDDDD), grid.dotRadiusPx, Offset(gx, gy))
                gy += grid.stepPx
            }
            gx += grid.stepPx
        }
    }
}
