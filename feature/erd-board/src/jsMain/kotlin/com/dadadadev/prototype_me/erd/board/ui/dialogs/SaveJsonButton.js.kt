package com.dadadadev.prototype_me.erd.board.ui.dialogs

import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import kotlinx.browser.document
import org.w3c.dom.HTMLAnchorElement

@JsName("encodeURIComponent")
private external fun encodeURIComponent(value: String): String

@Composable
internal actual fun SaveJsonButton(filename: String, content: String) {
    val latestContent by rememberUpdatedState(content)
    val latestFilename by rememberUpdatedState(filename)

    TextButton(onClick = {
        val anchor = document.createElement("a") as HTMLAnchorElement
        anchor.href = "data:text/plain;charset=utf-8,${encodeURIComponent(latestContent)}"
        anchor.download = latestFilename
        document.body?.appendChild(anchor)
        anchor.click()
        document.body?.removeChild(anchor)
    }) {
        Text(
            "Save to File",
            color = Color(0xFF111111),
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
        )
    }
}
