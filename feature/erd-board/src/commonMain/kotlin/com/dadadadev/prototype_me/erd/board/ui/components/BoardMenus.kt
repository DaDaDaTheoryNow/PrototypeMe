package com.dadadadev.prototype_me.erd.board.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dadadadev.prototype_me.domains.erd.design.api.domain.model.EntityNode
import com.dadadadev.prototype_me.feature.board.core.ui.viewport.boardToScreenX
import com.dadadadev.prototype_me.feature.board.core.ui.viewport.boardToScreenY
import kotlin.math.roundToInt

private val MenuShape = RoundedCornerShape(12.dp)

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

    CompactMenu(
        modifier = Modifier.offset {
            IntOffset(
                (cardRightPx + 6.dp.toPx()).roundToInt(),
                cardTopPx.roundToInt(),
            )
        },
    ) {
        MenuAction(
            label = "Edit Fields",
            color = Color(0xFF333333),
            onClick = onEditFields,
        )
        MenuDivider()
        MenuAction(
            label = "Delete",
            color = Color(0xFFCC3333),
            fontWeight = FontWeight.Medium,
            onClick = onDelete,
        )
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

    CompactMenu(
        modifier = Modifier.offset { IntOffset(menuX.roundToInt(), menuY.roundToInt()) },
    ) {
        Text(
            text = "$selectedCount selected",
            fontSize = 11.sp,
            color = Color(0xFF777777),
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
        )
        MenuDivider()
        MenuAction(
            label = "Copy",
            color = Color(0xFF2255CC),
            fontWeight = FontWeight.Medium,
            onClick = onCopy,
        )
        MenuDivider()
        MenuAction(
            label = "Delete",
            color = Color(0xFFCC3333),
            fontWeight = FontWeight.Medium,
            onClick = onDeleteAll,
        )
    }
}

// ── Shared primitives ──────────────────────────────────────────────────────────

@Composable
private fun CompactMenu(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = modifier
            .padding(0.dp),
        color = Color.White,
        shape = MenuShape,
        border = BorderStroke(1.dp, Color(0xFFEEEEEE)),
        tonalElevation = 0.dp,
        shadowElevation = 4.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            content()
        }
    }
}

@Composable
private fun MenuAction(
    label: String,
    color: Color,
    onClick: () -> Unit,
    fontWeight: FontWeight? = null,
) {
    Box(
        modifier = Modifier
            .defaultMinSize(minHeight = 32.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 7.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = color,
            fontWeight = fontWeight,
        )
    }
}

@Composable
private fun MenuDivider() {
    VerticalDivider(
        modifier = Modifier
            .height(20.dp)
            .padding(horizontal = 2.dp),
        color = Color(0xFFE0E0E0),
    )
}
