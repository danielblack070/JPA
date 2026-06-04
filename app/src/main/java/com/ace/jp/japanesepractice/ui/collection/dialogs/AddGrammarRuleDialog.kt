package com.ace.jp.japanesepractice.ui.collection.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ace.jp.japanesepractice.data.model.*

@Composable
fun AddGrammarRuleDialog(onDismiss: () -> Unit, onConfirm: (String, String, List<GrammarObject>) -> Unit) {
    var description by remember { mutableStateOf("") }
    var example by remember { mutableStateOf("") }
    var ruleObjects by remember { mutableStateOf(listOf<GrammarObject>()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Grammar Rule") },
        text = {
            Column(modifier = Modifier.heightIn(max = 400.dp)) {
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") })
                OutlinedTextField(value = example, onValueChange = { example = it }, label = { Text("Example") })
                
                Text("Rule Objects", modifier = Modifier.padding(top = 8.dp))
                Row {
                    Button(onClick = { ruleObjects = ruleObjects + FixedObject("New Fixed") }) { Text("Add Fixed") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = { ruleObjects = ruleObjects + DynamicObject(Type.Noun) }) { Text("Add Dynamic") }
                }

                LazyColumn {
                    itemsIndexed(ruleObjects) { index, obj ->
                        Card(modifier = Modifier.fillMaxWidth().padding(4.dp)) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text(obj.name)
                                IconButton(onClick = { ruleObjects = ruleObjects.toMutableList().apply { removeAt(index) } }) {
                                    Text("X")
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { if (description.isNotBlank()) onConfirm(description, example, ruleObjects) },
                enabled = description.isNotBlank()
            ) { Text("Add") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
