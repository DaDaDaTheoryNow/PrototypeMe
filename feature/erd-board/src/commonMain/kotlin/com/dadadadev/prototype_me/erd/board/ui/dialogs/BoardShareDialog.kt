package com.dadadadev.prototype_me.erd.board.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dadadadev.prototype_me.erd.board.ui.dimens.ErdBoardDimens
import com.dadadadev.prototype_me.erd.board.ui.theme.ErdBoardColors
import com.dadadadev.prototype_me.erd.board.ui.theme.ErdBoardStrings

@Composable
internal fun BoardShareDialog(
    boardId: String,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ErdBoardColors.surfaceDialog,
        shape = RoundedCornerShape(ErdBoardDimens.DIALOG_CORNER_RADIUS_DP.dp),
        title = {
            Text(
                text = ErdBoardStrings.SHARE_DIALOG_TITLE,
                fontWeight = FontWeight.SemiBold,
                fontSize = ErdBoardDimens.DIALOG_TITLE_FONT_SP.sp,
                color = ErdBoardColors.textPrimary,
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = ErdBoardStrings.SHARE_BOARD_ID_LABEL,
                    fontSize = ErdBoardDimens.DIALOG_BODY_FONT_SP.sp,
                    color = ErdBoardColors.textDisabled,
                    modifier = Modifier.padding(bottom = ErdBoardDimens.DIALOG_LABEL_BOTTOM_PADDING_DP.dp),
                )

                SelectionContainer {
                    Text(
                        text = boardId,
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = ErdBoardDimens.DIALOG_CODE_BORDER_WIDTH_DP.dp,
                                color = ErdBoardColors.divider,
                                shape = RoundedCornerShape(ErdBoardDimens.DIALOG_CODE_CORNER_RADIUS_DP.dp),
                            )
                            .background(
                                color = ErdBoardColors.surfaceCodeBlock,
                                shape = RoundedCornerShape(ErdBoardDimens.DIALOG_CODE_CORNER_RADIUS_DP.dp),
                            )
                            .padding(ErdBoardDimens.DIALOG_CODE_PADDING_DP.dp),
                        style = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = ErdBoardDimens.DIALOG_BUTTON_FONT_SP.sp,
                            color = ErdBoardColors.textSecondary,
                        ),
                    )
                }
            }
        },
        confirmButton = {
            CopyBoardIdButton(boardId = boardId)
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = ErdBoardStrings.SHARE_CLOSE,
                    color = ErdBoardColors.textDisabled,
                    fontSize = ErdBoardDimens.DIALOG_BUTTON_FONT_SP.sp,
                )
            }
        },
    )
}
