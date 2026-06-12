package com.ace.jp.japanesepractice.ui.collection.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AddMasterRuleDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
    existingRules: List<String> = emptyList(),
    initialName: String = ""
) {
    var name by remember { mutableStateOf(initialName) }
    val isBlank = name.trim().isBlank()
    val isDuplicate = existingRules.any { it.equals(name.trim(), ignoreCase = true) && !it.equals(initialName.trim(), ignoreCase = true) }
    val isError = isBlank || isDuplicate

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialName.isEmpty()) "Add Conjugation Form" else "Edit Conjugation Form") },
        text = {
            Column(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Form Name") },
                    isError = isError,
                    singleLine = true,
                    supportingText = {
                        if (isDuplicate) {
                            Text("Name must be unique", color = MaterialTheme.colorScheme.error)
                        } else if (isBlank) {
                            Text("Name is mandatory", color = MaterialTheme.colorScheme.error)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (!isError) onConfirm(name.trim()) },
                enabled = !isError
            ) { Text("Submit") }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}