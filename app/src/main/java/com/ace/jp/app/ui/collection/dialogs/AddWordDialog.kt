package com.ace.jp.app.ui.collection.dialogs

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
import androidx.compose.ui.graphics.Color
import com.ace.jp.app.data.model.Type
import com.ace.jp.app.data.model.WordList

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddWordDialog(
    wordListId: Int = 0,
    allWordLists: List<WordList> = emptyList(),
    existingWords: List<com.ace.jp.app.data.model.Word> = emptyList(),
    editingWordId: Int? = null,
    initialJapanese: String = "",
    initialReading: String? = null,
    initialEnglish: String = "",
    initialType: Type = Type.Noun,
    initialNotes: String? = null,
    isEditMode: Boolean = false,
    onDismiss: () -> Unit,
    onConfirm: (String, String?, String, Type, String?, Int) -> Unit
) {
    var japanese by remember { mutableStateOf(initialJapanese) }
    var reading by remember { mutableStateOf(initialReading ?: "") }
    var english by remember { mutableStateOf(initialEnglish) }
    var selectedType by remember { mutableStateOf(initialType) }
    var notes by remember { mutableStateOf(initialNotes ?: "") }

    var targetListId by remember { mutableIntStateOf(wordListId) }

    var typeExpanded by remember { mutableStateOf(false) }
    var listExpanded by remember { mutableStateOf(false) }
    var searchListQuery by remember { mutableStateOf("") }

    val isFormValid = japanese.trim().isNotBlank() && english.trim().isNotBlank()

    val isDuplicate by remember(japanese, reading, english, selectedType, notes, targetListId) {
        derivedStateOf {
            val normalizedJapanese = japanese.trim().lowercase()
            val normalizedReading = reading.trim().lowercase().ifBlank { null }
            val normalizedEnglish = english.trim()
            val userEnglishMeanings = normalizedEnglish.split(",").map { it.trim().lowercase() }.filter { it.isNotEmpty() }

            existingWords.any { word ->
                // Skip comparing against the item currently being edited
                if (isEditMode && editingWordId != null && word.id == editingWordId) {
                    return@any false
                }

                val wordJapanese = word.japanese.trim().lowercase()
                val wordReading = word.reading?.trim()?.lowercase()
                val wordEnglishMeanings = word.english.split(",").map { it.trim().lowercase() }.filter { it.isNotEmpty() }

                val japaneseMatches = wordJapanese == normalizedJapanese
                val readingMatches = wordReading == null || normalizedReading == null || wordReading == normalizedReading
                val englishMatches = userEnglishMeanings.toSet() == wordEnglishMeanings.toSet()
                val typeMatches = word.type == selectedType

                japaneseMatches && readingMatches && englishMatches && typeMatches
            }
        }
    }

    val hasJapaneseWarning by remember(japanese) {
        derivedStateOf {
            val normalizedJapanese = japanese.trim().lowercase()
            if (normalizedJapanese.isEmpty()) false else {
                existingWords.any { word ->
                    if (isEditMode && editingWordId != null && word.id == editingWordId) {
                        false
                    } else {
                        word.japanese.trim().lowercase() == normalizedJapanese
                    }
                }
            }
        }
    }

    val hasReadingWarning by remember(reading) {
        derivedStateOf {
            val normalizedReading = reading.trim().lowercase()
            if (normalizedReading.isEmpty()) false else {
                existingWords.any { word ->
                    if (isEditMode && editingWordId != null && word.id == editingWordId) {
                        false
                    } else {
                        word.reading?.trim()?.lowercase() == normalizedReading
                    }
                }
            }
        }
    }

    val hasEnglishWarning by remember(english) {
        derivedStateOf {
            val userEnglishMeanings = english.trim().split(",").map { it.trim().lowercase() }.filter { it.isNotEmpty() }
            if (userEnglishMeanings.isEmpty()) false else {
                existingWords.any { word ->
                    if (isEditMode && editingWordId != null && word.id == editingWordId) {
                        false
                    } else {
                        val wordEnglishMeanings = word.english.split(",").map { it.trim().lowercase() }.filter { it.isNotEmpty() }
                        userEnglishMeanings.any { it in wordEnglishMeanings }
                    }
                }
            }
        }
    }

    val warningColor = Color(0xFFCA8A04)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isEditMode) "Edit Word" else "Add Word") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // 3. Error Banner
                if (isDuplicate) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "An identical word already exists in this list.",
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                } else if (hasJapaneseWarning || hasReadingWarning || hasEnglishWarning) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFFEF08A)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "A word with matching parameters already exists. Review yellow highlighted fields.",
                            color = Color(0xFF854D0E),
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                OutlinedTextField(
                    value = japanese,
                    onValueChange = { japanese = it },
                    label = { Text("Japanese (Mandatory)") },
                    singleLine = true,
                    isError = isDuplicate, // Highlights field in red if it's a duplicate
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (hasJapaneseWarning && !isDuplicate) warningColor else MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = if (hasJapaneseWarning && !isDuplicate) warningColor else MaterialTheme.colorScheme.outline,
                        focusedLabelColor = if (hasJapaneseWarning && !isDuplicate) warningColor else MaterialTheme.colorScheme.primary,
                        unfocusedLabelColor = if (hasJapaneseWarning && !isDuplicate) warningColor else MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = reading,
                    onValueChange = { reading = it },
                    label = { Text("Reading (Optional)") },
                    singleLine = true,
                    isError = isDuplicate,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (hasReadingWarning && !isDuplicate) warningColor else MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = if (hasReadingWarning && !isDuplicate) warningColor else MaterialTheme.colorScheme.outline,
                        focusedLabelColor = if (hasReadingWarning && !isDuplicate) warningColor else MaterialTheme.colorScheme.primary,
                        unfocusedLabelColor = if (hasReadingWarning && !isDuplicate) warningColor else MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = english,
                    onValueChange = { english = it },
                    label = { Text("English (Mandatory)") },
                    singleLine = true,
                    isError = isDuplicate,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (hasEnglishWarning && !isDuplicate) warningColor else MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = if (hasEnglishWarning && !isDuplicate) warningColor else MaterialTheme.colorScheme.outline,
                        focusedLabelColor = if (hasEnglishWarning && !isDuplicate) warningColor else MaterialTheme.colorScheme.primary,
                        unfocusedLabelColor = if (hasEnglishWarning && !isDuplicate) warningColor else MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Type drop-down
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = selectedType.toDisplayString(),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Word Type") },
                        trailingIcon = { Icon(Icons.Default.ArrowDropDown, "Select Type") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { typeExpanded = true },
                        enabled = false,
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = MaterialTheme.colorScheme.onSurface,
                            disabledBorderColor = if (isDuplicate) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline,
                            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    DropdownMenu(
                        expanded = typeExpanded,
                        onDismissRequest = { typeExpanded = false },
                        modifier = Modifier.fillMaxWidth(0.9f)
                    ) {
                        Type.entries.forEach { typeOption ->
                            DropdownMenuItem(
                                text = { Text(typeOption.toDisplayString()) },
                                onClick = {
                                    selectedType = typeOption
                                    typeExpanded = false
                                }
                            )
                        }
                    }
                }

                // If editing, allow moving to a different word list
                if (isEditMode && allWordLists.isNotEmpty()) {
                    Text("Location List", style = MaterialTheme.typography.titleSmall)

                    val currentTargetListName = allWordLists.firstOrNull { it.id == targetListId }?.name ?: "Unknown"

                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = currentTargetListName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Select Target Word List") },
                            trailingIcon = { Icon(Icons.Default.ArrowDropDown, "Select Line") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { listExpanded = true },
                            enabled = false,
                            colors = OutlinedTextFieldDefaults.colors(
                                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                disabledBorderColor = if (isDuplicate) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline,
                                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )

                        DropdownMenu(
                            expanded = listExpanded,
                            onDismissRequest = { listExpanded = false },
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
                                        targetListId = wordList.id
                                        listExpanded = false
                                        searchListQuery = ""
                                    }
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (Optional)") },
                    minLines = 1,
                    isError = isDuplicate,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (isFormValid && !isDuplicate) {
                        onConfirm(
                            japanese.trim(),
                            reading.trim().ifBlank { null },
                            english.trim(),
                            selectedType,
                            notes.trim().ifBlank { null },
                            targetListId
                        )
                    }
                },
                enabled = isFormValid && !isDuplicate
            ) { Text("Submit") }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}