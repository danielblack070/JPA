package com.ace.jp.japanesepractice.ui.collection.dialogs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ace.jp.japanesepractice.data.model.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddGrammarRuleDialog(
    allMasterRules: List<MasterRule>,
    initialDescription: String = "",
    initialEnglishExample: String? = null,
    initialJapaneseExample: String? = null,
    initialRuleObjects: List<GrammarObject> = emptyList(),
    isEditMode: Boolean = false,
    onDismiss: () -> Unit,
    onConfirm: (String, String?, String?, List<GrammarObject>) -> Unit
) {
    var description by remember { mutableStateOf(initialDescription) }
    var englishExample by remember { mutableStateOf(initialEnglishExample ?: "") }
    var japaneseExample by remember { mutableStateOf(initialJapaneseExample ?: "") }
    var ruleObjects by remember { mutableStateOf(initialRuleObjects) }

    var showAddFixedDialog by remember { mutableStateOf(false) }
    var showAddDynamicDialog by remember { mutableStateOf(false) }

    // State to edit an existing object in our builder list
    var selectedObjectToEditIndex by remember { mutableStateOf<Int?>(null) }
    var showBuilderHelp by remember { mutableStateOf(false) }

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
                    value = englishExample,
                    onValueChange = { englishExample = it },
                    label = { Text("English Example (Optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = japaneseExample,
                    onValueChange = { japaneseExample = it },
                    label = { Text("Japanese Example (Optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Interactive Rule Builder", style = MaterialTheme.typography.titleMedium)
                    IconButton(onClick = { showBuilderHelp = true }) {
                        Text("❓", style = MaterialTheme.typography.bodyLarge)
                    }
                }

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

                // Vertical list representation with re-ordering controls
                if (ruleObjects.isEmpty()) {
                    Text("No rule elements added yet. Build your rule sequence above.", style = MaterialTheme.typography.bodySmall)
                } else {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
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
                                        selectedObjectToEditIndex = index
                                    }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(
                                        modifier = Modifier.weight(1f),
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        val typeLabel = if (obj is FixedObject) "[Fixed]" else "[Dynamic]"
                                        Text(
                                            text = "${index + 1}. $typeLabel ${obj.name}",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        IconButton(
                                            onClick = {
                                                if (index > 0) {
                                                    val list = ruleObjects.toMutableList()
                                                    val temp = list[index]
                                                    list[index] = list[index - 1]
                                                    list[index - 1] = temp
                                                    ruleObjects = list
                                                }
                                            },
                                            enabled = index > 0,
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Text("▲", style = MaterialTheme.typography.bodyMedium)
                                        }

                                        IconButton(
                                            onClick = {
                                                if (index < ruleObjects.size - 1) {
                                                    val list = ruleObjects.toMutableList()
                                                    val temp = list[index]
                                                    list[index] = list[index + 1]
                                                    list[index + 1] = temp
                                                    ruleObjects = list
                                                }
                                            },
                                            enabled = index < ruleObjects.size - 1,
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Text("▼", style = MaterialTheme.typography.bodyMedium)
                                        }
                                    }
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
                            englishExample.trim().ifBlank { null },
                            japaneseExample.trim().ifBlank { null },
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

    // Help Dialog Overlay for Builder Info
    if (showBuilderHelp) {
        AlertDialog(
            onDismissRequest = { showBuilderHelp = false },
            title = { Text("How to build your Grammar Rule") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("• Use the Up (▲) and Down (▼) buttons on each rule element card to re-order your sequence list.", style = MaterialTheme.typography.bodyMedium)
                    Text("• Click an element card's description/label area directly to edit its properties or permanently delete it.", style = MaterialTheme.typography.bodyMedium)
                    Text("• Use '+ Fixed' for permanent constant text (e.g. particles like は/が or endings).", style = MaterialTheme.typography.bodyMedium)
                    Text("• Use '+ Dynamic' to bind a general type category and optional conjugation rules.", style = MaterialTheme.typography.bodyMedium)
                }
            },
            confirmButton = {
                Button(onClick = { showBuilderHelp = false }) { Text("I see") }
            }
        )
    }

    // --- SUB-DIALOG: EDIT EXISTING RULE OBJECT ---
    if (selectedObjectToEditIndex != null) {
        val index = selectedObjectToEditIndex!!
        val obj = ruleObjects[index]

        if (obj is FixedObject) {
            var valFixed by remember { mutableStateOf(obj.word) }
            AlertDialog(
                onDismissRequest = { selectedObjectToEditIndex = null },
                title = { Text("Edit Fixed Element") },
                text = {
                    OutlinedTextField(
                        value = valFixed,
                        onValueChange = { valFixed = it },
                        label = { Text("Fixed Word Value") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                            onClick = {
                                ruleObjects = ruleObjects.toMutableList().apply { removeAt(index) }
                                selectedObjectToEditIndex = null
                            }
                        ) { Text("Delete") }

                        Button(
                            onClick = {
                                ruleObjects = ruleObjects.toMutableList().apply {
                                    this[index] = FixedObject(valFixed.trim())
                                }
                                selectedObjectToEditIndex = null
                            },
                            enabled = valFixed.trim().isNotBlank()
                        ) { Text("Save") }
                    }
                },
                dismissButton = {
                    OutlinedButton(onClick = { selectedObjectToEditIndex = null }) { Text("Cancel") }
                }
            )
        } else if (obj is DynamicObject) {
            var broadType by remember { mutableStateOf(obj.broadType) }
            var selectedForm by remember { mutableStateOf(obj.form) }

            var broadTypeExpanded by remember { mutableStateOf(false) }
            var formExpanded by remember { mutableStateOf(false) }
            var searchFormQuery by remember { mutableStateOf("") }

            val isAdverbOrOther = broadType == BroadType.Adverb || broadType == BroadType.Other

            AlertDialog(
                onDismissRequest = { selectedObjectToEditIndex = null },
                title = { Text("Edit Dynamic Element") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        // Broad Type Select
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = broadType.name,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Broad Type") },
                                trailingIcon = { Icon(Icons.Default.ArrowDropDown, "Select Broad Type") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { broadTypeExpanded = true },
                                enabled = false,
                                colors = OutlinedTextFieldDefaults.colors(
                                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                    disabledBorderColor = MaterialTheme.colorScheme.outline
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

                        // Conjugation Rule Form Selection
                        if (!isAdverbOrOther) {
                            Box(modifier = Modifier.fillMaxWidth()) {
                                OutlinedTextField(
                                    value = selectedForm?.name ?: "None (Optional)",
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Conjugation Form") },
                                    trailingIcon = { Icon(Icons.Default.ArrowDropDown, "Select Form") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { formExpanded = true },
                                    enabled = false,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                        disabledBorderColor = MaterialTheme.colorScheme.outline
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
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                            onClick = {
                                ruleObjects = ruleObjects.toMutableList().apply { removeAt(index) }
                                selectedObjectToEditIndex = null
                            }
                        ) { Text("Delete") }

                        Button(
                            onClick = {
                                ruleObjects = ruleObjects.toMutableList().apply {
                                    this[index] = DynamicObject(broadType, if (isAdverbOrOther) null else selectedForm)
                                }
                                selectedObjectToEditIndex = null
                            }
                        ) { Text("Save") }
                    }
                },
                dismissButton = {
                    OutlinedButton(onClick = { selectedObjectToEditIndex = null }) { Text("Cancel") }
                }
            )
        }
    }

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
                                disabledBorderColor = MaterialTheme.colorScheme.outline
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
                                    disabledBorderColor = MaterialTheme.colorScheme.outline
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
