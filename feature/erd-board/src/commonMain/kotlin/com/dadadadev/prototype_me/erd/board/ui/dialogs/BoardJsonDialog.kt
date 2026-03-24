package com.dadadadev.prototype_me.erd.board.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dadadadev.prototype_me.erd.board.ui.json.SaveJsonButton

private const val EXPORT_FILENAME = "board.json"

@Composable
internal fun BoardJsonDialog(
    currentJson: String,
    onImport: (json: String) -> Unit,
    onDismiss: () -> Unit,
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var importText by remember { mutableStateOf("") }
    var importError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        shape = RoundedCornerShape(12.dp),
        title = {
            Text(
                "Board JSON",
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                color = Color(0xFF111111),
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.Transparent,
                    contentColor = Color(0xFF111111),
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("View / Export", fontSize = 12.sp) },
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1; importError = null },
                        text = { Text("Import", fontSize = 12.sp) },
                    )
                }

                Spacer(Modifier.height(12.dp))

                when (selectedTab) {
                    0 -> ViewExportTab(json = currentJson)
                    1 -> ImportTab(
                        text = importText,
                        error = importError,
                        onTextChange = { importText = it; importError = null },
                    )
                }
            }
        },
        confirmButton = {
            when (selectedTab) {
                0 -> {
                    val clipboard = LocalClipboardManager.current
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        SaveJsonButton(filename = EXPORT_FILENAME, content = currentJson)
                        TextButton(onClick = { clipboard.setText(AnnotatedString(currentJson)) }) {
                            Text("Copy", color = Color(0xFF111111), fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
                1 -> {
                    TextButton(onClick = {
                        val trimmed = importText.trim()
                        when {
                            trimmed.isBlank() -> importError = "Paste JSON to import"
                            else -> onImport(trimmed)
                        }
                    }) {
                        Text("Import", color = Color(0xFF111111), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = Color(0xFF888888), fontSize = 13.sp)
            }
        },
    )
}

// ── Tabs ──────────────────────────────────────────────────────────────────────

@Composable
private fun ViewExportTab(json: String) {
    val scrollV = rememberScrollState()
    val scrollH = rememberScrollState()
    SelectionContainer {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 200.dp, max = 360.dp)
                .border(1.dp, Color(0xFFEEEEEE), RoundedCornerShape(8.dp))
                .background(Color(0xFFFAFAFA), RoundedCornerShape(8.dp))
                .verticalScroll(scrollV)
                .horizontalScroll(scrollH)
                .padding(12.dp),
        ) {
            Text(
                text = json,
                style = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = Color(0xFF333333),
                    lineHeight = 16.sp,
                ),
            )
        }
    }
}

@Composable
private fun ImportTab(
    text: String,
    error: String?,
    onTextChange: (String) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            "Paste board JSON below.",
            fontSize = 12.sp,
            color = Color(0xFF888888),
            modifier = Modifier.padding(bottom = 8.dp),
        )
        OutlinedTextField(
            value = text,
            onValueChange = onTextChange,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 160.dp, max = 300.dp),
            placeholder = { Text("{ \"version\": 1, \"nodes\": [...], ... }", color = Color(0xFFCCCCCC), fontSize = 11.sp) },
            textStyle = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 11.sp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = if (error != null) Color(0xFFCC3333) else Color(0xFF111111),
                unfocusedBorderColor = if (error != null) Color(0xFFCC3333) else Color(0xFFDDDDDD),
                cursorColor = Color(0xFF111111),
            ),
            isError = error != null,
        )
        if (error != null) {
            Text(
                error,
                fontSize = 11.sp,
                color = Color(0xFFCC3333),
                modifier = Modifier.padding(top = 4.dp),
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(
            "Warning: importing replaces the current board.",
            fontSize = 11.sp,
            color = Color(0xFFAAAAAA),
        )
    }
}
