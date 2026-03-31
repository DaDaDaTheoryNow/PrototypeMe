package com.dadadadev.prototype_me.erd.board.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dadadadev.prototype_me.domains.erd.design.api.domain.model.ErdEntityNode
import com.dadadadev.prototype_me.domains.erd.design.api.domain.model.ErdRelationEdge
import com.dadadadev.prototype_me.erd.board.ui.canvas.buildEdgeLabel
import com.dadadadev.prototype_me.erd.board.ui.dimens.ErdBoardDimens
import com.dadadadev.prototype_me.erd.board.ui.theme.ErdBoardColors
import com.dadadadev.prototype_me.erd.board.ui.theme.ErdBoardStrings
import kotlin.math.roundToInt

/** Bottom bar with add, undo and JSON-view actions. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun AddEntityToolbar(
    modifier: Modifier = Modifier,
    canUndo: Boolean = false,
    onUndo: (() -> Unit)? = null,
    onShowJson: () -> Unit,
    onShare: () -> Unit,
    onAddEntity: () -> Unit,
) {
    HorizontalFloatingToolbar(
        expanded = true,
        modifier = modifier,
        colors = FloatingToolbarDefaults.standardFloatingToolbarColors(
            toolbarContainerColor = ErdBoardColors.surfaceToolbar,
        ),
        contentPadding = PaddingValues(horizontal = ErdBoardDimens.TOOLBAR_PADDING_H_DP.dp, vertical = ErdBoardDimens.TOOLBAR_PADDING_V_DP.dp),
    ) {
        if (onUndo != null) {
            TextButton(onClick = onUndo, enabled = canUndo) {
                Text(
                    text = ErdBoardStrings.TOOLBAR_UNDO,
                    color = if (canUndo) ErdBoardColors.toolbarEnabled else ErdBoardColors.toolbarDisabled,
                    fontSize = ErdBoardDimens.TOOLBAR_FONT_LARGE_SP.sp,
                    fontWeight = FontWeight.Medium,
                )
            }
            ToolbarDivider()
        }
        TextButton(onClick = onAddEntity) {
            Text(
                ErdBoardStrings.TOOLBAR_ADD_ENTITY,
                color = ErdBoardColors.textPrimary,
                fontSize = ErdBoardDimens.TOOLBAR_FONT_LARGE_SP.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
        ToolbarDivider()
        TextButton(onClick = onShowJson) {
            Text(
                ErdBoardStrings.TOOLBAR_JSON,
                color = ErdBoardColors.textMuted,
                fontSize = ErdBoardDimens.TOOLBAR_FONT_LARGE_SP.sp,
                fontWeight = FontWeight.Medium,
            )
        }
        ToolbarDivider()
        TextButton(onClick = onShare) {
            Text(
                ErdBoardStrings.TOOLBAR_SHARE,
                color = ErdBoardColors.textMuted,
                fontSize = ErdBoardDimens.TOOLBAR_FONT_LARGE_SP.sp,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

@Composable
private fun RowScope.ToolbarDivider() {
    VerticalDivider(
        modifier = Modifier
            .height(ErdBoardDimens.TOOLBAR_DIVIDER_HEIGHT_DP.dp)
            .padding(horizontal = ErdBoardDimens.TOOLBAR_DIVIDER_PADDING_H_DP.dp)
            .align(Alignment.CenterVertically),
        color = ErdBoardColors.separator,
    )
}

/** Floating toolbar shown next to the selected edge. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun EdgeSelectionToolbar(
    edge: ErdRelationEdge,
    midpoint: Offset?,
    screenW: Float,
    screenH: Float,
    nodes: Map<String, ErdEntityNode>,
    onDeleteEdge: () -> Unit,
) {
    val tbX = ((midpoint?.x ?: (screenW / 2f)) - ErdBoardDimens.EDGE_TOOLBAR_HALF_WIDTH_PX).coerceIn(ErdBoardDimens.MENU_SCREEN_MARGIN_PX, screenW - ErdBoardDimens.MENU_ESTIMATED_WIDTH_PX)
    val tbY = ((midpoint?.y ?: (screenH - ErdBoardDimens.MENU_ESTIMATED_HEIGHT_PX)) - ErdBoardDimens.EDGE_TOOLBAR_HEIGHT_PX).coerceIn(ErdBoardDimens.MENU_SCREEN_MARGIN_PX, screenH - ErdBoardDimens.EDGE_TOOLBAR_HEIGHT_PX)

    HorizontalFloatingToolbar(
        expanded = true,
        modifier = Modifier.offset { IntOffset(tbX.roundToInt(), tbY.roundToInt()) },
        colors = FloatingToolbarDefaults.standardFloatingToolbarColors(
            toolbarContainerColor = ErdBoardColors.surfaceToolbar,
        ),
        contentPadding = PaddingValues(horizontal = ErdBoardDimens.TOOLBAR_PADDING_H_DP.dp, vertical = ErdBoardDimens.TOOLBAR_PADDING_V_DP.dp),
    ) {
        Text(
            text = buildEdgeLabel(edge, nodes),
            fontSize = ErdBoardDimens.TOOLBAR_FONT_SMALL_SP.sp,
            color = ErdBoardColors.textSubtle,
            modifier = Modifier.padding(horizontal = ErdBoardDimens.TOOLBAR_EDGE_LABEL_PADDING_H_DP.dp),
        )
        TextButton(onClick = onDeleteEdge) {
            Text(
                ErdBoardStrings.EDGE_DELETE,
                color = ErdBoardColors.accentRed,
                fontSize = ErdBoardDimens.TOOLBAR_FONT_MEDIUM_SP.sp,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

/** Top-center hint banner shown while connect mode is active. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun ConnectingHintBanner(
    connectingFromNodeId: String?,
    connectingFromFieldId: String?,
    nodes: Map<String, ErdEntityNode>,
    modifier: Modifier = Modifier,
) {
    if (connectingFromNodeId == null) return

    val srcName = nodes[connectingFromNodeId]?.let { n ->
        val field = n.fields.firstOrNull { it.id == connectingFromFieldId }
        if (field != null) "${n.name}.${field.name}" else n.name
    }

    HorizontalFloatingToolbar(
        expanded = true,
        modifier = modifier,
        colors = FloatingToolbarDefaults.standardFloatingToolbarColors(
            toolbarContainerColor = ErdBoardColors.toolbarBackground,
            toolbarContentColor = ErdBoardColors.textOnAccent,
        ),
        contentPadding = PaddingValues(horizontal = ErdBoardDimens.TOOLBAR_HINT_PADDING_H_DP.dp, vertical = ErdBoardDimens.TOOLBAR_HINT_PADDING_V_DP.dp),
    ) {
        Text(
            text = if (srcName == null) ErdBoardStrings.CONNECT_TAP_TO_START else ErdBoardStrings.connectFromLabel(srcName),
            color = ErdBoardColors.textOnAccent,
            fontSize = ErdBoardDimens.TOOLBAR_FONT_MEDIUM_SP.sp,
            fontWeight = FontWeight.Medium,
        )
    }
}
