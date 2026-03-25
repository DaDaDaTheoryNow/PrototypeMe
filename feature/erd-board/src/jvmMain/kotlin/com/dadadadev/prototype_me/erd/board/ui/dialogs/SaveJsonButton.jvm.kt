package com.dadadadev.prototype_me.erd.board.ui.dialogs

import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import java.io.File
import javax.swing.JFileChooser
import javax.swing.SwingUtilities
import javax.swing.filechooser.FileNameExtensionFilter

@Composable
internal actual fun SaveJsonButton(filename: String, content: String) {
    val latestContent by rememberUpdatedState(content)
    val latestFilename by rememberUpdatedState(filename)

    TextButton(onClick = {
        // Show the JFileChooser on the AWT Event Dispatch Thread.
        SwingUtilities.invokeLater {
            val chooser = JFileChooser().apply {
                dialogTitle = "Save Board as JSON"
                selectedFile = File(latestFilename)
                fileFilter = FileNameExtensionFilter("JSON files (*.json)", "json")
            }
            if (chooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
                val target = chooser.selectedFile.let { f ->
                    if (!f.name.endsWith(".json")) File("${f.absolutePath}.json") else f
                }
                target.writeText(latestContent, Charsets.UTF_8)
            }
        }
    }) {
        Text(
            "Save to File",
            color = Color(0xFF111111),
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
        )
    }
}
