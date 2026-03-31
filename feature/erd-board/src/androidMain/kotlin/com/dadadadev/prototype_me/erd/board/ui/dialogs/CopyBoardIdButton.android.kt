package com.dadadadev.prototype_me.erd.board.ui.dialogs

import android.content.ClipData
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.toClipEntry
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.dadadadev.prototype_me.erd.board.ui.dimens.ErdBoardDimens
import com.dadadadev.prototype_me.erd.board.ui.theme.ErdBoardColors
import com.dadadadev.prototype_me.erd.board.ui.theme.ErdBoardStrings
import kotlinx.coroutines.launch

private const val CLIPBOARD_LABEL = "Board ID"

@Composable
internal actual fun CopyBoardIdButton(boardId: String) {
    val clipboard = LocalClipboard.current
    val latestBoardId by rememberUpdatedState(boardId)
    val scope = rememberCoroutineScope()

    TextButton(
        onClick = {
            scope.launch {
                clipboard.setClipEntry(
                    ClipData.newPlainText(CLIPBOARD_LABEL, latestBoardId).toClipEntry(),
                )
            }
        },
    ) {
        Text(
            ErdBoardStrings.SHARE_COPY_BUTTON,
            color = ErdBoardColors.textPrimary,
            fontSize = ErdBoardDimens.PLATFORM_BUTTON_FONT_SP.sp,
            fontWeight = FontWeight.Medium,
        )
    }
}
