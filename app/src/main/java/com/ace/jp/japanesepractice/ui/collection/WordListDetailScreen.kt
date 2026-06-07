package com.ace.jp.japanesepractice.ui.collection

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.ace.jp.japanesepractice.data.model.Type
import com.ace.jp.japanesepractice.data.model.Word
import com.ace.jp.japanesepractice.ui.collection.dialogs.AddWordDialog
import com.ace.jp.japanesepractice.ui.collection.dialogs.ConfirmationDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WordListDetailScreen(
    viewModel: CollectionViewModel,
    wordListId: Int,
    onBack: () -> Unit
) {
    val allWords by viewModel.words.collectAsState()
    val allWordLists by viewModel.wordLists.collectAsState()
    val targetList = allWordLists.firstOrNull { it.id == wordListId }
    val listName = targetList?.name ?: "Deleted list"

    val listWords = allWords.filter { it.wordListId == wordListId }

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilterOption by remember { mutableStateOf("All Filter") }
    var filterExpanded by remember { mutableStateOf(false) }

    // Logic for Broad Type Filters
    val filteredWords = listWords.filter { word ->
        val matchesSearch = word.japanese.contains(searchQuery, ignoreCase = true) ||
                (word.reading?.contains(searchQuery, ignoreCase = true) == true) ||
                word.english.contains(searchQuery, ignoreCase = true)

        val matchesFilter = when (selectedFilterOption) {
            "All Filter" -> true
            "Verb (Broad)" -> word.type == Type.UVerb || word.type == Type.RuVerb || word.type == Type.IrrVerb
            "Adjective (Broad)" -> word.type == Type.IAdjective || word.type == Type.NaAdjective || word.type == Type.YoAdjective
            else -> {
                val resolvedEnum = runCatching { Type.fromDisplayString(selectedFilterOption) }.getOrNull()
                resolvedEnum == null || word.type == resolvedEnum
            }
        }
        matchesSearch && matchesFilter
    }

    var showAddWordDialog by remember { mutableStateOf(false) }
    var wordToEdit by remember { mutableStateOf<Word?>(null) }
    var wordToDelete by remember { mutableStateOf<Word?>(null) }

    var showDeleteAllDialog by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }
    var importTextData by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Return back")
        }

        Text(
            text = "Word List: $listName",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Search kanji, reading, or english...") },
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
        )

        // Drop-down for Type enum + broad verb & adjective filters
        Box(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
            OutlinedButton(
                onClick = { filterExpanded = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Filter: $selectedFilterOption")
                Icon(Icons.Default.ArrowDropDown, "Drop selector")
            }
            DropdownMenu(
                expanded = filterExpanded,
                onDismissRequest = { filterExpanded = false },
                modifier = Modifier.fillMaxWidth(0.9f)
            ) {
                DropdownMenuItem(
                    text = { Text("All Filters") },
                    onClick = { selectedFilterOption = "All Filter"; filterExpanded = false }
                )
                DropdownMenuItem(
                    text = { Text("Verb (Broad: U/Ru/Irr)") },
                    onClick = { selectedFilterOption = "Verb (Broad)"; filterExpanded = false }
                )
                DropdownMenuItem(
                    text = { Text("Adjective (Broad: I/Na/Yo)") },
                    onClick = { selectedFilterOption = "Adjective (Broad)"; filterExpanded = false }
                )
                HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)
                Type.entries.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option.toDisplayString()) },
                        onClick = { selectedFilterOption = option.toDisplayString(); filterExpanded = false }
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                modifier = Modifier.weight(1f),
                onClick = { showAddWordDialog = true }
            ) { Text("Add Word") }

            Button(
                modifier = Modifier.weight(1f),
                onClick = { showImportDialog = true }
            ) { Text("Import") }

            Button(
                modifier = Modifier.weight(1.2f),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                onClick = { showDeleteAllDialog = true }
            ) { Text("Delete All") }
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(filteredWords) { word ->
                WordItemRow(
                    word = word,
                    onToggle = { isEnabled ->
                        viewModel.updateWord(word.copy(isEnabled = isEnabled))
                    },
                    onEdit = { wordToEdit = word },
                    onDelete = { wordToDelete = word }
                )
            }
        }
    }

    // Add Overlay Dialog
    if (showAddWordDialog) {
        AddWordDialog(
            wordListId = wordListId,
            allWordLists = allWordLists,
            isEditMode = false,
            onDismiss = { showAddWordDialog = false },
            onConfirm = { jp, rd, eng, tp, nt, _ ->
                viewModel.addWord(wordListId, jp, rd, eng, tp, nt)
                showAddWordDialog = false
            }
        )
    }

    // Edit Overlay Dialog
    if (wordToEdit != null) {
        val w = wordToEdit!!
        AddWordDialog(
            wordListId = wordListId,
            allWordLists = allWordLists,
            initialJapanese = w.japanese,
            initialReading = w.reading,
            initialEnglish = w.english,
            initialType = w.type,
            initialNotes = w.notes,
            isEditMode = true,
            onDismiss = { wordToEdit = null },
            onConfirm = { jp, rd, eng, tp, nt, targetListId ->
                viewModel.updateWord(
                    w.copy(
                        japanese = jp,
                        reading = rd,
                        english = eng,
                        type = tp,
                        notes = nt,
                        wordListId = targetListId
                    )
                )
                wordToEdit = null
            }
        )
    }

    // Imports
    if (showImportDialog) {
        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            title = { Text("Import directly into list $listName") },
            text = {
                Column {
                    Text(
                        text = "Supports separated properties comma / semicolon parsing. Format:\nKanji, Reading, English, Type, Notes",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = importTextData,
                        onValueChange = { importTextData = it },
                        label = { Text("CSV/TXT lines") },
                        minLines = 4,
                        maxLines = 8,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (importTextData.isNotBlank()) {
                            viewModel.importWordsFromContent(wordListId, importTextData)
                            importTextData = ""
                            showImportDialog = false
                        }
                    },
                    enabled = importTextData.isNotBlank()
                ) { Text("Import") }
            },
            dismissButton = {
                OutlinedButton(onClick = { showImportDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Delete single word
    if (wordToDelete != null) {
        val w = wordToDelete!!
        ConfirmationDialog(
            title = "Delete Word",
            text = "Are you sure you want to delete '${w.japanese}'?",
            onDismiss = { wordToDelete = null },
            onConfirm = { viewModel.deleteWord(w) }
        )
    }

    // Delete all words
    if (showDeleteAllDialog) {
        ConfirmationDialog(
            title = "Delete All Words",
            text = "Confirm deleting all words inside '$listName'? This action is irreversible.",
            onDismiss = { showDeleteAllDialog = false },
            onConfirm = { viewModel.deleteWordsFromList(wordListId) }
        )
    }
}

@Composable
fun WordItemRow(
    word: Word,
    onToggle: (Boolean) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier.fillMaxWidth().clickable { isExpanded = !isExpanded }
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Japanese: ${word.japanese}", style = MaterialTheme.typography.bodyLarge)
                    if (!word.reading.isNullOrBlank()) {
                        Text("Reading: ${word.reading}", style = MaterialTheme.typography.bodyMedium)
                    }
                    Text("English: ${word.english}", style = MaterialTheme.typography.bodyMedium)
                    Text("Type: ${word.type.toDisplayString()}", style = MaterialTheme.typography.bodySmall)
                }

                // Green/Red styled Toggle switch
                val thumbColor = if (word.isEnabled) Color(0xFF4CAF50) else Color(0xFFF44336)
                Switch(
                    checked = word.isEnabled,
                    onCheckedChange = onToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = thumbColor,
                        uncheckedThumbColor = thumbColor,
                        checkedTrackColor = Color(0xFFC0EFC1),
                        uncheckedTrackColor = Color(0xFFFFCDD2)
                    )
                )

                OutlinedButton(onClick = onEdit, contentPadding = PaddingValues(horizontal = 8.dp)) {
                    Text("Edit")
                }
                IconButton(onClick = onDelete) {
                    Text("❌", style = MaterialTheme.typography.bodyMedium)
                }
            }
            if (isExpanded) {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    thickness = DividerDefaults.Thickness,
                    color = DividerDefaults.color
                )
                Text(
                    text = "Notes: ${word.notes ?: "No notes available"}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}