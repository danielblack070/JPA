package com.ace.jp.japanesepractice.ui.collection

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.ace.jp.japanesepractice.data.model.DynamicObject
import com.ace.jp.japanesepractice.data.model.FixedObject
import com.ace.jp.japanesepractice.data.model.GrammarRule
import com.ace.jp.japanesepractice.ui.collection.dialogs.AddGrammarRuleDialog
import com.ace.jp.japanesepractice.ui.collection.dialogs.ConfirmationDialog

@Composable
fun GrammarTabContent(viewModel: GrammarViewModel) {
    val grammarRules by viewModel.grammarRules.collectAsState()
    val allMasterRules by viewModel.masterRules.collectAsState()

    var searchQuery by remember { mutableStateOf("") }

    val filteredRules = grammarRules.filter {
        it.description.contains(searchQuery, ignoreCase = true) ||
                (it.example?.contains(searchQuery, ignoreCase = true) == true)
    }

    var showAddGrammarDialog by remember { mutableStateOf(false) }
    var grammarRuleToEdit by remember { mutableStateOf<GrammarRule?>(null) }
    var grammarRuleToDelete by remember { mutableStateOf<GrammarRule?>(null) }
    var showDeleteAllGrammarRulesDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                modifier = Modifier.weight(1f),
                onClick = { showAddGrammarDialog = true }
            ) { Text("Add Rule") }

            Button(
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                onClick = { showDeleteAllGrammarRulesDialog = true }
            ) { Text("Delete All") }
        }

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Search description or example sentences...") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        )

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(filteredRules) { rule ->
                GrammarRuleRow(
                    rule = rule,
                    onToggleEnabled = { isEnabled ->
                        viewModel.updateGrammarRule(rule.copy(isEnabled = isEnabled))
                    },
                    onEdit = { grammarRuleToEdit = rule },
                    onDelete = { grammarRuleToDelete = rule }
                )
            }
        }
    }

    // Add dialog
    if (showAddGrammarDialog) {
        AddGrammarRuleDialog(
            allMasterRules = allMasterRules,
            onDismiss = { showAddGrammarDialog = false },
            onConfirm = { desc, ex, list ->
                viewModel.addGrammarRule(desc, ex, list)
                showAddGrammarDialog = false
            }
        )
    }

    // Edit dialog
    if (grammarRuleToEdit != null) {
        val r = grammarRuleToEdit!!
        AddGrammarRuleDialog(
            allMasterRules = allMasterRules,
            initialDescription = r.description,
            initialExample = r.example,
            initialRuleObjects = r.rule,
            isEditMode = true,
            onDismiss = { grammarRuleToEdit = null },
            onConfirm = { desc, ex, list ->
                viewModel.updateGrammarRule(r.copy(description = desc, example = ex, rule = list))
                grammarRuleToEdit = null
            }
        )
    }

    // Deletion targets
    if (grammarRuleToDelete != null) {
        val r = grammarRuleToDelete!!
        ConfirmationDialog(
            title = "Delete Grammar Rule",
            text = "Are you sure you want to delete '${r.description}'?",
            onDismiss = { grammarRuleToDelete = null },
            onConfirm = { viewModel.deleteGrammarRule(r); grammarRuleToDelete = null }
        )
    }

    // Delete all
    if (showDeleteAllGrammarRulesDialog) {
        ConfirmationDialog(
            title = "Delete All Grammar Rules",
            text = "Confirm deleting all existing Grammar Rules? This action is irreversible.",
            onDismiss = { showDeleteAllGrammarRulesDialog = false },
            onConfirm = { viewModel.deleteAllGrammarRules(); showDeleteAllGrammarRulesDialog = false }
        )
    }
}

@Composable
fun GrammarRuleRow(
    rule: GrammarRule,
    onToggleEnabled: (Boolean) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { isExpanded = !isExpanded }
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = rule.description,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleMedium
                )

                // Green/Red styled Toggle Switch
                val switchThumbColor = if (rule.isEnabled) Color(0xFF4CAF50) else Color(0xFFF44336)
                Switch(
                    checked = rule.isEnabled,
                    onCheckedChange = onToggleEnabled,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = switchThumbColor,
                        uncheckedThumbColor = switchThumbColor,
                        checkedTrackColor = Color(0xFFC0EFC1),
                        uncheckedTrackColor = Color(0xFFFCF4D2)
                    )
                )

                OutlinedButton(onClick = onEdit, contentPadding = PaddingValues(horizontal = 8.dp)) {
                    Text("Edit")
                }
                IconButton(onClick = onDelete) {
                    Text("✖", style = MaterialTheme.typography.bodyMedium)
                }
            }

            if (isExpanded) {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    thickness = DividerDefaults.Thickness,
                    color = DividerDefaults.color
                )
                if (!rule.example.isNullOrBlank()) {
                    Text("Example Phrase: ${rule.example}", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(6.dp))
                }
                Text("Rule Element Formats:", style = MaterialTheme.typography.titleSmall)

                // Show cards inline
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    rule.rule.forEach { obj ->
                        val cardColor = if (obj is FixedObject) {
                            MaterialTheme.colorScheme.secondaryContainer
                        } else {
                            MaterialTheme.colorScheme.tertiaryContainer
                        }
                        Card(
                            colors = CardDefaults.cardColors(containerColor = cardColor),
                            modifier = Modifier.padding(2.dp)
                        ) {
                            Text(
                                text = obj.name,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}