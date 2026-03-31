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
import com.dadadadev.prototype_me.domains.erd.design.api.domain.model.ErdEntityNode
import com.dadadadev.prototype_me.erd.board.ui.dimens.ErdBoardDimens
import com.dadadadev.prototype_me.erd.board.ui.theme.ErdBoardColors
import com.dadadadev.prototype_me.erd.board.ui.theme.ErdBoardStrings
import com.dadadadev.prototype_me.feature.board.core.ui.viewport.boardToScreenX
import com.dadadadev.prototype_me.feature.board.core.ui.viewport.boardToScreenY
import kotlin.math.roundToInt

private val MenuShape = RoundedCornerShape(ErdBoardDimens.MENU_CORNER_RADIUS_DP.dp)

@Composable
internal fun NodeActionMenu(
    node: ErdEntityNode,
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
                (cardRightPx + ErdBoardDimens.MENU_OFFSET_FROM_NODE_DP.dp.toPx()).roundToInt(),
                cardTopPx.roundToInt(),
            )
        },
    ) {
        MenuAction(
            label = ErdBoardStrings.MENU_EDIT_FIELDS,
            color = ErdBoardColors.textSecondary,
            onClick = onEditFields,
        )
        MenuDivider()
        MenuAction(
            label = ErdBoardStrings.MENU_DELETE,
            color = ErdBoardColors.accentRed,
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
    val menuX = anchorPos.x.coerceIn(ErdBoardDimens.MENU_SCREEN_MARGIN_PX, screenW - ErdBoardDimens.MENU_ESTIMATED_WIDTH_PX)
    val menuY = anchorPos.y.coerceIn(ErdBoardDimens.MENU_SCREEN_MARGIN_PX, screenH - ErdBoardDimens.MENU_ESTIMATED_HEIGHT_PX)

    CompactMenu(
        modifier = Modifier.offset { IntOffset(menuX.roundToInt(), menuY.roundToInt()) },
    ) {
        Text(
            text = ErdBoardStrings.multiSelectCount(selectedCount),
            fontSize = ErdBoardDimens.MENU_COUNT_FONT_SP.sp,
            color = ErdBoardColors.textDisabled,
            modifier = Modifier.padding(horizontal = ErdBoardDimens.MENU_ACTION_PADDING_H_DP.dp, vertical = ErdBoardDimens.MENU_ACTION_PADDING_V_DP.dp),
        )
        MenuDivider()
        MenuAction(
            label = ErdBoardStrings.MENU_COPY,
            color = ErdBoardColors.accentBlue,
            fontWeight = FontWeight.Medium,
            onClick = onCopy,
        )
        MenuDivider()
        MenuAction(
            label = ErdBoardStrings.MENU_DELETE,
            color = ErdBoardColors.accentRed,
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
        color = ErdBoardColors.surfaceMenu,
        shape = MenuShape,
        border = BorderStroke(ErdBoardDimens.MENU_BORDER_WIDTH_DP.dp, ErdBoardColors.borderLight),
        tonalElevation = 0.dp,
        shadowElevation = ErdBoardDimens.MENU_SHADOW_ELEVATION_DP.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = ErdBoardDimens.MENU_INNER_PADDING_H_DP.dp, vertical = ErdBoardDimens.MENU_INNER_PADDING_V_DP.dp),
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
            .defaultMinSize(minHeight = ErdBoardDimens.MENU_ACTION_MIN_HEIGHT_DP.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = ErdBoardDimens.MENU_ACTION_PADDING_H_DP.dp, vertical = ErdBoardDimens.MENU_ACTION_PADDING_V_DP.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            fontSize = ErdBoardDimens.MENU_LABEL_FONT_SP.sp,
            color = color,
            fontWeight = fontWeight,
        )
    }
}

@Composable
private fun MenuDivider() {
    VerticalDivider(
        modifier = Modifier
            .height(ErdBoardDimens.MENU_DIVIDER_HEIGHT_DP.dp)
            .padding(horizontal = ErdBoardDimens.MENU_DIVIDER_PADDING_H_DP.dp),
        color = ErdBoardColors.separator,
    )
}
