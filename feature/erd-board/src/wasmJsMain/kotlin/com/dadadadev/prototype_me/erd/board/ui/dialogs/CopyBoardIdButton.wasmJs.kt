package com.dadadadev.prototype_me.erd.board.ui.dialogs

import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.dadadadev.prototype_me.erd.board.ui.dimens.ErdBoardDimens
import com.dadadadev.prototype_me.erd.board.ui.theme.ErdBoardColors
import com.dadadadev.prototype_me.erd.board.ui.theme.ErdBoardStrings
import kotlin.js.ExperimentalWasmJsInterop

@OptIn(ExperimentalWasmJsInterop::class)
@JsFun("(content) => navigator.clipboard.writeText(content)")
private external fun writeClipboardText(content: String)

@Composable
internal actual fun CopyBoardIdButton(boardId: String) {
    val latestBoardId by rememberUpdatedState(boardId)

    TextButton(onClick = { writeClipboardText(latestBoardId) }) {
        Text(
            ErdBoardStrings.SHARE_COPY_BUTTON,
            color = ErdBoardColors.textPrimary,
            fontSize = ErdBoardDimens.PLATFORM_BUTTON_FONT_SP.sp,
            fontWeight = FontWeight.Medium,
        )
    }
}
