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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dadadadev.prototype_me.domains.erd.design.api.domain.model.ErdEntityNode
import com.dadadadev.prototype_me.erd.board.ui.dimens.ErdBoardDimens
import com.dadadadev.prototype_me.erd.board.layout.ErdNodeDimens
import com.dadadadev.prototype_me.erd.board.ui.theme.ErdBoardColors
import com.dadadadev.prototype_me.erd.board.ui.theme.ErdBoardStrings
import com.dadadadev.prototype_me.feature.board.core.ui.viewport.boardToScreenX
import com.dadadadev.prototype_me.feature.board.core.ui.viewport.boardToScreenY

@Composable
internal fun EntityCard(
    node: ErdEntityNode,
    scale: Float,
    panOffset: Offset,
    density: Float,
    isSourceNode: Boolean,
    isSelected: Boolean = false,
    highlightedFieldIds: Set<String> = emptySet(),
) {
    val isLocked = node.lockedBy != null
    val isHighlighted = isSourceNode || isSelected
    val borderColor = when {
        isHighlighted -> ErdBoardColors.borderStrong
        isLocked -> ErdBoardColors.borderLocked
        else -> ErdBoardColors.borderDefault
    }
    val borderWidth = if (isHighlighted) ErdBoardDimens.CARD_BORDER_WIDTH_HIGHLIGHT_DP.dp else ErdBoardDimens.CARD_BORDER_WIDTH_DEFAULT_DP.dp
    val backgroundColor = if (isHighlighted) ErdBoardColors.surfaceCardHighlight else ErdBoardColors.surfaceCard

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
            modifier = Modifier.width(node.size.width.dp),
            shape = RoundedCornerShape(ErdBoardDimens.CARD_CORNER_RADIUS_DP.dp),
            border = BorderStroke(borderWidth, borderColor),
            colors = CardDefaults.cardColors(containerColor = backgroundColor),
            elevation = CardDefaults.cardElevation(
                defaultElevation = if (isHighlighted) ErdBoardDimens.CARD_ELEVATION_HIGHLIGHT_DP.dp
                else ErdBoardDimens.CARD_ELEVATION_DEFAULT_DP.dp,
            ),
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(ErdNodeDimens.CARD_HEADER_DP.dp)
                        .padding(horizontal = ErdBoardDimens.CARD_HEADER_PADDING_HORIZONTAL_DP.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = node.name,
                            fontSize = ErdBoardDimens.CARD_TITLE_FONT_SP.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isLocked) ErdBoardColors.textDisabled else ErdBoardColors.textPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        if (isLocked) {
                            Text(
                                text = ErdBoardStrings.cardLockedLabel(node.lockedBy.orEmpty()),
                                fontSize = ErdBoardDimens.CARD_LOCKED_FONT_SP.sp,
                                color = ErdBoardColors.textHint,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }

                if (node.fields.isNotEmpty()) {
                    HorizontalDivider(color = ErdBoardColors.divider, thickness = ErdNodeDimens.CARD_DIVIDER_DP.dp)
                    node.fields.forEach { field ->
                        val isHighlightedField = field.id in highlightedFieldIds
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(ErdNodeDimens.CARD_FIELD_ROW_DP.dp)
                                .background(
                                    if (isHighlightedField) ErdBoardColors.surfaceFieldHighlight else Color.Transparent,
                                )
                                .padding(start = ErdBoardDimens.CARD_FIELD_PADDING_START_DP.dp, end = ErdBoardDimens.CARD_FIELD_PADDING_END_DP.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = field.name,
                                fontSize = ErdBoardDimens.CARD_FIELD_NAME_FONT_SP.sp,
                                color = if (isHighlightedField) ErdBoardColors.textPrimary else ErdBoardColors.textTertiary,
                                modifier = Modifier.weight(1f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Text(
                                text = field.type.name.lowercase(),
                                fontSize = ErdBoardDimens.CARD_FIELD_TYPE_FONT_SP.sp,
                                color = ErdBoardColors.textHint,
                            )
                        }
                    }
                }
            }
        }
    }
}
