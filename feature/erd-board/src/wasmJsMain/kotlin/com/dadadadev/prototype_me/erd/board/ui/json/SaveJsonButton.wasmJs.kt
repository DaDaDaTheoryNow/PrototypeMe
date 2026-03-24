package com.dadadadev.prototype_me.erd.board.ui.json

import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Triggers a browser file download via a dynamically created anchor element.
 * Uses @JsFun for Kotlin/Wasm → JS interop.
 */
@JsFun(
    """(filename, content) => {
        var a = document.createElement('a');
        a.href = 'data:text/plain;charset=utf-8,' + encodeURIComponent(content);
        a.download = filename;
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
    }"""
)
private external fun triggerBrowserDownload(filename: String, content: String)

@Composable
internal actual fun SaveJsonButton(filename: String, content: String) {
    val latestContent by rememberUpdatedState(content)
    val latestFilename by rememberUpdatedState(filename)

    TextButton(onClick = { triggerBrowserDownload(latestFilename, latestContent) }) {
        Text(
            "Save to File",
            color = Color(0xFF111111),
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
        )
    }
}
