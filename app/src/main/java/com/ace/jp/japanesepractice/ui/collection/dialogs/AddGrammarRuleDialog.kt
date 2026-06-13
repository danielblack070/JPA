package com.ace.jp.japanesepractice.ui.collection.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddGrammarRuleDialog(
    initialName: String = "",
    initialDescription: String = "",
    initialDetails: String = "",
    isEditMode: Boolean = false,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String) -> Unit
) {
    var name by remember {mutableStateOf(value = initialName)}
    var description by remember { mutableStateOf(initialDescription) }
    var details by remember { mutableStateOf(initialDetails) }

    val isFormValid = name.trim().isNotBlank() && description.trim().isNotBlank() && details.trim().isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isEditMode) "Edit Grammar Rule" else "Add Grammar Rule") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name (Mandatory)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (Mandatory)") },
                    minLines = 1,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = details,
                    onValueChange = { details = it },
                    label = { Text("Details (Mandatory)") },
                    minLines = 1,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (isFormValid) {
                        onConfirm(
                            name.trim(),
                            description.trim(),
                            details.trim()
                        )
                    }
                },
                enabled = isFormValid
            ) { Text("Submit") }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
