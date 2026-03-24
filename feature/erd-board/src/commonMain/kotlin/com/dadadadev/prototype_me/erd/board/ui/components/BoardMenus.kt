package com.dadadadev.prototype_me.erd.board.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dadadadev.prototype_me.domains.erd.design.api.domain.model.EntityNode
import com.dadadadev.prototype_me.feature.board.core.ui.viewport.boardToScreenX
import com.dadadadev.prototype_me.feature.board.core.ui.viewport.boardToScreenY
import kotlin.math.roundToInt

@Composable
internal fun NodeActionMenu(
    node: EntityNode,
    scale: Float,
    panOffset: Offset,
    density: Float,
    onEditFields: () -> Unit,
    onDelete: () -> Unit,
) {
    val cardRightPx = boardToScreenX(node.position.x, scale, panOffset.x, density) +
        node.size.width * density * scale
    val cardTopPx = boardToScreenY(node.position.y, scale, panOffset.y, density)

    Box(
        modifier = Modifier
            .offset {
                IntOffset(
                    (cardRightPx + 6.dp.toPx()).roundToInt(),
                    cardTopPx.roundToInt(),
                )
            }
            .background(Color.White, RoundedCornerShape(10.dp))
            .border(1.dp, Color(0xFFEEEEEE), RoundedCornerShape(10.dp))
            .padding(vertical = 4.dp, horizontal = 2.dp),
    ) {
        Column {
            TextButton(onClick = onEditFields) {
                Text("Edit Fields", fontSize = 12.sp, color = Color(0xFF333333))
            }
            TextButton(onClick = onDelete) {
                Text("Delete", fontSize = 12.sp, color = Color(0xFFCC3333))
            }
        }
    }
}

@Composable
internal fun MultiSelectMenu(
    anchorPos: Offset,
    selectedCount: Int,
    screenW: Float,
    screenH: Float,
    onCopy: () -> Unit,
    onDeleteAll: () -> Unit,
) {
    val menuX = anchorPos.x.coerceIn(4f, screenW - 140f)
    val menuY = anchorPos.y.coerceIn(4f, screenH - 80f)

    Box(
        modifier = Modifier
            .offset { IntOffset(menuX.roundToInt(), menuY.roundToInt()) }
            .background(Color.White, RoundedCornerShape(10.dp))
            .border(1.dp, Color(0xFFEEEEEE), RoundedCornerShape(10.dp))
            .padding(vertical = 4.dp, horizontal = 2.dp),
    ) {
        Column {
            Text(
                text = "$selectedCount selected",
                fontSize = 11.sp,
                color = Color(0xFF777777),
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            )
            TextButton(onClick = onCopy) {
                Text("Copy", fontSize = 12.sp, color = Color(0xFF2255CC))
            }
            TextButton(onClick = onDeleteAll) {
                Text("Delete", fontSize = 12.sp, color = Color(0xFFCC3333))
            }
        }
    }
}
