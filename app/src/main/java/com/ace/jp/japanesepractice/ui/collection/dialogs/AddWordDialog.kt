package com.ace.jp.japanesepractice.ui.collection.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.ace.jp.japanesepractice.data.model.Type

@Composable
fun AddWordDialog(wordListId: Int, onDismiss: () -> Unit, onConfirm: (String, String?, String, Type, String?) -> Unit) {
    var japanese by remember { mutableStateOf("") }
    var reading by remember { mutableStateOf("") }
    var english by remember { mutableStateOf("") }
    var type by remember { mutableStateOf(Type.Noun) }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Word") },
        text = {
            Column {
                OutlinedTextField(value = japanese, onValueChange = { japanese = it }, label = { Text("Japanese") })
                OutlinedTextField(value = reading, onValueChange = { reading = it }, label = { Text("Reading (Optional)") })
                OutlinedTextField(value = english, onValueChange = { english = it }, label = { Text("English") })
                // Type drop-down would go here
                OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Notes (Optional)") })
            }
        },
        confirmButton = {
            TextButton(
                onClick = { if (japanese.isNotBlank() && english.isNotBlank()) onConfirm(japanese, reading.ifBlank { null }, english, type, notes.ifBlank { null }) },
                enabled = japanese.isNotBlank() && english.isNotBlank()
            ) { Text("Add") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
