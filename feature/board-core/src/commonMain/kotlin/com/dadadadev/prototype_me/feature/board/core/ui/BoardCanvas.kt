package com.dadadadev.prototype_me.feature.board.core.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.dadadadev.prototype_me.domains.board.core.api.domain.model.BoardEdge
import com.dadadadev.prototype_me.domains.board.core.api.domain.model.BoardEntity
import com.dadadadev.prototype_me.domains.board.core.api.domain.model.BoardPoint

@Composable
fun <T : BoardEntity> BoardCanvas(
    entities: List<T>,
    edges: List<BoardEdge>,
    modifier: Modifier = Modifier,
    minScale: Float = 0.25f,
    maxScale: Float = 4.0f,
    onNodeMove: (nodeId: String, newPosition: BoardPoint) -> Unit,
    onNodeDragStart: (nodeId: String) -> Unit = {},
    onNodeDragEnd: (nodeId: String) -> Unit = {},
    nodeRenderer: @Composable BoxScope.(T) -> Unit,
) {
    var panOffset by remember { mutableStateOf(Offset.Zero) }
    var scale by remember { mutableFloatStateOf(1f) }

    val entitiesById = remember(entities) { entities.associateBy { it.id } }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .pointerInput(Unit) {
                detectTransformGestures { centroid, pan, zoom, _ ->
                    val nextScale = (scale * zoom).coerceIn(minScale, maxScale)
                    val factor = nextScale / scale

                    panOffset = Offset(
                        x = centroid.x - factor * (centroid.x - panOffset.x) + pan.x,
                        y = centroid.y - factor * (centroid.y - panOffset.y) + pan.y,
                    )
                    scale = nextScale
                }
            },
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawGrid(scale = scale, panOffset = panOffset)
            drawEdges(
                edges = edges,
                entitiesById = entitiesById,
                scale = scale,
                panOffset = panOffset,
            )
        }

        entities.forEach { entity ->
            key(entity.id) {
                Box(
                    modifier = Modifier
                        .graphicsLayer {
                            translationX = entity.position.x * scale + panOffset.x
                            translationY = entity.position.y * scale + panOffset.y
                            scaleX = scale
                            scaleY = scale
                            transformOrigin = TransformOrigin(0f, 0f)
                        }
                        .size(
                            width = entity.size.width.coerceAtLeast(1f).dp,
                            height = entity.size.height.coerceAtLeast(1f).dp,
                        )
                        .pointerInput(entity.id, scale) {
                            detectDragGestures(
                                onDragStart = { onNodeDragStart(entity.id) },
                                onDragEnd = { onNodeDragEnd(entity.id) },
                                onDragCancel = { onNodeDragEnd(entity.id) },
                            ) { change, dragAmount ->
                                change.consume()
                                onNodeMove(
                                    entity.id,
                                    BoardPoint(
                                        x = entity.position.x + dragAmount.x / scale,
                                        y = entity.position.y + dragAmount.y / scale,
                                    ),
                                )
                            }
                        },
                ) {
                    nodeRenderer(entity)
                }
            }
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawGrid(
    scale: Float,
    panOffset: Offset,
) {
    val step = (48f * scale).coerceIn(20f, 96f)
    val startX = panOffset.x.positiveMod(step)
    val startY = panOffset.y.positiveMod(step)

    var x = startX
    while (x < size.width) {
        var y = startY
        while (y < size.height) {
            drawCircle(
                color = Color(0xFFDDE4EE),
                radius = 1.5f,
                center = Offset(x, y),
            )
            y += step
        }
        x += step
    }
}

private fun <T : BoardEntity> androidx.compose.ui.graphics.drawscope.DrawScope.drawEdges(
    edges: List<BoardEdge>,
    entitiesById: Map<String, T>,
    scale: Float,
    panOffset: Offset,
) {
    edges.forEach { edge ->
        val source = entitiesById[edge.sourceId] ?: return@forEach
        val target = entitiesById[edge.targetId] ?: return@forEach

        val sourceCenter = BoardPoint(
            x = source.position.x + source.size.width / 2f,
            y = source.position.y + source.size.height / 2f,
        )
        val targetCenter = BoardPoint(
            x = target.position.x + target.size.width / 2f,
            y = target.position.y + target.size.height / 2f,
        )

        drawLine(
            color = Color(0xFF64748B),
            start = sourceCenter.toScreen(scale, panOffset),
            end = targetCenter.toScreen(scale, panOffset),
            strokeWidth = 2f,
        )
    }
}

private fun BoardPoint.toScreen(scale: Float, panOffset: Offset): Offset {
    return Offset(
        x = x * scale + panOffset.x,
        y = y * scale + panOffset.y,
    )
}

private fun Float.positiveMod(base: Float): Float {
    if (base == 0f) return this
    val value = this % base
    return if (value < 0f) value + base else value
}
