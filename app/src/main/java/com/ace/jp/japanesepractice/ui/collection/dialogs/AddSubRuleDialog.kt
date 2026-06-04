package com.ace.jp.japanesepractice.ui.collection.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.*
import androidx.compose.runtime.*

@Composable
fun AddSubRuleDialog(onDismiss: () -> Unit, onConfirm: (String, String, String, Boolean) -> Unit) {
    var description by remember { mutableStateOf("") }
    var original by remember { mutableStateOf("") }
    var new by remember { mutableStateOf("") }
    var isUnique by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Sub Rule") },
        text = {
            Column {
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") })
                OutlinedTextField(value = original, onValueChange = { original = it }, label = { Text("Original Ending") })
                OutlinedTextField(value = new, onValueChange = { new = it }, label = { Text("New Ending") })
                Checkbox(checked = isUnique, onCheckedChange = { isUnique = it })
            }
        },
        confirmButton = {
            TextButton(
                onClick = { if (description.isNotBlank() && original.isNotBlank() && new.isNotBlank()) onConfirm(description, original, new, isUnique) },
                enabled = description.isNotBlank() && original.isNotBlank() && new.isNotBlank()
            ) { Text("Add") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
