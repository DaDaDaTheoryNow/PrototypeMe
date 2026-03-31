package com.dadadadev.prototype_me.erd.board.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dadadadev.prototype_me.domains.erd.design.api.domain.model.FieldType
import com.dadadadev.prototype_me.domains.erd.design.api.domain.model.ErdNodeField
import com.dadadadev.prototype_me.erd.board.ui.dimens.ErdBoardDimens
import com.dadadadev.prototype_me.erd.board.ui.theme.ErdBoardColors
import com.dadadadev.prototype_me.erd.board.ui.theme.ErdBoardStrings

@Composable
internal fun NodeDetailDialog(
    nodeName: String,
    fields: List<ErdNodeField>,
    onAddField: (name: String, type: FieldType) -> Unit,
    onRemoveField: (fieldId: String) -> Unit,
    onDismiss: () -> Unit,
) {
    var newFieldName by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(FieldType.TEXT) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ErdBoardColors.surfaceDialog,
        shape = RoundedCornerShape(ErdBoardDimens.DIALOG_CORNER_RADIUS_DP.dp),
        title = {
            Text(
                nodeName,
                fontWeight = FontWeight.SemiBold,
                fontSize = ErdBoardDimens.DIALOG_TITLE_FONT_SP.sp,
                color = ErdBoardColors.textPrimary,
            )
        },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                if (fields.isNotEmpty()) {
                    Text(
                        ErdBoardStrings.DETAIL_FIELDS_HEADER,
                        fontSize = ErdBoardDimens.DIALOG_CAPTION_FONT_SP.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = ErdBoardColors.textPlaceholder,
                        modifier = Modifier.padding(bottom = ErdBoardDimens.DIALOG_LABEL_BOTTOM_PADDING_DP.dp),
                    )
                    fields.forEach { field ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = ErdBoardDimens.DIALOG_ROW_VERTICAL_PADDING_DP.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                field.name,
                                fontSize = ErdBoardDimens.DIALOG_FIELD_NAME_FONT_SP.sp,
                                color = ErdBoardColors.textSecondary,
                                modifier = Modifier.weight(1f),
                            )
                            Text(
                                field.type.name.lowercase(),
                                fontSize = ErdBoardDimens.DIALOG_CAPTION_FONT_SP.sp,
                                color = ErdBoardColors.textPlaceholder,
                                modifier = Modifier.padding(horizontal = ErdBoardDimens.DIALOG_FIELD_TYPE_PADDING_H_DP.dp),
                            )
                            TextButton(onClick = { onRemoveField(field.id) }) {
                                Text(
                                    ErdBoardStrings.DETAIL_REMOVE_FIELD,
                                    color = ErdBoardColors.textGhost,
                                    fontSize = ErdBoardDimens.DIALOG_BODY_FONT_SP.sp,
                                )
                            }
                        }
                    }
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = ErdBoardDimens.DIALOG_SECTION_SPACING_LG_DP.dp),
                        color = ErdBoardColors.divider,
                    )
                }

                Text(
                    ErdBoardStrings.DETAIL_ADD_FIELD_HEADER,
                    fontSize = ErdBoardDimens.DIALOG_CAPTION_FONT_SP.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = ErdBoardColors.textPlaceholder,
                    modifier = Modifier.padding(bottom = ErdBoardDimens.DIALOG_SECTION_SPACING_MD_DP.dp),
                )
                OutlinedTextField(
                    value = newFieldName,
                    onValueChange = { newFieldName = it },
                    placeholder = { Text(ErdBoardStrings.DETAIL_FIELD_NAME_PLACEHOLDER, color = ErdBoardColors.textGhost) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ErdBoardColors.borderStrong,
                        unfocusedBorderColor = ErdBoardColors.borderDefault,
                        cursorColor = ErdBoardColors.borderStrong,
                    ),
                )
                Spacer(Modifier.height(ErdBoardDimens.DIALOG_SECTION_SPACING_LG_DP.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(ErdBoardDimens.DIALOG_SECTION_SPACING_MD_DP.dp)) {
                    FieldType.entries.forEach { type ->
                        val isSelected = selectedType == type
                        TextButton(
                            onClick = { selectedType = type },
                            modifier = Modifier.background(
                                if (isSelected) ErdBoardColors.borderStrong else ErdBoardColors.surfaceChipInactive,
                                RoundedCornerShape(ErdBoardDimens.DIALOG_CHIP_CORNER_RADIUS_DP.dp),
                            ),
                        ) {
                            Text(
                                type.name.lowercase(),
                                fontSize = ErdBoardDimens.DIALOG_CAPTION_FONT_SP.sp,
                                color = if (isSelected) ErdBoardColors.textOnAccent else ErdBoardColors.textMuted,
                            )
                        }
                    }
                }
                Spacer(Modifier.height(ErdBoardDimens.DIALOG_SECTION_SPACING_LG_DP.dp))
                TextButton(
                    onClick = {
                        if (newFieldName.isNotBlank()) {
                            onAddField(newFieldName.trim(), selectedType)
                            newFieldName = ""
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            ErdBoardColors.borderStrong,
                            RoundedCornerShape(ErdBoardDimens.DIALOG_BUTTON_CORNER_RADIUS_DP.dp),
                        ),
                ) {
                    Text(ErdBoardStrings.DETAIL_ADD_FIELD_BUTTON, color = ErdBoardColors.textOnAccent, fontWeight = FontWeight.SemiBold)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(ErdBoardStrings.DETAIL_DONE, color = ErdBoardColors.textPrimary, fontWeight = FontWeight.SemiBold)
            }
        },
    )
}

