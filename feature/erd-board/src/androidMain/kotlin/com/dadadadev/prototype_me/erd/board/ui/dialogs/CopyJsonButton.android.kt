package com.dadadadev.prototype_me.erd.board.ui.dialogs

import android.content.ClipData
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.toClipEntry
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

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
            "Copy",
            color = Color(0xFF111111),
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
        )
    }
}
