package com.ace.jp.app.ui.collection

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.ace.jp.app.data.SettingsManager
import com.ace.jp.app.data.model.Type
import com.ace.jp.app.data.model.Word
import com.ace.jp.app.data.model.formatDisplay
import com.ace.jp.app.data.model.formatWithKatakanaFreeMode
import com.ace.jp.app.ui.AutoScalingText
import com.ace.jp.app.ui.collection.dialogs.AddWordDialog
import com.ace.jp.app.ui.collection.dialogs.ConfirmationDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WordListDetailScreen(
    viewModel: VocabularyViewModel,
    wordListId: Int,
    settingsManager: SettingsManager,
    onBack: () -> Unit
) {
    val appSettings by settingsManager.settings.collectAsState()
    LaunchedEffect(Unit) {
        viewModel.loadData()
    }

    val allWords by viewModel.words.collectAsState()
    val allWordLists by viewModel.wordLists.collectAsState()
    val allFolders by viewModel.folders.collectAsState()
    val targetList = allWordLists.firstOrNull { it.id == wordListId }
    val listName = targetList?.name ?: "Deleted list"

    val listWords = remember(allWords, wordListId) {
        allWords.filter { it.wordListId == wordListId }
            .sortedWith(compareBy<Word> { it.sortOrder }.thenBy { it.id })
    }

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilterOption by remember { mutableStateOf("All") }
    var filterExpanded by remember { mutableStateOf(false) }

    var selectedConfidenceLevels by remember { mutableStateOf(setOf(0, 1, 2, 3, 4, 5)) }
    var lastPracticedFilter by remember { mutableStateOf("Any") }
    var lastPracticedFilterExpanded by remember { mutableStateOf(false) }

    // Logic for Broad Type, Confidence, and Last Practiced Filters
    val filteredWords = listWords.filter { word ->
        val matchesSearch = searchQuery.isBlank() ||
                word.japanese.any {
                    it.text.contains(searchQuery, ignoreCase = true) ||
                            (it.pronunciation?.contains(searchQuery, ignoreCase = true) == true)
                } ||
                word.english.any {
                    it.text.contains(searchQuery, ignoreCase = true) ||
                            (it.pronunciation?.contains(searchQuery, ignoreCase = true) == true)
                }

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

    var isFilterVisible by remember { mutableStateOf(false) }

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

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        ) {
            Button(onClick = { isFilterVisible = !isFilterVisible }, Modifier.fillMaxWidth()) {
                Text(text = if (isFilterVisible) "Hide Searchbar and Filters" else "Show Searchbar and Filters")
            }

            // Conditional rendering: when false, the content doesn't even exist in the UI tree
            if (isFilterVisible) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Search Japanese, Pronunciation, or English...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )

                // Drop-down for Type enum + broad verb & adjective filters
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                ) {
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
                        HorizontalDivider(
                            modifier = Modifier,
                            thickness = DividerDefaults.Thickness,
                            color = DividerDefaults.color
                        )
                        Type.entries.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option.toDisplayString()) },
                                onClick = {
                                    selectedFilterOption =
                                        option.toDisplayString(); filterExpanded = false
                                }
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
                        Text(
                            "Selected Confidence Levels",
                            style = MaterialTheme.typography.labelMedium
                        )
                        Button(
                            onClick = {
                                selectedConfidenceLevels =
                                    if (selectedConfidenceLevels.size == 6) emptySet() else setOf(
                                        0,
                                        1,
                                        2,
                                        3,
                                        4,
                                        5
                                    )
                            },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            colors = ButtonDefaults.outlinedButtonColors()
                        ) {
                            Text(
                                if (selectedConfidenceLevels.size == 6) "Clear All" else "Select All",
                                fontSize = 10.sp
                            )
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
                                    selectedConfidenceLevels =
                                        if (lvl in selectedConfidenceLevels) selectedConfidenceLevels - lvl else selectedConfidenceLevels + lvl
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
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
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

        Button(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            onClick = { showAddWordDialog = true }
        ) { Text("Add Word", textAlign = TextAlign.Center) }

        val isReorderEnabled = searchQuery.isBlank() &&
                selectedFilterOption == "All" &&
                selectedConfidenceLevels.size == 6 &&
                lastPracticedFilter == "Any"

        val dragDropState = rememberDragDropListState(
            externalItems = filteredWords,
            keySelector = { it.id },
            onCommitOrder = { reordered ->
                viewModel.reorderWords(reordered)
            }
        )

        LazyColumn(
            state = dragDropState.lazyListState,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(dragDropState.items, key = { it.id }) { word ->
                val isDragging = dragDropState.draggedItemKey == word.id
                val cardModifier = Modifier
                    .fillMaxWidth()
                    .zIndex(if (isDragging) 1f else 0f)
                    .graphicsLayer {
                        translationY = if (isDragging) dragDropState.draggedOffsetY else 0f
                    }
                val dragHandleModifier = if (isReorderEnabled) {
                    Modifier.pointerInput(word.id) {
                        detectDragGestures(
                            onDragStart = { dragDropState.onDragStart(word.id) },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                dragDropState.onDrag(word.id, dragAmount.y)
                            },
                            onDragEnd = { dragDropState.onDragEnd() },
                            onDragCancel = { dragDropState.onDragEnd() }
                        )
                    }
                } else {
                    null
                }

                WordItemRow(
                    word = word,
                    onToggle = { isEnabled ->
                        viewModel.updateWord(word.copy(isEnabled = isEnabled))
                    },
                    onEdit = { wordToEdit = word },
                    onDelete = { wordToDelete = word },
                    modifier = cardModifier,
                    katakanaFreeMode = appSettings.katakanaFreeMode,
                    dragHandleModifier = dragHandleModifier
                )
            }
        }
    }

    // Add Overlay Dialog
    if (showAddWordDialog) {
        AddWordDialog(
            wordListId = wordListId,
            allWordLists = allWordLists,
            allFolders = allFolders,
            existingWords = allWords,
            isEditMode = false,
            onDismiss = { showAddWordDialog = false },
            onConfirm = { jp, eng, tp, nt, _ ->
                viewModel.addWord(wordListId, jp, eng, tp, nt)
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
            allFolders = allFolders,
            existingWords = allWords,
            editingWordId = w.id,
            initialJapanese = w.japanese,
            initialEnglish = w.english,
            initialType = w.type,
            initialNotes = w.notes,
            isEditMode = true,
            onDismiss = { wordToEdit = null },
            onConfirm = { jp, eng, tp, nt, targetListId ->
                viewModel.updateWord(
                    w.copy(
                        japanese = jp,
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

    // Delete single word
    if (wordToDelete != null) {
        val w = wordToDelete!!
        ConfirmationDialog(
            title = "Delete Word",
            text = "Are you sure you want to delete '${w.japanese.firstOrNull()?.text?.formatWithKatakanaFreeMode(appSettings.katakanaFreeMode) ?: ""}'?",
            onDismiss = { wordToDelete = null },
            onConfirm = { viewModel.deleteWord(w); wordToDelete = null }
        )
    }

}

@Composable
fun WordItemRow(
    word: Word,
    onToggle: (Boolean) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
    katakanaFreeMode: Boolean = false,
    dragHandleModifier: Modifier? = null
) {
    var isExpanded by remember { mutableStateOf(false) }
    val displayText = remember(word, katakanaFreeMode) {
        val firstJapanese = word.japanese.firstOrNull()?.text?.formatWithKatakanaFreeMode(katakanaFreeMode).orEmpty()
        val firstEnglish = word.english.firstOrNull()?.text.orEmpty()
        listOf(firstJapanese, firstEnglish).filter { it.isNotBlank() }.joinToString(" | ")
    }

    Card(
        modifier = modifier.clickable { isExpanded = !isExpanded }
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.height(IntrinsicSize.Max)
            ) {
                if (dragHandleModifier != null) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Drag to reorder",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = dragHandleModifier.padding(4.dp)
                    )
                }

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

                Text("Japanese:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                word.japanese.forEach { meaning ->
                    Text(
                        text = "• ${meaning.formatDisplay(showPronunciation = true, katakanaFreeMode = katakanaFreeMode)}",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text("English:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                word.english.forEach { meaning ->
                    Text(
                        text = "• ${meaning.formatDisplay(showPronunciation = true, katakanaFreeMode = katakanaFreeMode)}",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text("Type: ${word.type.toDisplayString()}", style = MaterialTheme.typography.bodyMedium)

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
