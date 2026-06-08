package com.ace.jp.japanesepractice.ui.practice

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

@Composable
fun PracticeScreen(viewModel: PracticeViewModel) {
    var activeSubTabIndex by remember { mutableIntStateOf(0) } // 0 = Vocabulary, 1 = Conjugation, 2 = Grammar

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = activeSubTabIndex) {
            Tab(
                selected = activeSubTabIndex == 0,
                onClick = { activeSubTabIndex = 0 },
                text = { Text("Vocabulary") }
            )
            Tab(
                selected = activeSubTabIndex == 1,
                onClick = { activeSubTabIndex = 1 },
                text = { Text("Conjugation") }
            )
            Tab(
                selected = activeSubTabIndex == 2,
                onClick = { activeSubTabIndex = 2 },
                text = { Text("Grammar") }
            )
        }

        when (activeSubTabIndex) {
            0 -> VocabularyPracticeTab(viewModel = viewModel)
            1 -> ConjugationPracticeTab()
            2 -> GrammarPracticeTab()
        }
    }
}