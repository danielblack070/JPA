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
import com.ace.jp.japanesepractice.data.model.WordList

@Composable
fun WordListImportDialog(
    allWordLists: List<WordList>,
    onDismiss: () -> Unit,
    onConfirm: (Int, String) -> Unit
) {
    if (allWordLists.isEmpty()) {
        return // Safety guard handled by caller
    }

    var selectedListId by remember { mutableStateOf(allWordLists.first().id) }
    var textContent by remember { mutableStateOf("") }

    var expanded by remember { mutableStateOf(false) }
    var searchListQuery by remember { mutableStateOf("") }

    val isFormValid = textContent.trim().isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Import Vocabulary Words") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Select target Word list to import into:", style = MaterialTheme.typography.titleSmall)

                val currentListName = allWordLists.firstOrNull { it.id == selectedListId }?.name ?: "Select Droplist"

                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = currentListName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Word List") },
                        trailingIcon = { Icon(Icons.Default.ArrowDropDown, "Select Line") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expanded = true }
                    )

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.fillMaxWidth(0.9f)
                    ) {
                        OutlinedTextField(
                            value = searchListQuery,
                            onValueChange = { searchListQuery = it },
                            label = { Text("Search word lists...") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            singleLine = true
                        )

                        val filteredLists = allWordLists.filter {
                            it.name.contains(searchListQuery, ignoreCase = true)
                        }
                        filteredLists.forEach { wordList ->
                            DropdownMenuItem(
                                text = { Text(wordList.name) },
                                onClick = {
                                    selectedListId = wordList.id
                                    expanded = false
                                    searchListQuery = ""
                                }
                            )
                        }
                    }
                }

                Text(
                    text = "Separated fields supported: CSV (, and ; splitters) + txt. Line format:\nJapanese, Reading, English, Type, Notes",
                    style = MaterialTheme.typography.bodySmall
                )

                OutlinedTextField(
                    value = textContent,
                    onValueChange = { textContent = it },
                    label = { Text("Paste CSV or TXT text contents") },
                    minLines = 6,
                    maxLines = 10,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (isFormValid) onConfirm(selectedListId, textContent.trim()) },
                enabled = isFormValid
            ) { Text("Import") }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}