package com.ace.jp.japanesepractice.ui.collection.dialogs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ace.jp.japanesepractice.data.model.Type

@Composable
fun AddSubRuleDialog(
    initialDescription: String = "",
    initialOriginalEnding: String = "",
    initialNewEnding: String = "",
    initialType: Type = Type.Noun,
    initialIsUnique: Boolean = false,
    isEditMode: Boolean = false,
    onDismiss: () -> Unit,
    onConfirm: (String, Type, String, String, Boolean) -> Unit
) {
    var description by remember { mutableStateOf(initialDescription) }
    var originalEnding by remember { mutableStateOf(initialOriginalEnding) }
    var newEnding by remember { mutableStateOf(initialNewEnding) }
    var isUnique by remember { mutableStateOf(initialIsUnique) }
    var selectedType by remember { mutableStateOf(initialType) }

    var typeExpanded by remember { mutableStateOf(false) }

    val isFormValid = description.trim().isNotBlank() &&
            originalEnding.trim().isNotBlank() &&
            newEnding.trim().isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isEditMode) "Edit Subrule" else "Add Subrule") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = selectedType.toDisplayString(),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Type") },
                        trailingIcon = { Icon(Icons.Default.ArrowDropDown, "Select Type") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { typeExpanded = true }
                    )
                    DropdownMenu(
                        expanded = typeExpanded,
                        onDismissRequest = { typeExpanded = false },
                        modifier = Modifier.fillMaxWidth(0.9f)
                    ) {
                        Type.entries.filter { it != Type.Adverb && it != Type.Other }.forEach { t ->
                            DropdownMenuItem(
                                text = { Text(t.toDisplayString()) },
                                onClick = {
                                    selectedType = t
                                    typeExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = originalEnding,
                    onValueChange = { originalEnding = it },
                    label = { Text("Original Ending") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = newEnding,
                    onValueChange = { newEnding = it },
                    label = { Text("New Ending") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth().clickable { isUnique = !isUnique },
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Checkbox(checked = isUnique, onCheckedChange = { isUnique = it })
                    Text("Is Unique")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { if (isFormValid) onConfirm(description.trim(), selectedType, originalEnding.trim(), newEnding.trim(), isUnique) },
                enabled = isFormValid
            ) { Text("Submit") }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}