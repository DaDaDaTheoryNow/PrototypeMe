package com.dadadadev.prototype_me.board.ui.dialogs

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dadadadev.prototype_me.domain.models.FieldType
import com.dadadadev.prototype_me.domain.models.NodeField

@Composable
internal fun NodeDetailDialog(
    nodeName: String,
    fields: List<NodeField>,
    onAddField: (name: String, type: FieldType) -> Unit,
    onRemoveField: (fieldId: String) -> Unit,
    onDismiss: () -> Unit,
) {
    var newFieldName by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(FieldType.TEXT) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        shape = RoundedCornerShape(12.dp),
        title = {
            Text(
                nodeName,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                color = Color(0xFF111111),
            )
        },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                if (fields.isNotEmpty()) {
                    Text(
                        "Fields",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFAAAAAA),
                        modifier = Modifier.padding(bottom = 4.dp),
                    )
                    fields.forEach { field ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                field.name,
                                fontSize = 13.sp,
                                color = Color(0xFF333333),
                                modifier = Modifier.weight(1f),
                            )
                            Text(
                                field.type.name.lowercase(),
                                fontSize = 11.sp,
                                color = Color(0xFFAAAAAA),
                                modifier = Modifier.padding(horizontal = 8.dp),
                            )
                            TextButton(onClick = { onRemoveField(field.id) }) {
                                Text("x", color = Color(0xFFCCCCCC), fontSize = 12.sp)
                            }
                        }
                    }
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = Color(0xFFEEEEEE),
                    )
                }

                Text(
                    "Add field",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFFAAAAAA),
                    modifier = Modifier.padding(bottom = 6.dp),
                )
                OutlinedTextField(
                    value = newFieldName,
                    onValueChange = { newFieldName = it },
                    placeholder = { Text("Field name", color = Color(0xFFCCCCCC)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF111111),
                        unfocusedBorderColor = Color(0xFFDDDDDD),
                        cursorColor = Color(0xFF111111),
                    ),
                )
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FieldType.entries.forEach { type ->
                        val isSelected = selectedType == type
                        TextButton(
                            onClick = { selectedType = type },
                            modifier = Modifier.background(
                                if (isSelected) Color(0xFF111111) else Color(0xFFF0F0F0),
                                RoundedCornerShape(16.dp),
                            ),
                        ) {
                            Text(
                                type.name.lowercase(),
                                fontSize = 11.sp,
                                color = if (isSelected) Color.White else Color(0xFF555555),
                            )
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                TextButton(
                    onClick = {
                        if (newFieldName.isNotBlank()) {
                            onAddField(newFieldName.trim(), selectedType)
                            newFieldName = ""
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF111111), RoundedCornerShape(8.dp)),
                ) {
                    Text("Add field", color = Color.White, fontWeight = FontWeight.SemiBold)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Done", color = Color(0xFF111111), fontWeight = FontWeight.SemiBold)
            }
        },
    )
}
