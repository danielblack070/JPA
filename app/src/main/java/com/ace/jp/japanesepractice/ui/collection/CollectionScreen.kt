package com.ace.jp.japanesepractice.ui.collection

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.ace.jp.japanesepractice.ui.practice.PracticeScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectionScreen(
    vocabularyViewModel: VocabularyViewModel,
    conjugationViewModel: ConjugationViewModel,
    grammarViewModel: GrammarViewModel
) {
    // Top layout state (Main bottom active selection tracking)
    var mainActiveTabIndex by remember { mutableIntStateOf(0) } // 0 = Collection, 1 = Practice

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
                CollectionMainTabbedView(
                    vocabularyViewModel = vocabularyViewModel,
                    conjugationViewModel = conjugationViewModel,
                    grammarViewModel = grammarViewModel
                )
            }
        }
    }
}

@Composable
fun CollectionMainTabbedView(
    vocabularyViewModel: VocabularyViewModel,
    conjugationViewModel: ConjugationViewModel,
    grammarViewModel: GrammarViewModel
) {
    var collectionActiveSubTabIndex by remember { mutableIntStateOf(0) } // 0 = Vocab, 1 = Conjugation, 2 = Grammar
    var selectedWordListId by remember { mutableStateOf<Int?>(null) }

    if (selectedWordListId != null) {
        WordListDetailScreen(
            viewModel = vocabularyViewModel,
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
                0 -> VocabularyTabContent(vocabularyViewModel, onListSelected = { selectedWordListId = it })
                1 -> ConjugationTabContent(conjugationViewModel)
                2 -> GrammarTabContent(grammarViewModel)
            }
        }
    }
}