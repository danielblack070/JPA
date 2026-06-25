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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    LaunchedEffect(Unit) {
        viewModel.loadData()
    }

    var vocabSubTabState by remember { mutableIntStateOf(0) } // 0 = Lists, 1 = All Words
    val wordLists by viewModel.wordLists.collectAsState()
    val allWords by viewModel.words.collectAsState()

    var listSearchQuery by remember { mutableStateOf("") }
    var wordSearchQuery by remember { mutableStateOf("") }
    var selectedFilterQueryWordType by remember { mutableStateOf("All") }
    var typeFilterExpanded by remember { mutableStateOf(false) }

    var selectedConfidenceLevels by remember { mutableStateOf(setOf(0, 1, 2, 3, 4, 5)) }
    var lastPracticedFilter by remember { mutableStateOf("Any") }
    var lastPracticedFilterExpanded by remember { mutableStateOf(false) }

    val filteredLists = wordLists.filter {
        it.name.contains(listSearchQuery, ignoreCase = true)
    }

    val filteredAllWords = allWords.filter { word ->
        val matchesSearch = word.japanese.contains(wordSearchQuery, ignoreCase = true) ||
                (word.reading?.contains(wordSearchQuery, ignoreCase = true) == true) ||
                word.english.contains(wordSearchQuery, ignoreCase = true)

        val matchesFilter = when (selectedFilterQueryWordType) {
            "All" -> true
            "Verb" -> word.type == Type.UVerb || word.type == Type.RuVerb || word.type == Type.IrrVerb
            "Adjective" -> word.type == Type.IAdjective || word.type == Type.NaAdjective || word.type == Type.IrrAdjective
            else -> {
                val resolvedEnum = runCatching { Type.fromDisplayString(selectedFilterQueryWordType) }.getOrNull()
                resolvedEnum == null || word.type == resolvedEnum
            }
        }

        val matchesConf = word.confidence in selectedConfidenceLevels
        val matchesLastPracticed = when (lastPracticedFilter) {
            "Any" -> true
            "Never" -> word.lastPracticed == null
            else -> {
                val durationMs = when (lastPracticedFilter) {
                    "More than a day ago" -> 24 * 3600_000L
                    "More than a week ago" -> 7 * 24 * 3600_000L
                    "More than a month ago" -> 30 * 24 * 3600_000L
                    else -> 0L
                }
                val cutOffTime = System.currentTimeMillis() - durationMs
                word.lastPracticed != null && word.lastPracticed <= cutOffTime
            }
        }

        matchesSearch && matchesFilter && matchesConf && matchesLastPracticed
    }

    var showAddListDialog by remember { mutableStateOf(false) }
    var listToEdit by remember { mutableStateOf<WordList?>(null) }
    var listToDelete by remember { mutableStateOf<WordList?>(null) }
    var showDeleteAllListsDialog by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }

    var wordToEditAllTab by remember { mutableStateOf<Word?>(null) }
    var wordToDeleteAllTab by remember { mutableStateOf<Word?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        PrimaryTabRow(selectedTabIndex = vocabSubTabState) {
            Tab(selected = vocabSubTabState == 0, onClick = { vocabSubTabState = 0 }, text = { Text("Lists") })
            Tab(selected = vocabSubTabState == 1, onClick = { vocabSubTabState = 1 }, text = { Text("All Words") })
        }

        if (vocabSubTabState == 0) {
            // LISTS SECTION
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Max)
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        onClick = { showAddListDialog = true }
                    ) {
                        Text("Add List")
                    }
                    Button(
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        onClick = {
                            if (wordLists.isNotEmpty()) {
                                showImportDialog = true
                            }
                        }
                    ) {
                        Text("Import")
                    }
                    Button(
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        onClick = { showDeleteAllListsDialog = true }
                    ) {
                        Text("Delete All", textAlign = TextAlign.Center)
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
                                modifier = Modifier
                                    .padding(12.dp)
                                    .height(IntrinsicSize.Max),
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
                                    contentPadding = PaddingValues(horizontal = 8.dp),
                                    modifier = Modifier.fillMaxHeight()
                                ) {
                                    Text("Edit")
                                }

                                IconButton(
                                    onClick = { listToDelete = list },
                                    modifier = Modifier.fillMaxHeight()
                                ) {
                                    Text("✖", style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // ALL WORDS SECTION
            var isFilterVisible by remember { mutableStateOf(false) }

            Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp, horizontal = 16.dp)) {
                Button(onClick = { isFilterVisible = !isFilterVisible }, Modifier.fillMaxWidth()) {
                    Text(text = if (isFilterVisible) "Hide Searchbar and Filters" else "Show Searchbar and Filters")
                }

                // Conditional rendering: when false, the content doesn't even exist in the UI tree
                if (isFilterVisible) {
                    OutlinedTextField(
                        value = wordSearchQuery,
                        onValueChange = { wordSearchQuery = it },
                        label = { Text("Search Japanese, Reading, or English interpretation...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp)
                    )

                    // Type Option Selector
                    Box(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                        OutlinedButton(
                            onClick = { typeFilterExpanded = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Type: $selectedFilterQueryWordType")
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
                                onClick = { selectedFilterQueryWordType = "Verb"; typeFilterExpanded = false }
                            )
                            DropdownMenuItem(
                                text = { Text("Adjective") },
                                onClick = { selectedFilterQueryWordType = "Adjective"; typeFilterExpanded = false }
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

                    // Confidence levels (column layout)
                    Column(verticalArrangement = Arrangement.spacedBy(0.dp), modifier = Modifier.padding(bottom = 12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Selected Confidence Levels", style = MaterialTheme.typography.labelMedium)
                            Button(
                                onClick = {
                                    selectedConfidenceLevels = if (selectedConfidenceLevels.size == 6) emptySet() else setOf(0, 1, 2, 3, 4, 5)
                                },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                colors = ButtonDefaults.outlinedButtonColors()
                            ) {
                                Text(if (selectedConfidenceLevels.size == 6) "Clear All" else "Select All", fontSize = 10.sp)
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            (0..5).forEach { lvl ->
                                FilterChip(
                                    selected = lvl in selectedConfidenceLevels,
                                    onClick = {
                                        selectedConfidenceLevels = if (lvl in selectedConfidenceLevels) selectedConfidenceLevels - lvl else selectedConfidenceLevels + lvl
                                    },
                                    label = { Text("${lvl * 20}%", fontSize = 9.sp) }
                                )
                            }
                        }
                    }

                    // Last Practiced Filter
                    Box(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                        OutlinedButton(
                            onClick = { lastPracticedFilterExpanded = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Last Practiced Filter: $lastPracticedFilter")
                            Icon(Icons.Default.ArrowDropDown, "Select Period")
                        }
                        DropdownMenu(
                            expanded = lastPracticedFilterExpanded,
                            onDismissRequest = { lastPracticedFilterExpanded = false },
                            modifier = Modifier.fillMaxWidth(0.9f)
                        ) {
                            listOf(
                                "Any", "More than a day ago", "More than a week ago",
                                "More than a month ago", "Never practiced"
                            ).forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option) },
                                    onClick = {
                                        lastPracticedFilter = option
                                        lastPracticedFilterExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
            Column(modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp)) {
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

    // Import dialog
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
