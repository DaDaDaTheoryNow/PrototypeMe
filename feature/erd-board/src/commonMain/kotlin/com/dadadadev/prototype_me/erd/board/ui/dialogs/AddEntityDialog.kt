package com.dadadadev.prototype_me.erd.board.ui.dialogs

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dadadadev.prototype_me.erd.board.ui.dimens.ErdBoardDimens
import com.dadadadev.prototype_me.erd.board.ui.theme.ErdBoardColors
import com.dadadadev.prototype_me.erd.board.ui.theme.ErdBoardStrings

@Composable
internal fun AddEntityDialog(
    onConfirm: (name: String) -> Unit,
    onDismiss: () -> Unit,
) {
    var name by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ErdBoardColors.surfaceDialog,
        shape = RoundedCornerShape(ErdBoardDimens.DIALOG_CORNER_RADIUS_DP.dp),
        title = {
            Text(ErdBoardStrings.DIALOG_NEW_ENTITY_TITLE, fontWeight = FontWeight.SemiBold, color = ErdBoardColors.textPrimary)
        },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                placeholder = { Text(ErdBoardStrings.DIALOG_ENTITY_NAME_PLACEHOLDER, color = ErdBoardColors.textPlaceholder) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ErdBoardColors.borderStrong,
                    unfocusedBorderColor = ErdBoardColors.borderDefault,
                    cursorColor = ErdBoardColors.borderStrong,
                ),
            )
        },
        confirmButton = {
            TextButton(onClick = {
                onConfirm(name.ifBlank { ErdBoardStrings.DIALOG_ENTITY_DEFAULT_NAME })
                name = ""
            }) {
                Text(ErdBoardStrings.DIALOG_ADD, color = ErdBoardColors.textPrimary, fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(ErdBoardStrings.DIALOG_CANCEL, color = ErdBoardColors.textDisabled)
            }
        },
    )
}
