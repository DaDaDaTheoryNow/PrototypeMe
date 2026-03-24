package com.dadadadev.prototype_me.erd.board.ui.json

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@Composable
internal actual fun SaveJsonButton(filename: String, content: String) {
    val context = LocalContext.current
    val latestContent by rememberUpdatedState(content)

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json"),
    ) { uri ->
        if (uri != null) {
            context.contentResolver.openOutputStream(uri)?.use { stream ->
                stream.write(latestContent.toByteArray(Charsets.UTF_8))
            }
        }
    }

    TextButton(onClick = { launcher.launch(filename) }) {
        Text(
            "Save to File",
            color = Color(0xFF111111),
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
        )
    }
}
