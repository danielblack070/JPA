package com.ace.jp.japanesepractice.ui.collection.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.*
import androidx.compose.runtime.*


@Composable
fun AddWordListDialog(onDismiss: () -> Unit, onConfirm: (String) -> Unit, existingNames: List<String> = emptyList()) {
    var name by remember { mutableStateOf("") }
    val isError = name.isBlank() || existingNames.contains(name)
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Word List") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    isError = isError,
                    supportingText = { if (isError) Text("Name must be unique and non-empty") }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { if (!isError) onConfirm(name) }, enabled = !isError) {
                Text("Add")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
