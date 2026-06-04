package com.ace.jp.japanesepractice.ui.practice

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun PracticeScreen() {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Practice Tab")
        Text("Placeholder for practice features like flashcards or quizzes.")
    }
}
