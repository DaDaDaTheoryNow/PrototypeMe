package com.dadadadev.prototype_me.erd.board.ui.components

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
internal fun SelectionMarquee(marqueeRect: Rect?) {
    if (marqueeRect == null) return
    Canvas(modifier = Modifier.fillMaxSize()) {
        val topLeft = Offset(marqueeRect.left, marqueeRect.top)
        val size = Size(marqueeRect.width, marqueeRect.height)
        drawRect(color = Color(0x223B82F6), topLeft = topLeft, size = size)
        drawRect(color = Color(0xFF3B82F6), topLeft = topLeft, size = size, style = Stroke(width = 1.5f))
    }
}

