package com.dadadadev.prototype_me.erd.board.ui.node

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dadadadev.prototype_me.domains.erd.design.api.domain.model.EntityNode
import com.dadadadev.prototype_me.erd.board.ui.canvas.CARD_DIVIDER_DP
import com.dadadadev.prototype_me.erd.board.ui.canvas.CARD_FIELD_ROW_DP
import com.dadadadev.prototype_me.erd.board.ui.canvas.CARD_HEADER_DP
import com.dadadadev.prototype_me.feature.board.core.ui.input.nodeGestureHandler
import com.dadadadev.prototype_me.feature.board.core.ui.viewport.boardToScreenX
import com.dadadadev.prototype_me.feature.board.core.ui.viewport.boardToScreenY
import com.dadadadev.prototype_me.feature.board.core.ui.viewport.screenDeltaToBoardDelta

@Composable
fun EntityCard(
    node: EntityNode,
    scale: Float,
    panOffset: Offset,
    density: Float,
    isSourceNode: Boolean,
    isSelected: Boolean = false,
    highlightedFieldIds: Set<String> = emptySet(),
    onDragStart: () -> Unit,
    onDrag: (Offset) -> Unit,
    onDragEnd: () -> Unit,
    onTap: () -> Unit,
    onLongPress: () -> Unit,
) {
    val isLocked = node.lockedBy != null
    val isHighlighted = isSourceNode || isSelected
    val borderColor = when {
        isHighlighted -> Color(0xFF111111)
        isLocked -> Color(0xFFBBBBBB)
        else -> Color(0xFFDDDDDD)
    }
    val borderWidth = if (isHighlighted) 2.dp else 1.dp
    val backgroundColor = if (isHighlighted) Color(0xFFF8F8F8) else Color.White

    Box(
        modifier = Modifier.graphicsLayer {
            translationX = boardToScreenX(node.position.x, scale, panOffset.x, density)
            translationY = boardToScreenY(node.position.y, scale, panOffset.y, density)
            scaleX = scale
            scaleY = scale
            transformOrigin = TransformOrigin(0f, 0f)
        },
    ) {
        Card(
            modifier = Modifier
                .width(node.size.width.dp)
                .pointerInput(node.id, scale, density) {
                    nodeGestureHandler(
                        scale = scale,
                        onDragStart = onDragStart,
                        onDrag = { screenDelta ->
                            onDrag(screenDeltaToBoardDelta(screenDelta, scale, density))
                        },
                        onDragEnd = onDragEnd,
                        onTap = onTap,
                        onLongPress = onLongPress,
                    )
                },
            shape = RoundedCornerShape(6.dp),
            border = BorderStroke(borderWidth, borderColor),
            colors = CardDefaults.cardColors(containerColor = backgroundColor),
            elevation = CardDefaults.cardElevation(defaultElevation = if (isHighlighted) 6.dp else 2.dp),
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(CARD_HEADER_DP.dp)
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = node.name,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isLocked) Color(0xFF888888) else Color(0xFF111111),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        if (isLocked) {
                            Text(
                                text = "* ${node.lockedBy}",
                                fontSize = 9.sp,
                                color = Color(0xFFBBBBBB),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }

                if (node.fields.isNotEmpty()) {
                    HorizontalDivider(color = Color(0xFFEEEEEE), thickness = CARD_DIVIDER_DP.dp)
                    node.fields.forEach { field ->
                        val isHighlightedField = field.id in highlightedFieldIds
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(CARD_FIELD_ROW_DP.dp)
                                .background(
                                    if (isHighlightedField) Color(0xFFF2F2F2) else Color.Transparent,
                                )
                                .padding(start = 12.dp, end = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = field.name,
                                fontSize = 11.sp,
                                color = if (isHighlightedField) Color(0xFF111111) else Color(0xFF444444),
                                modifier = Modifier.weight(1f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Text(
                                text = field.type.name.lowercase(),
                                fontSize = 10.sp,
                                color = Color(0xFFBBBBBB),
                            )
                        }
                    }
                }
            }
        }
    }
}
