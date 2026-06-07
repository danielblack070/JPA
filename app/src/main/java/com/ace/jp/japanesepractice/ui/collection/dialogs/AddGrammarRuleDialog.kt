package com.ace.jp.japanesepractice.ui.collection.dialogs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ace.jp.japanesepractice.data.model.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddGrammarRuleDialog(
    allMasterRules: List<MasterRule>,
    initialDescription: String = "",
    initialExample: String? = null,
    initialRuleObjects: List<GrammarObject> = emptyList(),
    isEditMode: Boolean = false,
    onDismiss: () -> Unit,
    onConfirm: (String, String?, List<GrammarObject>) -> Unit
) {
    var description by remember { mutableStateOf(initialDescription) }
    var example by remember { mutableStateOf(initialExample ?: "") }
    var ruleObjects by remember { mutableStateOf(initialRuleObjects) }

    var showAddFixedDialog by remember { mutableStateOf(false) }
    var showAddDynamicDialog by remember { mutableStateOf(false) }

    val isFormValid = description.trim().isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isEditMode) "Edit Grammar Rule" else "Add Grammar Rule") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (Mandatory)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = example,
                    onValueChange = { example = it },
                    label = { Text("Example Phrase (Optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)

                Text("Interactive Rule Builder", style = MaterialTheme.typography.titleMedium)
                Text("Cards represent ordering. Click any card to delete it.", style = MaterialTheme.typography.bodySmall)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        modifier = Modifier.weight(1f),
                        onClick = { showAddFixedDialog = true }
                    ) { Text("+ Fixed") }

                    Button(
                        modifier = Modifier.weight(1f),
                        onClick = { showAddDynamicDialog = true }
                    ) { Text("+ Dynamic") }
                }

                // Cards Flow representation list
                if (ruleObjects.isEmpty()) {
                    Text("No rule elements added yet.", style = MaterialTheme.typography.bodyMedium)
                } else {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        ruleObjects.forEachIndexed { index, obj ->
                            val cardColor = if (obj is FixedObject) {
                                MaterialTheme.colorScheme.secondaryContainer
                            } else {
                                MaterialTheme.colorScheme.tertiaryContainer
                            }
                            Card(
                                colors = CardDefaults.cardColors(containerColor = cardColor),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        ruleObjects = ruleObjects.toMutableList().apply { removeAt(index) }
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${index + 1}. ${obj.name}",
                                        modifier = Modifier.weight(1f),
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text("❌", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (isFormValid) {
                        onConfirm(
                            description.trim(),
                            example.trim().ifBlank { null },
                            ruleObjects
                        )
                    }
                },
                enabled = isFormValid
            ) { Text("Submit") }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Cancel") }
        }
    )

    // --- SUB-DIALOG: ADD FIXED OBJECT ---
    if (showAddFixedDialog) {
        var word by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddFixedDialog = false },
            title = { Text("Add Fixed Rule Part") },
            text = {
                OutlinedTextField(
                    value = word,
                    onValueChange = { word = it },
                    label = { Text("Fixed String (Mandatory)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        ruleObjects = ruleObjects + FixedObject(word.trim())
                        showAddFixedDialog = false
                    },
                    enabled = word.trim().isNotBlank()
                ) { Text("Add") }
            },
            dismissButton = {
                OutlinedButton(onClick = { showAddFixedDialog = false }) { Text("Cancel") }
            }
        )
    }

    // --- SUB-DIALOG: ADD DYNAMIC OBJECT ---
    if (showAddDynamicDialog) {
        var broadType by remember { mutableStateOf(BroadType.Noun) }
        var selectedForm by remember { mutableStateOf<MasterRule?>(null) }

        var broadTypeExpanded by remember { mutableStateOf(false) }
        var formExpanded by remember { mutableStateOf(false) }
        var searchFormQuery by remember { mutableStateOf("") }

        val isAdverbOrOther = broadType == BroadType.Adverb || broadType == BroadType.Other

        AlertDialog(
            onDismissRequest = { showAddDynamicDialog = false },
            title = { Text("Add Dynamic Rule Part") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Selecting BroadType
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = broadType.name,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Broad Type") },
                            trailingIcon = { Icon(Icons.Default.ArrowDropDown, "Type Dropdown") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { broadTypeExpanded = true },
                            enabled = false,
                            colors = OutlinedTextFieldDefaults.colors(
                                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                disabledBorderColor = MaterialTheme.colorScheme.outline,
                                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                        DropdownMenu(
                            expanded = broadTypeExpanded,
                            onDismissRequest = { broadTypeExpanded = false }
                        ) {
                            BroadType.entries.forEach { bType ->
                                DropdownMenuItem(
                                    text = { Text(bType.name) },
                                    onClick = {
                                        broadType = bType
                                        broadTypeExpanded = false
                                        if (bType == BroadType.Adverb || bType == BroadType.Other) {
                                            selectedForm = null
                                        }
                                    }
                                )
                            }
                        }
                    }

                    // Selecting Master Rule Form (if not Adverb/Other)
                    if (!isAdverbOrOther) {
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = selectedForm?.name ?: "None (Optional)",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Conjugation Form") },
                                trailingIcon = { Icon(Icons.Default.ArrowDropDown, "Rule Select") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { formExpanded = true },
                                enabled = false,
                                colors = OutlinedTextFieldDefaults.colors(
                                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                    disabledBorderColor = MaterialTheme.colorScheme.outline,
                                    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                            DropdownMenu(
                                expanded = formExpanded,
                                onDismissRequest = { formExpanded = false },
                                modifier = Modifier.fillMaxWidth(0.9f)
                            ) {
                                OutlinedTextField(
                                    value = searchFormQuery,
                                    onValueChange = { searchFormQuery = it },
                                    label = { Text("Search Master Rules...") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp),
                                    singleLine = true
                                )

                                DropdownMenuItem(
                                    text = { Text("None (Optional)") },
                                    onClick = {
                                        selectedForm = null
                                        formExpanded = false
                                        searchFormQuery = ""
                                    }
                                )

                                val filteredRules = allMasterRules.filter {
                                    it.name.contains(searchFormQuery, ignoreCase = true)
                                }
                                filteredRules.forEach { rule ->
                                    DropdownMenuItem(
                                        text = { Text(rule.name) },
                                        onClick = {
                                            selectedForm = rule
                                            formExpanded = false
                                            searchFormQuery = ""
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        ruleObjects = ruleObjects + DynamicObject(broadType, if (isAdverbOrOther) null else selectedForm)
                        showAddDynamicDialog = false
                    }
                ) { Text("Add") }
            },
            dismissButton = {
                OutlinedButton(onClick = { showAddDynamicDialog = false }) { Text("Cancel") }
            }
        )
    }
}