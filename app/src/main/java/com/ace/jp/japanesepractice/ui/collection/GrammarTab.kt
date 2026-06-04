package com.ace.jp.japanesepractice.ui.collection

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun GrammarTab() {
    Column(modifier = Modifier.padding(16.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = {}) { Text("Add Rule") }
            Button(onClick = {}) { Text("Delete all rules") }
        }
        Text("List of grammar rules (placeholder)")
    }
}
