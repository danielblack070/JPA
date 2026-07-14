package com.ace.jp.app.ui.collection

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
import androidx.compose.ui.unit.sp
import com.ace.jp.app.data.model.Type
import com.ace.jp.app.data.model.Word
import com.ace.jp.app.ui.collection.dialogs.AddWordDialog
import com.ace.jp.app.ui.collection.dialogs.ConfirmationDialog
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import com.ace.jp.app.ui.AutoScalingText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WordListDetailScreen(
    viewModel: VocabularyViewModel,
    wordListId: Int,
    onBack: () -> Unit
) {
    LaunchedEffect(Unit) {
        viewModel.loadData()
    }

    val allWords by viewModel.words.collectAsState()
    val allWordLists by viewModel.wordLists.collectAsState()
    val targetList = allWordLists.firstOrNull { it.id == wordListId }
    val listName = targetList?.name ?: "Deleted list"

    val listWords = allWords.filter { it.wordListId == wordListId }

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilterOption by remember { mutableStateOf("All") }
    var filterExpanded by remember { mutableStateOf(false) }

    var selectedConfidenceLevels by remember { mutableStateOf(setOf(0, 1, 2, 3, 4, 5)) }
    var lastPracticedFilter by remember { mutableStateOf("Any") }
    var lastPracticedFilterExpanded by remember { mutableStateOf(false) }

    // Logic for Broad Type, Confidence, and Last Practiced Filters
    val filteredWords = listWords.filter { word ->
        val matchesSearch = word.japanese.contains(searchQuery, ignoreCase = true) ||
                (word.reading?.contains(searchQuery, ignoreCase = true) == true) ||
                word.english.contains(searchQuery, ignoreCase = true)

        val matchesFilter = when (selectedFilterOption) {
            "All" -> true
            "Verb" -> word.type == Type.UVerb || word.type == Type.RuVerb || word.type == Type.IrrVerb
            "Adjective" -> word.type == Type.IAdjective || word.type == Type.NaAdjective || word.type == Type.IrrAdjective
            else -> {
                val resolvedEnum = runCatching { Type.fromDisplayString(selectedFilterOption) }.getOrNull()
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

    var showAddWordDialog by remember { mutableStateOf(false) }
    var wordToEdit by remember { mutableStateOf<Word?>(null) }
    var wordToDelete by remember { mutableStateOf<Word?>(null) }

    var showDeleteAllDialog by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }
    var importTextData by remember { mutableStateOf("") }

    var isFilterVisible by remember { mutableStateOf(false) }

    var visibilityFilter by remember { mutableStateOf(WordVisibilityFilter()) }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Return back")
        }

        Text(
            text = "Word List: $listName",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Column(modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)) {
            Button(onClick = { isFilterVisible = !isFilterVisible }, Modifier.fillMaxWidth()) {
                Text(text = if (isFilterVisible) "Hide Searchbar and Filters" else "Show Searchbar and Filters")
            }

            // Conditional rendering: when false, the content doesn't even exist in the UI tree
            if (isFilterVisible) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Search Japanese, Reading, or English...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )

                // Drop-down for Type enum + broad verb & adjective filters
                Box(modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)) {
                    OutlinedButton(
                        onClick = { filterExpanded = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Type Filter: $selectedFilterOption")
                        Icon(Icons.Default.ArrowDropDown, "Drop selector")
                    }
                    DropdownMenu(
                        expanded = filterExpanded,
                        onDismissRequest = { filterExpanded = false },
                        modifier = Modifier.fillMaxWidth(0.9f)
                    ) {
                        DropdownMenuItem(
                            text = { Text("All") },
                            onClick = { selectedFilterOption = "All"; filterExpanded = false }
                        )
                        DropdownMenuItem(
                            text = { Text("Verb") },
                            onClick = { selectedFilterOption = "Verb"; filterExpanded = false }
                        )
                        DropdownMenuItem(
                            text = { Text("Adjective") },
                            onClick = { selectedFilterOption = "Adjective"; filterExpanded = false }
                        )
                        HorizontalDivider(modifier = Modifier, thickness = DividerDefaults.Thickness, color = DividerDefaults.color)
                        Type.entries.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option.toDisplayString()) },
                                onClick = { selectedFilterOption = option.toDisplayString(); filterExpanded = false }
                            )
                        }
                    }
                }

                // Confidence levels (column layout)
                Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
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
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val initialFontSize = 12.sp
                        var unifiedFontSize by remember { mutableStateOf(initialFontSize) }

                        (0..5).forEach { lvl ->
                            FilterChip(
                                // Crucial: weight(1f) forces all 6 chips to divide the screen width exactly equally
                                modifier = Modifier.weight(1f),
                                selected = lvl in selectedConfidenceLevels,
                                onClick = {
                                    selectedConfidenceLevels = if (lvl in selectedConfidenceLevels) selectedConfidenceLevels - lvl else selectedConfidenceLevels + lvl
                                },
                                label = {
                                    AutoScalingText(
                                        text = "${lvl * 20}%",
                                        modifier = Modifier.fillMaxWidth(),
                                        style = TextStyle(textAlign = TextAlign.Center),
                                        initialFontSize = initialFontSize,
                                        // Pass the currently decided unified size down
                                        currentUnifiedSize = unifiedFontSize,
                                        // When this specific chip shrinks, it reports its size back here
                                        onSizeShrunk = { determinedSize ->
                                            if (determinedSize < unifiedFontSize) {
                                                unifiedFontSize = determinedSize
                                            }
                                        }
                                    )
                                }
                            )
                        }
                    }
                }

                // Last Practiced Filter
                Box(modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)) {
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
                Column(modifier = Modifier.fillMaxSize()) {
                    Text("Property display toggles")
                    // The interactive selection UI at the top
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Chip for toggling Japanese
                        FilterChip(
                            modifier = Modifier.weight(0.3f),
                            selected = visibilityFilter.showJapanese,
                            onClick = {
                                val nextJapaneseValue = !visibilityFilter.showJapanese
                                // Ensure we don't turn off both Japanese and English simultaneously
                                val nextEnglishValue = if (!nextJapaneseValue && !visibilityFilter.showEnglish) true else visibilityFilter.showEnglish

                                // Re-assign a brand new copied instance to trigger recomposition
                                visibilityFilter = visibilityFilter.copy(
                                    showJapanese = nextJapaneseValue,
                                    showEnglish = nextEnglishValue
                                )
                            },
                            label = { Text("Japanese", textAlign = TextAlign.Center) }
                        )

                        // Chip for toggling English
                        FilterChip(
                            modifier = Modifier.weight(0.25f),
                            selected = visibilityFilter.showEnglish,
                            onClick = {
                                val nextEnglishValue = !visibilityFilter.showEnglish
                                val nextJapaneseValue = if (!visibilityFilter.showJapanese && !nextEnglishValue) true else visibilityFilter.showJapanese

                                visibilityFilter = visibilityFilter.copy(
                                    showJapanese = nextJapaneseValue,
                                    showEnglish = nextEnglishValue
                                )
                            },
                            label = { Text("English", textAlign = TextAlign.Center) }
                        )

                        // Chip for toggling Reading
                        FilterChip(
                            modifier = Modifier.weight(0.25f),
                            selected = visibilityFilter.showReading,
                            onClick = {
                                visibilityFilter = visibilityFilter.copy(showReading = !visibilityFilter.showReading)
                            },
                            label = { Text("Reading", textAlign = TextAlign.Center) }
                        )

                        // Chip for toggling Type
                        FilterChip(
                            modifier = Modifier.weight(0.2f),
                            selected = visibilityFilter.showType,
                            onClick = {
                                visibilityFilter = visibilityFilter.copy(showType = !visibilityFilter.showType)
                            },
                            label = { Text("Type", textAlign = TextAlign.Center) }
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Max)
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                onClick = { showAddWordDialog = true }
            ) { Text("Add Word", textAlign = TextAlign.Center) }

            Button(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                onClick = { showImportDialog = true }
            ) { Text("Import") }

            Button(
                modifier = Modifier
                    .weight(1.2f)
                    .fillMaxHeight(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                onClick = { showDeleteAllDialog = true }
            ) { Text("Delete All") }
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(filteredWords) { word ->
                WordItemRow(
                    word = word,
                    filter = visibilityFilter,
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
            existingWords = allWords,
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
            existingWords = allWords,
            editingWordId = w.id,
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
                        text = "Supports separated properties comma / semicolon parsing. Format:\nJapanese, Reading, English, Type, Notes",
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
            onConfirm = { viewModel.deleteWord(w); wordToDelete = null }
        )
    }

    // Delete all words
    if (showDeleteAllDialog) {
        ConfirmationDialog(
            title = "Delete All Words",
            text = "Confirm deleting all words inside '$listName'? This action is irreversible.",
            onDismiss = { showDeleteAllDialog = false },
            onConfirm = { viewModel.deleteWordsFromList(wordListId); showDeleteAllDialog = false }
        )
    }
}

data class WordVisibilityFilter(
    var showJapanese: Boolean = true,
    var showEnglish: Boolean = true,
    var showReading: Boolean = false,
    var showType: Boolean = false
)

@Composable
fun WordItemRow(
    word: Word,
    filter: WordVisibilityFilter,
    onToggle: (Boolean) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    val displayText = remember(word, filter) {
        buildList {
            if (filter.showJapanese) add(word.japanese)
            if (filter.showEnglish) add(word.english)
            if (filter.showReading && !word.reading.isNullOrBlank()) add(word.reading)
            if (filter.showType) add(word.type.toDisplayString())
        }.joinToString(" ")
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { isExpanded = !isExpanded }
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.height(IntrinsicSize.Max)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(displayText, style = MaterialTheme.typography.bodyMedium)
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
                        uncheckedTrackColor = Color(0xFFFCF4D2)
                    )
                )

                OutlinedButton(
                    onClick = onEdit,
                    contentPadding = PaddingValues(horizontal = 8.dp),
                    modifier = Modifier.fillMaxHeight()
                ) {
                    Text("Edit")
                }
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.fillMaxHeight()
                ) {
                    Text("✖", style = MaterialTheme.typography.bodyMedium)
                }
            }
            if (isExpanded) {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    thickness = DividerDefaults.Thickness,
                    color = DividerDefaults.color
                )

                if (!filter.showJapanese) {
                    Text("Japanese: ${word.japanese}", style = MaterialTheme.typography.bodyMedium)
                }

                if (!filter.showEnglish) {
                    Text("English: ${word.english}", style = MaterialTheme.typography.bodyMedium)
                }

                if (!word.reading.isNullOrBlank() && !filter.showReading) {
                    Text("Reading: ${word.reading}", style = MaterialTheme.typography.bodyMedium)
                }

                if (!filter.showType) {
                    Text("Type: ${word.type.toDisplayString()}", style = MaterialTheme.typography.bodyMedium)
                }

                Text(
                    text = "Notes: ${word.notes ?: "No notes available"}",
                    style = MaterialTheme.typography.bodyMedium
                )

                val formattedConfidencePct = word.confidence * 20
                val dateStr = if (word.lastPracticed == null) {
                    "Never"
                } else {
                    java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", LocalLocale.current.platformLocale).format(java.util.Date(word.lastPracticed))
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Confidence: $formattedConfidencePct% | Last Practiced: $dateStr",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
