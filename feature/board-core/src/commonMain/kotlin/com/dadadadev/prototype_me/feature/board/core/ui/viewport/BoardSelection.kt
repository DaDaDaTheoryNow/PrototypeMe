package com.dadadadev.prototype_me.feature.board.core.ui.viewport

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import com.dadadadev.prototype_me.domains.board.core.api.domain.model.BoardEntity

fun computeBoardEntityScreenBounds(
    entity: BoardEntity,
    scale: Float,
    panOffset: Offset,
    density: Float,
): Rect {
    val left = boardToScreenX(entity.position.x, scale, panOffset.x, density)
    val top = boardToScreenY(entity.position.y, scale, panOffset.y, density)
    return Rect(
        left = left,
        top = top,
        right = left + entity.size.width * density * scale,
        bottom = top + entity.size.height * density * scale,
    )
}

fun <T : BoardEntity> findIntersectingBoardEntityIds(
    rect: Rect,
    entities: Iterable<T>,
    scale: Float,
    panOffset: Offset,
    density: Float,
): Set<String> = entities.asSequence()
    .filter { entity ->
        val bounds = computeBoardEntityScreenBounds(
            entity = entity,
            scale = scale,
            panOffset = panOffset,
            density = density,
        )
        rect.left <= bounds.right &&
            rect.right >= bounds.left &&
            rect.top <= bounds.bottom &&
            rect.bottom >= bounds.top
    }
    .map(BoardEntity::id)
    .toSet()
