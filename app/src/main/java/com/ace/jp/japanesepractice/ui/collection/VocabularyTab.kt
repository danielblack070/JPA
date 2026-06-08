package com.ace.jp.japanesepractice.ui.collection

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.ace.jp.japanesepractice.data.model.Type
import com.ace.jp.japanesepractice.data.model.Word
import com.ace.jp.japanesepractice.data.model.WordList
import com.ace.jp.japanesepractice.ui.collection.dialogs.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VocabularyTabContent(
    viewModel: VocabularyViewModel,
    onListSelected: (Int) -> Unit
) {
    var vocabSubTabState by remember { mutableStateOf(0) } // 0 = Lists, 1 = All Words
    val wordLists by viewModel.wordLists.collectAsState()
    val allWords by viewModel.words.collectAsState()

    var listSearchQuery by remember { mutableStateOf("") }
    var wordSearchQuery by remember { mutableStateOf("") }
    var selectedFilterQueryWordType by remember { mutableStateOf("All") }
    var typeFilterExpanded by remember { mutableStateOf(false) }

    val filteredLists = wordLists.filter {
        it.name.contains(listSearchQuery, ignoreCase = true)
    }

    val filteredAllWords = allWords.filter { word ->
        val matchesSearch = word.japanese.contains(wordSearchQuery, ignoreCase = true) ||
                (word.reading?.contains(wordSearchQuery, ignoreCase = true) == true) ||
                word.english.contains(wordSearchQuery, ignoreCase = true)

        val matchesFilter = when (selectedFilterQueryWordType) {
            "All" -> true
            "Verb (Broad)" -> word.type == Type.UVerb || word.type == Type.RuVerb || word.type == Type.IrrVerb
            "Adjective (Broad)" -> word.type == Type.IAdjective || word.type == Type.NaAdjective || word.type == Type.YoAdjective
            else -> {
                val resolvedEnum = runCatching { Type.fromDisplayString(selectedFilterQueryWordType) }.getOrNull()
                resolvedEnum == null || word.type == resolvedEnum
            }
        }
        matchesSearch && matchesFilter
    }

    var showAddListDialog by remember { mutableStateOf(false) }
    var listToEdit by remember { mutableStateOf<WordList?>(null) }
    var listToDelete by remember { mutableStateOf<WordList?>(null) }
    var showDeleteAllListsDialog by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }

    var wordToEditAllTab by remember { mutableStateOf<Word?>(null) }
    var wordToDeleteAllTab by remember { mutableStateOf<Word?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = vocabSubTabState) {
            Tab(selected = vocabSubTabState == 0, onClick = { vocabSubTabState = 0 }, text = { Text("Lists") })
            Tab(selected = vocabSubTabState == 1, onClick = { vocabSubTabState = 1 }, text = { Text("All Words") })
        }

        if (vocabSubTabState == 0) {
            // LISTS SECTION
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(modifier = Modifier.weight(1f), onClick = { showAddListDialog = true }) {
                        Text("Add List")
                    }
                    Button(
                        modifier = Modifier.weight(1f),
                        onClick = {
                            if (wordLists.isNotEmpty()) {
                                showImportDialog = true
                            }
                        }
                    ) {
                        Text("Import")
                    }
                    Button(
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        onClick = { showDeleteAllListsDialog = true }
                    ) {
                        Text("Delete All")
                    }
                }

                OutlinedTextField(
                    value = listSearchQuery,
                    onValueChange = { listSearchQuery = it },
                    label = { Text("Search word lists by name...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )

                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(filteredLists) { list ->
                        val wordCount = allWords.count { it.wordListId == list.id }
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onListSelected(list.id) }
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(list.name, style = MaterialTheme.typography.titleMedium)
                                    Text("Words inside: $wordCount", style = MaterialTheme.typography.bodySmall)
                                }

                                val toggleThumbnailColor = if (list.isEnabled) Color(0xFF4CAF50) else Color(0xFFF44336)
                                Switch(
                                    checked = list.isEnabled,
                                    onCheckedChange = { isEnabled ->
                                        viewModel.updateWordList(list.copy(isEnabled = isEnabled))
                                    },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = toggleThumbnailColor,
                                        uncheckedThumbColor = toggleThumbnailColor,
                                        checkedTrackColor = Color(0xFFC0EFC1),
                                        uncheckedTrackColor = Color(0xFFFCF4D2)
                                    )
                                )

                                OutlinedButton(
                                    onClick = { listToEdit = list },
                                    contentPadding = PaddingValues(horizontal = 8.dp)
                                ) {
                                    Text("Edit")
                                }

                                IconButton(onClick = { listToDelete = list }) {
                                    Text("✖", style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // ALL WORDS SECTION
            Column(modifier = Modifier.padding(16.dp)) {
                OutlinedTextField(
                    value = wordSearchQuery,
                    onValueChange = { wordSearchQuery = it },
                    label = { Text("Search japanese, reading, or english interpretation...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp)
                )

                Box(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                    OutlinedButton(
                        onClick = { typeFilterExpanded = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Filter: $selectedFilterQueryWordType")
                        Icon(Icons.Default.ArrowDropDown, "Select Filters")
                    }
                    DropdownMenu(
                        expanded = typeFilterExpanded,
                        onDismissRequest = { typeFilterExpanded = false },
                        modifier = Modifier.fillMaxWidth(0.9f)
                    ) {
                        DropdownMenuItem(
                            text = { Text("All") },
                            onClick = { selectedFilterQueryWordType = "All"; typeFilterExpanded = false }
                        )
                        DropdownMenuItem(
                            text = { Text("Verb") },
                            onClick = { selectedFilterQueryWordType = "Verb (Broad)"; typeFilterExpanded = false }
                        )
                        DropdownMenuItem(
                            text = { Text("Adjective") },
                            onClick = { selectedFilterQueryWordType = "Adjective (Broad)"; typeFilterExpanded = false }
                        )
                        HorizontalDivider(
                            modifier = Modifier,
                            thickness = DividerDefaults.Thickness,
                            color = DividerDefaults.color
                        )
                        Type.entries.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option.toDisplayString()) },
                                onClick = { selectedFilterQueryWordType = option.toDisplayString(); typeFilterExpanded = false }
                            )
                        }
                    }
                }

                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(filteredAllWords) { word ->
                        WordItemRow(
                            word = word,
                            onToggle = { isEnabled ->
                                viewModel.updateWord(word.copy(isEnabled = isEnabled))
                            },
                            onEdit = { wordToEditAllTab = word },
                            onDelete = { wordToDeleteAllTab = word }
                        )
                    }
                }
            }
        }
    }

    // Add List Overlay
    if (showAddListDialog) {
        AddWordListDialog(
            existingNames = wordLists.map { it.name },
            onDismiss = { showAddListDialog = false },
            onConfirm = { name ->
                viewModel.addWordList(name)
                showAddListDialog = false
            }
        )
    }

    // Edit List Overlay
    if (listToEdit != null) {
        val list = listToEdit!!
        AddWordListDialog(
            existingNames = wordLists.map { it.name },
            initialName = list.name,
            onDismiss = { listToEdit = null },
            onConfirm = { name ->
                viewModel.updateWordList(list.copy(name = name))
                listToEdit = null
            }
        )
    }

    // Single List Deletion Confirmation
    if (listToDelete != null) {
        val list = listToDelete!!
        ConfirmationDialog(
            title = "Delete Word List",
            text = "Are you sure you want to delete '${list.name}' and all words inside of it?",
            onDismiss = { listToDelete = null },
            onConfirm = { viewModel.deleteWordList(list); listToDelete = null }
        )
    }

    // Delete All lists Confirmation
    if (showDeleteAllListsDialog) {
        ConfirmationDialog(
            title = "Delete All Word Lists",
            text = "Are you sure you want to delete all Lists and their Words? This cannot be undone.",
            onDismiss = { showDeleteAllListsDialog = false },
            onConfirm = { viewModel.deleteAllWordLists(); showDeleteAllListsDialog = false }
        )
    }

    // Import dialouge
    if (showImportDialog) {
        WordListImportDialog(
            allWordLists = wordLists,
            onDismiss = { showImportDialog = false },
            onConfirm = { destId, csv ->
                viewModel.importWordsFromContent(destId, csv)
                showImportDialog = false
            }
        )
    }

    // Edit Word on All Tab
    if (wordToEditAllTab != null) {
        val w = wordToEditAllTab!!
        AddWordDialog(
            wordListId = w.wordListId,
            allWordLists = wordLists,
            initialJapanese = w.japanese,
            initialReading = w.reading,
            initialEnglish = w.english,
            initialType = w.type,
            initialNotes = w.notes,
            isEditMode = true,
            onDismiss = { wordToEditAllTab = null },
            onConfirm = { jp, rd, eng, tp, nt, destId ->
                viewModel.updateWord(
                    w.copy(
                        japanese = jp,
                        reading = rd,
                        english = eng,
                        type = tp,
                        notes = nt,
                        wordListId = destId
                    )
                )
                wordToEditAllTab = null
            }
        )
    }

    // Delete word on All Tab
    if (wordToDeleteAllTab != null) {
        val w = wordToDeleteAllTab!!
        ConfirmationDialog(
            title = "Delete Word",
            text = "Are you sure you want to delete '${w.japanese}'?",
            onDismiss = { wordToDeleteAllTab = null },
            onConfirm = { viewModel.deleteWord(w); wordToDeleteAllTab = null }
        )
    }
}