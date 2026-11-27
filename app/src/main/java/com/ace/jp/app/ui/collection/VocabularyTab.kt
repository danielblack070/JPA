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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.ace.jp.app.data.SettingsManager
import com.ace.jp.app.data.model.Folder
import com.ace.jp.app.data.model.Type
import com.ace.jp.app.data.model.Word
import com.ace.jp.app.data.model.WordList
import com.ace.jp.app.data.model.formatWithKatakanaFreeMode
import com.ace.jp.app.ui.AutoScalingText
import com.ace.jp.app.ui.collection.dialogs.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VocabularyTabContent(
    viewModel: VocabularyViewModel,
    settingsManager: SettingsManager,
    onListSelected: (Int) -> Unit
) {
    val appSettings by settingsManager.settings.collectAsState()
    LaunchedEffect(Unit) {
        viewModel.loadData()
    }

    var vocabSubTabState by remember { mutableIntStateOf(0) } // 0 = Lists, 1 = All Words
    val wordLists by viewModel.wordLists.collectAsState()
    val folders by viewModel.folders.collectAsState()
    val currentFolderId by viewModel.currentFolderId.collectAsState()
    val allWords by viewModel.words.collectAsState()

    var listSearchQuery by remember { mutableStateOf("") }
    var wordSearchQuery by remember { mutableStateOf("") }
    var selectedFilterQueryWordType by remember { mutableStateOf("All") }
    var typeFilterExpanded by remember { mutableStateOf(false) }

    var selectedConfidenceLevels by remember { mutableStateOf(setOf(0, 1, 2, 3, 4, 5)) }
    var lastPracticedFilter by remember { mutableStateOf("Any") }
    var lastPracticedFilterExpanded by remember { mutableStateOf(false) }

    val currentFolder = folders.firstOrNull { it.id == currentFolderId }
    val currentFolders = remember(folders, currentFolderId, listSearchQuery) {
        folders.filter {
            it.parentFolderId == currentFolderId &&
                    it.name.contains(listSearchQuery, ignoreCase = true)
        }
    }
    val currentWordLists = remember(wordLists, currentFolderId, listSearchQuery) {
        wordLists.filter {
            it.folderId == currentFolderId &&
                    it.name.contains(listSearchQuery, ignoreCase = true)
        }
    }
    val folderEntries = remember(currentFolders, currentWordLists) {
        (currentFolders.map { FolderEntry.FolderItem(it) } +
                currentWordLists.map { FolderEntry.WordListItem(it) })
            .sortedWith(compareBy<FolderEntry> { it.sortOrder }.thenBy { it !is FolderEntry.FolderItem }.thenBy { it.key })
    }

    val filteredAllWords = allWords.filter { word ->
        val matchesSearch = wordSearchQuery.isBlank() ||
                word.japanese.any {
                    it.text.contains(wordSearchQuery, ignoreCase = true) ||
                            (it.pronunciation?.contains(wordSearchQuery, ignoreCase = true) == true)
                } ||
                word.english.any {
                    it.text.contains(wordSearchQuery, ignoreCase = true) ||
                            (it.pronunciation?.contains(wordSearchQuery, ignoreCase = true) == true)
                }

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

    var showAddFolderDialog by remember { mutableStateOf(false) }
    var folderToEdit by remember { mutableStateOf<Folder?>(null) }
    var folderToDelete by remember { mutableStateOf<Folder?>(null) }

    var showAddListDialog by remember { mutableStateOf(false) }
    var listToEdit by remember { mutableStateOf<WordList?>(null) }
    var listToDelete by remember { mutableStateOf<WordList?>(null) }

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
                if (currentFolderId != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        IconButton(onClick = { viewModel.navigateUpFolder() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Up one folder")
                        }
                        Text(
                            text = "📁 ${currentFolder?.name ?: "Folder"}",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.weight(1f)
                        )
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
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        onClick = { showAddFolderDialog = true }
                    ) {
                        Text("Add Folder")
                    }
                    Button(
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        onClick = { showAddListDialog = true }
                    ) {
                        Text("Add List")
                    }
                }

                OutlinedTextField(
                    value = listSearchQuery,
                    onValueChange = { listSearchQuery = it },
                    label = { Text("Search folders or word lists...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )

                val isReorderEnabled = listSearchQuery.isBlank()
                val dragDropState = rememberDragDropListState(
                    externalItems = folderEntries,
                    keySelector = { it.key },
                    onCommitOrder = { reordered ->
                        viewModel.reorderFolderEntries(reordered)
                    }
                )

                LazyColumn(
                    state = dragDropState.lazyListState,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(dragDropState.items, key = { it.key }) { entry ->
                        val isDragging = dragDropState.draggedItemKey == entry.key
                        val cardModifier = Modifier
                            .fillMaxWidth()
                            .zIndex(if (isDragging) 1f else 0f)
                            .graphicsLayer {
                                translationY = if (isDragging) dragDropState.draggedOffsetY else 0f
                            }
                        val dragHandleModifier = if (isReorderEnabled) {
                            Modifier.pointerInput(entry.key) {
                                detectDragGestures(
                                    onDragStart = { dragDropState.onDragStart(entry.key) },
                                    onDrag = { change, dragAmount ->
                                        change.consume()
                                        dragDropState.onDrag(entry.key, dragAmount.y)
                                    },
                                    onDragEnd = { dragDropState.onDragEnd() },
                                    onDragCancel = { dragDropState.onDragEnd() }
                                )
                            }
                        } else {
                            Modifier
                        }

                        when (entry) {
                            is FolderEntry.FolderItem -> {
                                val folder = entry.folder
                                val subFolderCount = folders.count { it.parentFolderId == folder.id }
                                val subListCount = wordLists.count { it.folderId == folder.id }
                                Card(
                                    modifier = cardModifier.clickable { viewModel.openFolder(folder.id) }
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .padding(12.dp)
                                            .height(IntrinsicSize.Max),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        if (isReorderEnabled) {
                                            Icon(
                                                imageVector = Icons.Default.Menu,
                                                contentDescription = "Drag to reorder",
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = dragHandleModifier.padding(4.dp)
                                            )
                                        }
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("📁 ${folder.name}", style = MaterialTheme.typography.titleMedium)
                                            Text(
                                                "Folders: $subFolderCount | Lists: $subListCount",
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                        }
                                        OutlinedButton(
                                            onClick = { folderToEdit = folder },
                                            contentPadding = PaddingValues(horizontal = 8.dp),
                                            modifier = Modifier.fillMaxHeight()
                                        ) {
                                            Text("Edit")
                                        }
                                        IconButton(
                                            onClick = { folderToDelete = folder },
                                            modifier = Modifier.fillMaxHeight()
                                        ) {
                                            Text("✖", style = MaterialTheme.typography.bodyMedium)
                                        }
                                    }
                                }
                            }
                            is FolderEntry.WordListItem -> {
                                val list = entry.wordList
                                val wordCount = allWords.count { it.wordListId == list.id }
                                Card(
                                    modifier = cardModifier.clickable { onListSelected(list.id) }
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .padding(12.dp)
                                            .height(IntrinsicSize.Max),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        if (isReorderEnabled) {
                                            Icon(
                                                imageVector = Icons.Default.Menu,
                                                contentDescription = "Drag to reorder",
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = dragHandleModifier.padding(4.dp)
                                            )
                                        }
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
                        label = { Text("Search Japanese, Pronunciation, or English...") },
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
                    items(filteredAllWords, key = { it.id }) { word ->
                        WordItemRow(
                            word = word,
                            katakanaFreeMode = appSettings.katakanaFreeMode,
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

    // Add Folder Overlay
    if (showAddFolderDialog) {
        AddFolderDialog(
            onDismiss = { showAddFolderDialog = false },
            onConfirm = { name, targetParentFolderId ->
                viewModel.addFolder(name, targetParentFolderId)
                showAddFolderDialog = false
            },
            initialParentFolderId = currentFolderId,
            allFolders = folders,
            isEditMode = false
        )
    }

    // Edit Folder Overlay
    if (folderToEdit != null) {
        val folder = folderToEdit!!
        AddFolderDialog(
            onDismiss = { folderToEdit = null },
            onConfirm = { name, targetParentFolderId ->
                viewModel.updateFolder(folder.copy(name = name, parentFolderId = targetParentFolderId))
                folderToEdit = null
            },
            initialName = folder.name,
            initialParentFolderId = folder.parentFolderId,
            editingFolderId = folder.id,
            allFolders = folders,
            isEditMode = true
        )
    }

    // Single Folder Deletion Confirmation
    if (folderToDelete != null) {
        val folder = folderToDelete!!
        ConfirmationDialog(
            title = "Delete Folder",
            text = "Are you sure you want to delete '${folder.name}' and everything inside of it?",
            onDismiss = { folderToDelete = null },
            onConfirm = { viewModel.deleteFolder(folder); folderToDelete = null }
        )
    }

    // Add List Overlay
    if (showAddListDialog) {
        val siblingListNames = wordLists.filter { it.folderId == currentFolderId }.map { it.name }
        AddWordListDialog(
            onDismiss = { showAddListDialog = false },
            onConfirm = { name, targetFolderId ->
                viewModel.addWordList(name, targetFolderId)
                showAddListDialog = false
            },
            existingNames = siblingListNames,
            initialFolderId = currentFolderId,
            allFolders = folders,
            allWordLists = wordLists,
            isEditMode = false
        )
    }

    // Edit List Overlay
    if (listToEdit != null) {
        val list = listToEdit!!
        AddWordListDialog(
            onDismiss = { listToEdit = null },
            onConfirm = { name, targetFolderId ->
                viewModel.updateWordList(list.copy(name = name, folderId = targetFolderId))
                listToEdit = null
            },
            initialName = list.name,
            initialFolderId = list.folderId,
            editingListId = list.id,
            allFolders = folders,
            allWordLists = wordLists,
            isEditMode = true
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

    // Edit Word on All Tab
    if (wordToEditAllTab != null) {
        val w = wordToEditAllTab!!
        AddWordDialog(
            wordListId = w.wordListId,
            allWordLists = wordLists,
            allFolders = folders,
            existingWords = allWords,
            editingWordId = w.id,
            initialJapanese = w.japanese,
            initialEnglish = w.english,
            initialType = w.type,
            initialNotes = w.notes,
            isEditMode = true,
            onDismiss = { wordToEditAllTab = null },
            onConfirm = { jp, eng, tp, nt, destId ->
                viewModel.updateWord(
                    w.copy(
                        japanese = jp,
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
            text = "Are you sure you want to delete '${w.japanese.firstOrNull()?.text?.formatWithKatakanaFreeMode(appSettings.katakanaFreeMode) ?: ""}'?",
            onDismiss = { wordToDeleteAllTab = null },
            onConfirm = { viewModel.deleteWord(w); wordToDeleteAllTab = null }
        )
    }
}
