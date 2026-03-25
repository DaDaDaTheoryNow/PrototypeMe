package com.dadadadev.prototype_me.erd.board.ui.dialogs

import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import kotlin.js.ExperimentalWasmJsInterop

@OptIn(ExperimentalWasmJsInterop::class)
@JsFun("(content) => navigator.clipboard.writeText(content)")
private external fun writeClipboardText(content: String)

@Composable
internal actual fun CopyJsonButton(content: String) {
    val latestContent by rememberUpdatedState(content)

    TextButton(onClick = { writeClipboardText(latestContent) }) {
        Text(
            "Copy",
            color = Color(0xFF111111),
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
        )
    }
}
