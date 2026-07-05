package com.ace.jp.app.ui.collection.dialogs

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
    initialEnglishRule: String = "",
    initialJapaneseRule: String = "",
    initialReadingRule: String = "",
    initialNotes: String = "",
    isEditMode: Boolean = false,
    onDismiss: () -> Unit,
    onConfirm: (
        name: String,
        englishRule: String,
        japaneseRule: String,
        readingRule: String?,
        notes: String?
    ) -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var englishRule by remember { mutableStateOf(initialEnglishRule) }
    var japaneseRule by remember { mutableStateOf(initialJapaneseRule) }
    var readingRule by remember { mutableStateOf(initialReadingRule) }
    var notes by remember { mutableStateOf(initialNotes) }

    val isFormValid = name.trim().isNotEmpty() &&
            englishRule.trim().isNotEmpty() &&
            japaneseRule.trim().isNotEmpty()

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
                    label = { Text("Name (e.g. ~てください) *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = japaneseRule,
                    onValueChange = { japaneseRule = it },
                    label = { Text("Japanese Rule *") },
                    minLines = 1,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = englishRule,
                    onValueChange = { englishRule = it },
                    label = { Text("English Rule *") },
                    minLines = 1,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = readingRule,
                    onValueChange = { readingRule = it },
                    label = { Text("Reading Rule (Optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (Optional)") },
                    minLines = 2,
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
                            englishRule.trim(),
                            japaneseRule.trim(),
                            readingRule.trim().takeIf { it.isNotEmpty() },
                            notes.trim().takeIf { it.isNotEmpty() }
                        )
                    }
                },
                enabled = isFormValid
            ) {
                Text("Submit")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
