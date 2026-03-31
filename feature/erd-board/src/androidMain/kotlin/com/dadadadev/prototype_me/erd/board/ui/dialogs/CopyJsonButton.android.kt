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
import kotlinx.coroutines.launch
import com.dadadadev.prototype_me.erd.board.ui.dimens.ErdBoardDimens
import com.dadadadev.prototype_me.erd.board.ui.theme.ErdBoardColors
import com.dadadadev.prototype_me.erd.board.ui.theme.ErdBoardStrings

private const val CLIPBOARD_LABEL = "Board JSON"

@Composable
internal actual fun CopyJsonButton(content: String) {
    val clipboard = LocalClipboard.current
    val latestContent by rememberUpdatedState(content)
    val scope = rememberCoroutineScope()

    TextButton(
        onClick = {
            scope.launch {
                clipboard.setClipEntry(
                    ClipData.newPlainText(CLIPBOARD_LABEL, latestContent).toClipEntry(),
                )
            }
        },
    ) {
        Text(
            ErdBoardStrings.JSON_COPY_BUTTON,
            color = ErdBoardColors.textPrimary,
            fontSize = ErdBoardDimens.PLATFORM_BUTTON_FONT_SP.sp,
            fontWeight = FontWeight.Medium,
        )
    }
}
