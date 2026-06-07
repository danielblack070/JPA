package com.ace.jp.japanesepractice.ui.collection

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.ace.jp.japanesepractice.data.model.*
import com.ace.jp.japanesepractice.ui.collection.dialogs.*
import com.ace.jp.japanesepractice.ui.practice.PracticeScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectionScreen(viewModel: CollectionViewModel) {
    // Top layout state (Main bottom active selection tracking)
    var mainActiveTabIndex by remember { mutableStateOf(0) } // 0 = Collection, 1 = Practice

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = mainActiveTabIndex == 0,
                    onClick = { mainActiveTabIndex = 0 },
                    icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Navigation List") },
                    label = { Text("Collection") }
                )
                NavigationBarItem(
                    selected = mainActiveTabIndex == 1,
                    onClick = { mainActiveTabIndex = 1 },
                    icon = { Icon(Icons.Default.PlayArrow, contentDescription = "Active Practice") },
                    label = { Text("Practice") }
                )
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            if (mainActiveTabIndex == 1) {
                PracticeScreen()
            } else {
                CollectionMainTabbedView(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun CollectionMainTabbedView(viewModel: CollectionViewModel) {
    var collectionActiveSubTabIndex by remember { mutableStateOf(0) } // 0 = Vocab, 1 = Conjugation, 2 = Grammar
    var selectedWordListId by remember { mutableStateOf<Int?>(null) }

    if (selectedWordListId != null) {
        WordListDetailScreen(
            viewModel = viewModel,
            wordListId = selectedWordListId!!,
            onBack = { selectedWordListId = null }
        )
    } else {
        Column(modifier = Modifier.fillMaxSize()) {
            TabRow(selectedTabIndex = collectionActiveSubTabIndex) {
                Tab(
                    selected = collectionActiveSubTabIndex == 0,
                    onClick = { collectionActiveSubTabIndex = 0 },
                    text = { Text("Vocabulary") }
                )
                Tab(
                    selected = collectionActiveSubTabIndex == 1,
                    onClick = { collectionActiveSubTabIndex = 1 },
                    text = { Text("Conjugation") }
                )
                Tab(
                    selected = collectionActiveSubTabIndex == 2,
                    onClick = { collectionActiveSubTabIndex = 2 },
                    text = { Text("Grammar") }
                )
            }

            when (collectionActiveSubTabIndex) {
                0 -> VocabularyTabContent(viewModel, onListSelected = { selectedWordListId = it })
                1 -> ConjugationTabContent(viewModel)
                2 -> GrammarTabContent(viewModel)
            }
        }
    }
}

// ===================== IMPLEMENT VOCABULARY SUB-TAB =====================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VocabularyTabContent(
    viewModel: CollectionViewModel,
    onListSelected: (Int) -> Unit
) {
    var vocabSubTabState by remember { mutableStateOf(0) } // 0 = Lists, 1 = All
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
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(modifier = Modifier.weight(1f), onClick = { showAddListDialog = true }) {
                        Text("Add List")
                    }
                    Button(
                        modifier = Modifier.weight(1f),
                        onClick = {
                            if (wordLists.isEmpty()) {
                                // show toast warning if empty
                            } else {
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
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                )

                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(filteredLists) { list ->
                        val wordCount = allWords.count { it.wordListId == list.id }
                        Card(
                            modifier = Modifier.fillMaxWidth().clickable { onListSelected(list.id) }
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
                                        uncheckedTrackColor = Color(0xFFFFCDD2)
                                    )
                                )

                                OutlinedButton(
                                    onClick = { listToEdit = list },
                                    contentPadding = PaddingValues(horizontal = 8.dp)
                                ) {
                                    Text("Edit")
                                }

                                IconButton(onClick = { listToDelete = list }) {
                                    Text("❌", style = MaterialTheme.typography.bodyMedium)
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
                    modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)
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
                            Modifier,
                            DividerDefaults.Thickness,
                            DividerDefaults.color
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

    // Add Overlay
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

    // Edit Overlay
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

    // Single deletion
    if (listToDelete != null) {
        val list = listToDelete!!
        ConfirmationDialog(
            title = "Delete Word List",
            text = "Are you sure you want to delete '${list.name}' and all words inside of it?",
            onDismiss = { listToDelete = null },
            onConfirm = { viewModel.deleteWordList(list) }
        )
    }

    // Delete All
    if (showDeleteAllListsDialog) {
        ConfirmationDialog(
            title = "Delete All Word Lists",
            text = "Are you sure you want to delete all Lists and their Words? This cannot be undone.",
            onDismiss = { showDeleteAllListsDialog = false },
            onConfirm = { viewModel.deleteAllWordLists() }
        )
    }

    // Searchable CSV imports
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

    // Edit Word on All tab
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

    // Delete word on All tab
    if (wordToDeleteAllTab != null) {
        val w = wordToDeleteAllTab!!
        ConfirmationDialog(
            title = "Delete Word",
            text = "Are you sure you want to delete '${w.japanese}'?",
            onDismiss = { wordToDeleteAllTab = null },
            onConfirm = { viewModel.deleteWord(w) }
        )
    }
}

// ===================== IMPLEMENT CONJUGATION TAB =====================
@Composable
fun ConjugationTabContent(viewModel: CollectionViewModel) {
    val masterRules by viewModel.masterRules.collectAsState()
    val subRules by viewModel.subRules.collectAsState()

    var ruleSearchQuery by remember { mutableStateOf("") }

    val filteredMasterRules = masterRules.filter {
        it.name.contains(ruleSearchQuery, ignoreCase = true)
    }

    var showAddMasterDialog by remember { mutableStateOf(false) }
    var masterRuleToEdit by remember { mutableStateOf<MasterRule?>(null) }
    var masterRuleToDelete by remember { mutableStateOf<MasterRule?>(null) }
    var showDeleteAllMasterRulesDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                modifier = Modifier.weight(1.5f),
                onClick = { showAddMasterDialog = true }
            ) { Text("Add Master Rule") }

            Button(
                modifier = Modifier.weight(1.2f),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                onClick = { showDeleteAllMasterRulesDialog = true }
            ) { Text("Delete All") }
        }

        OutlinedTextField(
            value = ruleSearchQuery,
            onValueChange = { ruleSearchQuery = it },
            label = { Text("Search Master Selection Rules...") },
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
        )

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(filteredMasterRules) { masterRule ->
                val nestedSubrules = subRules.filter { it.masterRuleId == masterRule.id }
                MasterRuleRow(
                    masterRule = masterRule,
                    subRules = nestedSubrules,
                    onToggleEnabled = { isEnabled ->
                        viewModel.updateMasterRule(masterRule.copy(isEnabled = isEnabled))
                    },
                    onEdit = { masterRuleToEdit = masterRule },
                    onDelete = { masterRuleToDelete = masterRule },
                    onAddSubRule = { desc, tp, orig, new, unique ->
                        viewModel.addSubRule(masterRule.id, desc, tp, orig, new, unique)
                    },
                    onDeleteAllSubrules = {
                        viewModel.deleteSubRulesForMasterRule(masterRule.id)
                    },
                    onToggleSubrule = { sub ->
                        viewModel.updateSubRule(sub.copy(isEnabled = !sub.isEnabled))
                    },
                    onUpdateSubrule = { sub, d, t, o, n, u ->
                        viewModel.updateSubRule(sub.copy(description = d, type = t, originalEnding = o, newEnding = n, isUnique = u))
                    },
                    onDeleteSubrule = { sub ->
                        viewModel.deleteSubRule(sub)
                    }
                )
            }
        }
    }

    // Add Overlay
    if (showAddMasterDialog) {
        AddMasterRuleDialog(
            existingRules = masterRules.map { it.name },
            onDismiss = { showAddMasterDialog = false },
            onConfirm = { name ->
                viewModel.addMasterRule(name)
                showAddMasterDialog = false
            }
        )
    }

    // Edit Overlay
    if (masterRuleToEdit != null) {
        val mr = masterRuleToEdit!!
        AddMasterRuleDialog(
            existingRules = masterRules.map { it.name },
            initialName = mr.name,
            onDismiss = { masterRuleToEdit = null },
            onConfirm = { name ->
                viewModel.updateMasterRule(mr.copy(name = name))
                masterRuleToEdit = null
            }
        )
    }

    // Single Deletion rule
    if (masterRuleToDelete != null) {
        val mr = masterRuleToDelete!!
        ConfirmationDialog(
            title = "Delete Master Rule",
            text = "Are you sure you want to delete '${mr.name}' and all subrules nested inside it?",
            onDismiss = { masterRuleToDelete = null },
            onConfirm = { viewModel.deleteMasterRule(mr) }
        )
    }

    // Deletall master rules
    if (showDeleteAllMasterRulesDialog) {
        ConfirmationDialog(
            title = "Delete All Master Rules",
            text = "Are you sure you want to delete all Rules and their subrules? This cannot be undone.",
            onDismiss = { showDeleteAllMasterRulesDialog = false },
            onConfirm = { viewModel.deleteAllMasterRules() }
        )
    }
}

@Composable
fun MasterRuleRow(
    masterRule: MasterRule,
    subRules: List<SubRule>,
    onToggleEnabled: (Boolean) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onAddSubRule: (String, Type, String, String, Boolean) -> Unit,
    onDeleteAllSubrules: () -> Unit,
    onToggleSubrule: (SubRule) -> Unit,
    onDeleteSubrule: (SubRule) -> Unit,
    onUpdateSubrule: (SubRule, String, Type, String, String, Boolean) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    var showAddSubruleDialog by remember { mutableStateOf(false) }
    var showDeleteAllSubrulesConfirm by remember { mutableStateOf(false) }

    var selectedSubruleDetails by remember { mutableStateOf<SubRule?>(null) }
    var subRuleToEdit by remember { mutableStateOf<SubRule?>(null) }
    var subRuleToDelete by remember { mutableStateOf<SubRule?>(null) }

    Card(
        modifier = Modifier.fillMaxWidth().clickable { isExpanded = !isExpanded }
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(masterRule.name, style = MaterialTheme.typography.titleMedium)
                    Text("Subrules: ${subRules.size}", style = MaterialTheme.typography.bodySmall)
                }

                // Green/Red styled toggle switch
                val toggThumbColor = if (masterRule.isEnabled) Color(0xFF4CAF50) else Color(0xFFF44336)
                Switch(
                    checked = masterRule.isEnabled,
                    onCheckedChange = onToggleEnabled,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = toggThumbColor,
                        uncheckedThumbColor = toggThumbColor,
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
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Button(
                        modifier = Modifier.weight(1f),
                        onClick = { showAddSubruleDialog = true }
                    ) { Text("+ Subrule") }

                    Button(
                        modifier = Modifier.weight(1.2f),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        onClick = { showDeleteAllSubrulesConfirm = true }
                    ) { Text("Clear All") }
                }

                if (subRules.isEmpty()) {
                    Text("No subrules defined under this conjugation.", style = MaterialTheme.typography.bodyMedium)
                } else {
                    subRules.forEach { subRule ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp).clickable { selectedSubruleDetails = subRule }
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(subRule.description, style = MaterialTheme.typography.bodyLarge)
                                    Text("Type: ${subRule.type.toDisplayString()}", style = MaterialTheme.typography.bodySmall)
                                }

                                // State toggler green/red
                                val thumbColor = if (subRule.isEnabled) Color(0xFF4CAF50) else Color(0xFFF44336)
                                Switch(
                                    checked = subRule.isEnabled,
                                    onCheckedChange = { onToggleSubrule(subRule) },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = thumbColor,
                                        uncheckedThumbColor = thumbColor,
                                        checkedTrackColor = Color(0xFFC0EFC1),
                                        uncheckedTrackColor = Color(0xFFFFCDD2)
                                    )
                                )

                                OutlinedButton(
                                    onClick = { subRuleToEdit = subRule },
                                    contentPadding = PaddingValues(horizontal = 4.dp)
                                ) {
                                    Text("Edit")
                                }

                                IconButton(onClick = { subRuleToDelete = subRule }) {
                                    Text("❌", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Add nested
    if (showAddSubruleDialog) {
        AddSubRuleDialog(
            isEditMode = false,
            onDismiss = { showAddSubruleDialog = false },
            onConfirm = { d, t, o, n, u ->
                onAddSubRule(d, t, o, n, u)
                showAddSubruleDialog = false
            }
        )
    }

    // Edit nested
    if (subRuleToEdit != null) {
        val s = subRuleToEdit!!
        AddSubRuleDialog(
            initialDescription = s.description,
            initialOriginalEnding = s.originalEnding,
            initialNewEnding = s.newEnding,
            initialType = s.type,
            initialIsUnique = s.isUnique,
            isEditMode = true,
            onDismiss = { subRuleToEdit = null },
            onConfirm = { d, t, o, n, u ->
                onUpdateSubrule(s, d, t, o, n, u)
                subRuleToEdit = null
            }
        )
    }

    // Delete nested single
    if (subRuleToDelete != null) {
        val s = subRuleToDelete!!
        ConfirmationDialog(
            title = "Delete Subrule",
            text = "Are you sure you want to delete '${s.description}'?",
            onDismiss = { subRuleToDelete = null },
            onConfirm = { onDeleteSubrule(s) }
        )
    }

    // Deletall nested subrules of rule
    if (showDeleteAllSubrulesConfirm) {
        ConfirmationDialog(
            title = "Clear All Subrules",
            text = "Delete all subrules inside '${masterRule.name}'?",
            onDismiss = { showDeleteAllSubrulesConfirm = false },
            onConfirm = onDeleteAllSubrules
        )
    }

    // Detailed popup cards of a single clickable Subrule
    if (selectedSubruleDetails != null) {
        val s = selectedSubruleDetails!!
        AlertDialog(
            onDismissRequest = { selectedSubruleDetails = null },
            title = { Text("Subrule: ${s.description}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Type Constraint: ${s.type.toDisplayString()}", style = MaterialTheme.typography.bodyLarge)
                    Text("Original Ending matching: ${s.originalEnding}", style = MaterialTheme.typography.bodyLarge)
                    Text("Replacement Ending mapped: ${s.newEnding}", style = MaterialTheme.typography.bodyLarge)
                    Text("Is Unique check: ${if (s.isUnique) "Yes" else "No"}", style = MaterialTheme.typography.bodyLarge)
                    Text("Status state: ${if (s.isEnabled) "Enabled" else "Disabled"}", style = MaterialTheme.typography.bodyLarge)
                }
            },
            confirmButton = {
                Button(onClick = { selectedSubruleDetails = null }) { Text("Close") }
            }
        )
    }
}