package com.ace.jp.japanesepractice.ui.collection

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ace.jp.japanesepractice.data.model.*
import com.ace.jp.japanesepractice.ui.collection.dialogs.*

@Composable
fun CollectionScreen(viewModel: CollectionViewModel) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var selectedWordListId by remember { mutableStateOf<Int?>(null) }

    if (selectedWordListId != null) {
        WordListDetailScreen(
            viewModel = viewModel,
            wordListId = selectedWordListId!!,
            onBack = { selectedWordListId = null }
        )
    } else {
        Column {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Vocabulary") })
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Conjugation") })
                Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }, text = { Text("Grammar") })
            }
            when (selectedTab) {
                0 -> VocabularyTab(viewModel, onListSelected = { selectedWordListId = it })
                1 -> ConjugationTab(viewModel)
                2 -> GrammarTabContent(viewModel)
            }
        }
    }
}

@Composable
fun VocabularyTab(viewModel: CollectionViewModel, onListSelected: (Int) -> Unit) {
    val subTab = remember { mutableIntStateOf(0) }
    val wordLists by viewModel.wordLists.collectAsState()
    val allWords by viewModel.words.collectAsState()
    
    var searchQuery by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf<Type?>(null) }
    var expandedType by remember { mutableStateOf(false) }

    val filteredWords = allWords.filter { 
        (it.japanese.contains(searchQuery, ignoreCase = true) || 
         (it.reading?.contains(searchQuery, ignoreCase = true) == true) || 
         it.english.contains(searchQuery, ignoreCase = true)) &&
        (selectedType == null || it.type == selectedType)
    }

    var showAddListDialog by remember { mutableStateOf(false) }
    var showDeleteAllDialog by remember { mutableStateOf(false) }

    Column {
        TabRow(selectedTabIndex = subTab.intValue) {
            Tab(selected = subTab.intValue == 0, onClick = { subTab.intValue = 0 }, text = { Text("Lists") })
            Tab(selected = subTab.intValue == 1, onClick = { subTab.intValue = 1 }, text = { Text("All") })
        }
        
        Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search") },
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Box {
                OutlinedButton(onClick = { expandedType = true }) {
                    Text(selectedType?.name ?: "All Types")
                }
                DropdownMenu(expanded = expandedType, onDismissRequest = { expandedType = false }) {
                    DropdownMenuItem(text = { Text("All Types") }, onClick = { selectedType = null; expandedType = false })
                    Type.entries.forEach { type ->
                        DropdownMenuItem(text = { Text(type.name) }, onClick = { selectedType = type; expandedType = false })
                    }
                }
            }
        }
        
        if (subTab.intValue == 0) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { showAddListDialog = true }) { Text("Add List") }
                    Button(onClick = {}) { Text("Import") }
                    Button(onClick = { showDeleteAllDialog = true }) { Text("Delete all") }
                }
                
                LazyColumn {
                    items(wordLists) { list ->
                        Text(
                            text = list.name, 
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                                .clickable { onListSelected(list.id) }
                        )
                    }
                }
            }
        } else {
            LazyColumn(modifier = Modifier.padding(16.dp)) {
                items(filteredWords) { word ->
                    Text(word.japanese, modifier = Modifier.padding(8.dp))
                }
            }
        }
    }

    if (showAddListDialog) {
        AddWordListDialog(
            onDismiss = { showAddListDialog = false },
            onConfirm = { name ->
                viewModel.addWordList(name)
                showAddListDialog = false
            }
        )
    }
    
    if (showDeleteAllDialog) {
        ConfirmationDialog(
            title = "Delete All Lists",
            text = "Are you sure you want to delete all word lists and their words?",
            onDismiss = { showDeleteAllDialog = false },
            onConfirm = { viewModel.deleteAllWordLists() }
        )
    }
}

@Composable
fun ConjugationTab(viewModel: CollectionViewModel) {
    val allRules by viewModel.masterRules.collectAsState()
    var showAddMasterRuleDialog by remember { mutableStateOf(false) }
    var showDeleteAllDialog by remember { mutableStateOf(false) }
    
    var searchQuery by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf<Type?>(null) }
    var expandedType by remember { mutableStateOf(false) }
    
    val filteredRules = allRules.filter {
        (it.name.contains(searchQuery, ignoreCase = true)) &&
        (selectedType == null || it.type == selectedType)
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { showAddMasterRuleDialog = true }) { Text("Add Master Rule") }
            Button(onClick = { showDeleteAllDialog = true }) { Text("Delete all rules") }
        }
        
        // Search and Filter UI
        Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search") },
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Box {
                OutlinedButton(onClick = { expandedType = true }) {
                    Text(selectedType?.name ?: "All Types")
                }
                DropdownMenu(expanded = expandedType, onDismissRequest = { expandedType = false }) {
                    DropdownMenuItem(text = { Text("All Types") }, onClick = { selectedType = null; expandedType = false })
                    Type.entries.forEach { type ->
                        DropdownMenuItem(text = { Text(type.name) }, onClick = { selectedType = type; expandedType = false })
                    }
                }
            }
        }

        LazyColumn {
            items(filteredRules) { rule ->
                Text(rule.name, modifier = Modifier.padding(8.dp))
            }
        }
    }

    if (showAddMasterRuleDialog) {
        AddMasterRuleDialog(
            onDismiss = { showAddMasterRuleDialog = false },
            onConfirm = { name, type ->
                viewModel.addMasterRule(name, type)
                showAddMasterRuleDialog = false
            }
        )
    }

    if (showDeleteAllDialog) {
        ConfirmationDialog(
            title = "Delete All Rules",
            text = "Are you sure you want to delete all master rules and their sub-rules?",
            onDismiss = { showDeleteAllDialog = false },
            onConfirm = { viewModel.deleteAllMasterRules() }
        )
    }
}

@Composable
fun GrammarTabContent(viewModel: CollectionViewModel) {
    val grammarRules by viewModel.grammarRules.collectAsState()
    var showAddGrammarRuleDialog by remember { mutableStateOf(false) }
    var showDeleteAllDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.padding(16.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { showAddGrammarRuleDialog = true }) { Text("Add Rule") }
            Button(onClick = { showDeleteAllDialog = true }) { Text("Delete all rules") }
        }
        LazyColumn {
            items(grammarRules) { rule ->
                Text(rule.description, modifier = Modifier.padding(8.dp))
            }
        }
    }

    if (showAddGrammarRuleDialog) {
        AddGrammarRuleDialog(
            onDismiss = { showAddGrammarRuleDialog = false },
            onConfirm = { description, example, objects ->
                viewModel.addGrammarRule(description, example, objects)
                showAddGrammarRuleDialog = false
            }
        )
    }

    if (showDeleteAllDialog) {
        ConfirmationDialog(
            title = "Delete All Rules",
            text = "Are you sure you want to delete all grammar rules?",
            onDismiss = { showDeleteAllDialog = false },
            onConfirm = { viewModel.deleteAllGrammarRules() }
        )
    }
}
