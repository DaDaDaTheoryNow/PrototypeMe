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
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dadadadev.prototype_me.erd.board.ui.dimens.ErdBoardDimens
import com.dadadadev.prototype_me.erd.board.ui.theme.ErdBoardColors
import com.dadadadev.prototype_me.erd.board.ui.theme.ErdBoardStrings

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
        containerColor = ErdBoardColors.surfaceDialog,
        shape = RoundedCornerShape(ErdBoardDimens.DIALOG_CORNER_RADIUS_DP.dp),
        title = {
            Text(
                ErdBoardStrings.JSON_DIALOG_TITLE,
                fontWeight = FontWeight.SemiBold,
                fontSize = ErdBoardDimens.DIALOG_TITLE_FONT_SP.sp,
                color = ErdBoardColors.textPrimary,
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                SecondaryTabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.Transparent,
                    contentColor = ErdBoardColors.textPrimary,
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text(ErdBoardStrings.JSON_TAB_VIEW_EXPORT, fontSize = ErdBoardDimens.DIALOG_BODY_FONT_SP.sp) },
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1; importError = null },
                        text = { Text(ErdBoardStrings.JSON_TAB_IMPORT, fontSize = ErdBoardDimens.DIALOG_BODY_FONT_SP.sp) },
                    )
                }

                Spacer(Modifier.height(ErdBoardDimens.DIALOG_SECTION_SPACING_XL_DP.dp))

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
                    Row(horizontalArrangement = Arrangement.spacedBy(ErdBoardDimens.DIALOG_BUTTONS_SPACING_DP.dp)) {
                        SaveJsonButton(filename = EXPORT_FILENAME, content = currentJson)
                        CopyJsonButton(content = currentJson)
                    }
                }
                1 -> {
                    TextButton(onClick = {
                        val trimmed = importText.trim()
                        when {
                            trimmed.isBlank() -> importError = ErdBoardStrings.JSON_IMPORT_ERROR_EMPTY
                            else -> onImport(trimmed)
                        }
                    }) {
                        Text(
                            ErdBoardStrings.JSON_IMPORT_BUTTON,
                            color = ErdBoardColors.textPrimary,
                            fontSize = ErdBoardDimens.DIALOG_BUTTON_FONT_SP.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    ErdBoardStrings.JSON_CLOSE,
                    color = ErdBoardColors.textDisabled,
                    fontSize = ErdBoardDimens.DIALOG_BUTTON_FONT_SP.sp,
                )
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
                .heightIn(
                    min = ErdBoardDimens.DIALOG_JSON_EXPORT_MIN_HEIGHT_DP.dp,
                    max = ErdBoardDimens.DIALOG_JSON_EXPORT_MAX_HEIGHT_DP.dp,
                )
                .border(
                    ErdBoardDimens.DIALOG_CODE_BORDER_WIDTH_DP.dp,
                    ErdBoardColors.divider,
                    RoundedCornerShape(ErdBoardDimens.DIALOG_CODE_CORNER_RADIUS_DP.dp),
                )
                .background(
                    ErdBoardColors.surfaceCodeBlock,
                    RoundedCornerShape(ErdBoardDimens.DIALOG_CODE_CORNER_RADIUS_DP.dp),
                )
                .verticalScroll(scrollV)
                .horizontalScroll(scrollH)
                .padding(ErdBoardDimens.DIALOG_CODE_PADDING_DP.dp),
        ) {
            Text(
                text = json,
                style = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontSize = ErdBoardDimens.DIALOG_CAPTION_FONT_SP.sp,
                    color = ErdBoardColors.textSecondary,
                    lineHeight = ErdBoardDimens.DIALOG_CODE_LINE_HEIGHT_SP.sp,
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
            ErdBoardStrings.JSON_IMPORT_HINT,
            fontSize = ErdBoardDimens.DIALOG_BODY_FONT_SP.sp,
            color = ErdBoardColors.textDisabled,
            modifier = Modifier.padding(bottom = ErdBoardDimens.DIALOG_SECTION_SPACING_LG_DP.dp),
        )
        OutlinedTextField(
            value = text,
            onValueChange = onTextChange,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(
                    min = ErdBoardDimens.DIALOG_JSON_IMPORT_MIN_HEIGHT_DP.dp,
                    max = ErdBoardDimens.DIALOG_JSON_IMPORT_MAX_HEIGHT_DP.dp,
                ),
            placeholder = {
                Text(
                    ErdBoardStrings.JSON_IMPORT_PLACEHOLDER,
                    color = ErdBoardColors.textGhost,
                    fontSize = ErdBoardDimens.DIALOG_CAPTION_FONT_SP.sp,
                )
            },
            textStyle = TextStyle(
                fontFamily = FontFamily.Monospace,
                fontSize = ErdBoardDimens.DIALOG_CAPTION_FONT_SP.sp,
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = if (error != null) ErdBoardColors.accentRed else ErdBoardColors.borderStrong,
                unfocusedBorderColor = if (error != null) ErdBoardColors.accentRed else ErdBoardColors.borderDefault,
                cursorColor = ErdBoardColors.borderStrong,
            ),
            isError = error != null,
        )
        if (error != null) {
            Text(
                error,
                fontSize = ErdBoardDimens.DIALOG_CAPTION_FONT_SP.sp,
                color = ErdBoardColors.accentRed,
                modifier = Modifier.padding(top = ErdBoardDimens.DIALOG_LABEL_BOTTOM_PADDING_DP.dp),
            )
        }
        Spacer(Modifier.height(ErdBoardDimens.DIALOG_LABEL_BOTTOM_PADDING_DP.dp))
        Text(
            ErdBoardStrings.JSON_IMPORT_WARNING,
            fontSize = ErdBoardDimens.DIALOG_CAPTION_FONT_SP.sp,
            color = ErdBoardColors.textPlaceholder,
        )
    }
}
