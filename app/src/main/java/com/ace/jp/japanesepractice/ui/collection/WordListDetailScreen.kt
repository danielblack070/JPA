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
import com.ace.jp.japanesepractice.data.model.Type
import com.ace.jp.japanesepractice.data.model.Word
import com.ace.jp.japanesepractice.ui.collection.dialogs.AddWordDialog

@Composable
fun WordListDetailScreen(
    viewModel: CollectionViewModel,
    wordListId: Int,
    onBack: () -> Unit
) {
    val allWords by viewModel.words.collectAsState()
    val listWords = allWords.filter { it.wordListId == wordListId }
    
    var searchQuery by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf<Type?>(null) }
    
    val filteredWords = listWords.filter { 
        (it.japanese.contains(searchQuery, ignoreCase = true) || 
         (it.reading?.contains(searchQuery, ignoreCase = true) == true) || 
         it.english.contains(searchQuery, ignoreCase = true)) &&
        (selectedType == null || it.type == selectedType)
    }

    var showAddWordDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.padding(16.dp)) {
        Button(onClick = onBack) { Text("Back") }
        
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Search") },
            modifier = Modifier.fillMaxWidth()
        )
        
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { showAddWordDialog = true }) { Text("Add Word") }
            Button(onClick = { /* TODO: Implement Import */ }) { Text("Import") }
            Button(onClick = { /* TODO: Implement Delete All */ }) { Text("Delete All Words") }
        }
        
        LazyColumn {
            items(filteredWords) { word ->
                WordItem(word = word, onToggle = { /* TODO: viewModel.updateWord(word.copy(isEnabled = it)) */ }, onDelete = { /* TODO: Confirm Delete */ })
            }
        }
    }

    if (showAddWordDialog) {
        AddWordDialog(
            wordListId = wordListId,
            onDismiss = { showAddWordDialog = false },
            onConfirm = { japanese, reading, english, type, notes ->
                viewModel.addWord(wordListId, japanese, reading, english, type, notes)
                showAddWordDialog = false
            }
        )
    }
}

@Composable
fun WordItem(word: Word, onToggle: (Boolean) -> Unit, onDelete: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Card(modifier = Modifier.fillMaxWidth().padding(4.dp).clickable { expanded = !expanded }) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Japanese: ${word.japanese}")
                    Text("Reading: ${word.reading ?: ""}")
                    Text("English: ${word.english}")
                    Text("Type: ${word.type.name}")
                }
                Switch(checked = word.isEnabled, onCheckedChange = onToggle)
                IconButton(onClick = { /* TODO: Edit */ }) { Text("Edit") }
                IconButton(onClick = onDelete) { Text("Del") }
            }
            if (expanded) {
                Text("Notes: ${word.notes ?: "No notes"}", modifier = Modifier.padding(top = 8.dp))
            }
        }
    }
}
