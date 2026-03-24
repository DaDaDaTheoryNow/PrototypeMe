package com.dadadadev.prototype_me.feature.board.core.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke

@Composable
fun BoardSelectionMarquee(
    marqueeRect: Rect?,
    modifier: Modifier = Modifier,
    fillColor: Color = Color(0x223B82F6),
    strokeColor: Color = Color(0xFF3B82F6),
) {
    if (marqueeRect == null) return

    Canvas(modifier = modifier.fillMaxSize()) {
        val topLeft = Offset(marqueeRect.left, marqueeRect.top)
        val size = Size(marqueeRect.width, marqueeRect.height)
        drawRect(color = fillColor, topLeft = topLeft, size = size)
        drawRect(
            color = strokeColor,
            topLeft = topLeft,
            size = size,
            style = Stroke(width = 1.5f),
        )
    }
}
