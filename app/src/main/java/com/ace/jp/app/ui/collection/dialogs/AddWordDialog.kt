package com.ace.jp.app.ui.collection.dialogs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import com.ace.jp.app.data.model.Meaning
import com.ace.jp.app.data.model.Type
import com.ace.jp.app.data.model.WordList

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddWordDialog(
    wordListId: Int = 0,
    allWordLists: List<WordList> = emptyList(),
    existingWords: List<com.ace.jp.app.data.model.Word> = emptyList(),
    editingWordId: Int? = null,
    initialJapanese: List<Meaning> = listOf(Meaning(text = "")),
    initialEnglish: List<Meaning> = listOf(Meaning(text = "")),
    initialType: Type = Type.Noun,
    initialNotes: String? = null,
    isEditMode: Boolean = false,
    onDismiss: () -> Unit,
    onConfirm: (List<Meaning>, List<Meaning>, Type, String?, Int) -> Unit
) {
    var japaneseMeanings by remember { mutableStateOf(initialJapanese.ifEmpty { listOf(Meaning(text = "")) }) }
    var englishMeanings by remember { mutableStateOf(initialEnglish.ifEmpty { listOf(Meaning(text = "")) }) }
    var selectedType by remember { mutableStateOf(initialType) }
    var notes by remember { mutableStateOf(initialNotes ?: "") }

    var targetListId by remember { mutableIntStateOf(wordListId) }

    var typeExpanded by remember { mutableStateOf(false) }
    var listExpanded by remember { mutableStateOf(false) }
    var searchListQuery by remember { mutableStateOf("") }

    val isFormValid = japaneseMeanings.isNotEmpty() &&
            japaneseMeanings.all { it.text.trim().isNotBlank() } &&
            englishMeanings.isNotEmpty() &&
            englishMeanings.all { it.text.trim().isNotBlank() }

    val isDuplicate by remember(japaneseMeanings, englishMeanings, selectedType, notes, targetListId) {
        derivedStateOf {
            val currentJpTexts = japaneseMeanings.map { it.text.trim().lowercase() }.filter { it.isNotEmpty() }.toSet()
            val currentEngTexts = englishMeanings.map { it.text.trim().lowercase() }.filter { it.isNotEmpty() }.toSet()
            val currentJpPrs = japaneseMeanings.mapNotNull { it.pronunciation?.trim()?.lowercase()?.ifBlank { null } }.toSet()

            if (currentJpTexts.isEmpty() || currentEngTexts.isEmpty()) return@derivedStateOf false

            existingWords.any { word ->
                if (isEditMode && editingWordId != null && word.id == editingWordId) {
                    return@any false
                }

                val wordJpTexts = word.japanese.map { it.text.trim().lowercase() }.filter { it.isNotEmpty() }.toSet()
                val wordEngTexts = word.english.map { it.text.trim().lowercase() }.filter { it.isNotEmpty() }.toSet()
                val wordJpPrs = word.japanese.mapNotNull { it.pronunciation?.trim()?.lowercase()?.ifBlank { null } }.toSet()

                val japaneseMatches = wordJpTexts == currentJpTexts
                val pronMatches = wordJpPrs.isEmpty() || currentJpPrs.isEmpty() || wordJpPrs == currentJpPrs
                val englishMatches = wordEngTexts == currentEngTexts
                val typeMatches = word.type == selectedType

                japaneseMatches && pronMatches && englishMatches && typeMatches
            }
        }
    }

    val hasJapaneseWarning by remember(japaneseMeanings) {
        derivedStateOf {
            val currentJpTexts = japaneseMeanings.map { it.text.trim().lowercase() }.filter { it.isNotEmpty() }.toSet()
            if (currentJpTexts.isEmpty()) false else {
                existingWords.any { word ->
                    if (isEditMode && editingWordId != null && word.id == editingWordId) false
                    else word.japanese.any { it.text.trim().lowercase() in currentJpTexts }
                }
            }
        }
    }

    val hasPronunciationWarning by remember(japaneseMeanings, englishMeanings) {
        derivedStateOf {
            val currentPrs = (japaneseMeanings + englishMeanings)
                .mapNotNull { it.pronunciation?.trim()?.lowercase()?.ifBlank { null } }
                .toSet()
            if (currentPrs.isEmpty()) false else {
                existingWords.any { word ->
                    if (isEditMode && editingWordId != null && word.id == editingWordId) false
                    else (word.japanese + word.english).any { it.pronunciation?.trim()?.lowercase() in currentPrs }
                }
            }
        }
    }

    val hasEnglishWarning by remember(englishMeanings) {
        derivedStateOf {
            val currentEngTexts = englishMeanings.map { it.text.trim().lowercase() }.filter { it.isNotEmpty() }.toSet()
            if (currentEngTexts.isEmpty()) false else {
                existingWords.any { word ->
                    if (isEditMode && editingWordId != null && word.id == editingWordId) false
                    else word.english.any { it.text.trim().lowercase() in currentEngTexts }
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
                } else if (hasJapaneseWarning || hasPronunciationWarning || hasEnglishWarning) {
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

                // Japanese Meanings Section
                Text("Japanese Meanings", style = MaterialTheme.typography.titleSmall)
                japaneseMeanings.forEachIndexed { index, meaning ->
                    key(meaning.id) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Japanese #${index + 1}", style = MaterialTheme.typography.labelMedium)
                                    if (japaneseMeanings.size > 1) {
                                        IconButton(
                                            onClick = {
                                                japaneseMeanings = japaneseMeanings.filterNot { it.id == meaning.id }
                                            },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(Icons.Default.Delete, contentDescription = "Remove Japanese Meaning")
                                        }
                                    }
                                }
                                OutlinedTextField(
                                    value = meaning.text,
                                    onValueChange = { newText ->
                                        japaneseMeanings = japaneseMeanings.map { if (it.id == meaning.id) it.copy(text = newText) else it }
                                    },
                                    label = { Text("Text (Mandatory)") },
                                    singleLine = true,
                                    isError = isDuplicate,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = if (hasJapaneseWarning && !isDuplicate) warningColor else MaterialTheme.colorScheme.primary,
                                        unfocusedBorderColor = if (hasJapaneseWarning && !isDuplicate) warningColor else MaterialTheme.colorScheme.outline,
                                        focusedLabelColor = if (hasJapaneseWarning && !isDuplicate) warningColor else MaterialTheme.colorScheme.primary,
                                        unfocusedLabelColor = if (hasJapaneseWarning && !isDuplicate) warningColor else MaterialTheme.colorScheme.onSurfaceVariant
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )
                                OutlinedTextField(
                                    value = meaning.pronunciation ?: "",
                                    onValueChange = { newPron ->
                                        japaneseMeanings = japaneseMeanings.map { if (it.id == meaning.id) it.copy(pronunciation = newPron) else it }
                                    },
                                    label = { Text("Pronunciation (Optional)") },
                                    singleLine = true,
                                    isError = isDuplicate,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = if (hasPronunciationWarning && !isDuplicate) warningColor else MaterialTheme.colorScheme.primary,
                                        unfocusedBorderColor = if (hasPronunciationWarning && !isDuplicate) warningColor else MaterialTheme.colorScheme.outline,
                                        focusedLabelColor = if (hasPronunciationWarning && !isDuplicate) warningColor else MaterialTheme.colorScheme.primary,
                                        unfocusedLabelColor = if (hasPronunciationWarning && !isDuplicate) warningColor else MaterialTheme.colorScheme.onSurfaceVariant
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )
                                OutlinedTextField(
                                    value = meaning.sense ?: "",
                                    onValueChange = { newSense ->
                                        japaneseMeanings = japaneseMeanings.map { if (it.id == meaning.id) it.copy(sense = newSense) else it }
                                    },
                                    label = { Text("Sense (Optional)") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
                OutlinedButton(
                    onClick = { japaneseMeanings = japaneseMeanings + Meaning(text = "") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add Japanese Meaning")
                }

                // English Meanings Section
                Text("English Meanings", style = MaterialTheme.typography.titleSmall)
                englishMeanings.forEachIndexed { index, meaning ->
                    key(meaning.id) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("English #${index + 1}", style = MaterialTheme.typography.labelMedium)
                                    if (englishMeanings.size > 1) {
                                        IconButton(
                                            onClick = {
                                                englishMeanings = englishMeanings.filterNot { it.id == meaning.id }
                                            },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(Icons.Default.Delete, contentDescription = "Remove English Meaning")
                                        }
                                    }
                                }
                                OutlinedTextField(
                                    value = meaning.text,
                                    onValueChange = { newText ->
                                        englishMeanings = englishMeanings.map { if (it.id == meaning.id) it.copy(text = newText) else it }
                                    },
                                    label = { Text("Text (Mandatory)") },
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
                                OutlinedTextField(
                                    value = meaning.pronunciation ?: "",
                                    onValueChange = { newPron ->
                                        englishMeanings = englishMeanings.map { if (it.id == meaning.id) it.copy(pronunciation = newPron) else it }
                                    },
                                    label = { Text("Pronunciation (Optional)") },
                                    singleLine = true,
                                    isError = isDuplicate,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = if (hasPronunciationWarning && !isDuplicate) warningColor else MaterialTheme.colorScheme.primary,
                                        unfocusedBorderColor = if (hasPronunciationWarning && !isDuplicate) warningColor else MaterialTheme.colorScheme.outline,
                                        focusedLabelColor = if (hasPronunciationWarning && !isDuplicate) warningColor else MaterialTheme.colorScheme.primary,
                                        unfocusedLabelColor = if (hasPronunciationWarning && !isDuplicate) warningColor else MaterialTheme.colorScheme.onSurfaceVariant
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )
                                OutlinedTextField(
                                    value = meaning.sense ?: "",
                                    onValueChange = { newSense ->
                                        englishMeanings = englishMeanings.map { if (it.id == meaning.id) it.copy(sense = newSense) else it }
                                    },
                                    label = { Text("Sense (Optional)") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
                OutlinedButton(
                    onClick = { englishMeanings = englishMeanings + Meaning(text = "") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add English Meaning")
                }

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
                        val cleanedJapanese = japaneseMeanings.map {
                            it.copy(
                                text = it.text.trim(),
                                pronunciation = it.pronunciation?.trim()?.ifBlank { null },
                                sense = it.sense?.trim()?.ifBlank { null }
                            )
                        }
                        val cleanedEnglish = englishMeanings.map {
                            it.copy(
                                text = it.text.trim(),
                                pronunciation = it.pronunciation?.trim()?.ifBlank { null },
                                sense = it.sense?.trim()?.ifBlank { null }
                            )
                        }
                        onConfirm(
                            cleanedJapanese,
                            cleanedEnglish,
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