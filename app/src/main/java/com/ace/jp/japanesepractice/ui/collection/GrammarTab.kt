package com.ace.jp.japanesepractice.ui.collection

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ace.jp.japanesepractice.data.model.GrammarRule
import com.ace.jp.japanesepractice.ui.collection.dialogs.AddGrammarRuleDialog
import com.ace.jp.japanesepractice.ui.collection.dialogs.ConfirmationDialog
import androidx.compose.ui.platform.LocalLocale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun GrammarTabContent(viewModel: GrammarViewModel) {

    LaunchedEffect(Unit) {
        viewModel.loadData()
    }

    val grammarRules by viewModel.grammarRules.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedConfidenceLevels by remember { mutableStateOf(setOf(0, 1, 2, 3, 4, 5)) }
    var lastPracticedFilter by remember { mutableStateOf("Any") }
    var lastPracticedFilterExpanded by remember { mutableStateOf(false) }

    val filteredRules = grammarRules.filter {
        val matchesSearch = it.name.contains(searchQuery, ignoreCase = true) ||
            it.description.contains(searchQuery, ignoreCase = true) ||
                it.details.contains(searchQuery, ignoreCase = true)
        val matchesConf = it.confidence in selectedConfidenceLevels
        val matchesLastPracticed = when (lastPracticedFilter) {
            "Any" -> true
            "Never" -> it.lastPracticed == null
            else -> {
                val durationMs = when (lastPracticedFilter) {
                    "More than a day ago" -> 24 * 3600_000L
                    "More than a week ago" -> 7 * 24 * 3600_000L
                    "More than a month ago" -> 30 * 24 * 3600_000L
                    else -> 0L
                }
                val cutOffTime = System.currentTimeMillis() - durationMs
                it.lastPracticed != null && it.lastPracticed <= cutOffTime
            }
        }
        matchesSearch && matchesConf && matchesLastPracticed
    }

    var showAddGrammarDialog by remember { mutableStateOf(false) }
    var grammarRuleToEdit by remember { mutableStateOf<GrammarRule?>(null) }
    var grammarRuleToDelete by remember { mutableStateOf<GrammarRule?>(null) }
    var showDeleteAllGrammarRulesDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Max)
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                onClick = { showAddGrammarDialog = true }
            ) { Text("Add Rule") }

            Button(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                onClick = { showDeleteAllGrammarRulesDialog = true }
            ) { Text("Delete All") }
        }

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Search name, description or details...") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        )

        // Confidence levels (column layout)
        Column(verticalArrangement = Arrangement.spacedBy(0.dp), modifier = Modifier.padding(bottom = 12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Selected Confidence Levels", style = MaterialTheme.typography.labelMedium)
                Button(
                    onClick = {
                        selectedConfidenceLevels = if (selectedConfidenceLevels.size == 6) emptySet() else setOf(0, 1, 2, 3, 4, 5)
                    },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    colors = ButtonDefaults.outlinedButtonColors()
                ) {
                    Text(if (selectedConfidenceLevels.size == 6) "Clear All" else "Select All", fontSize = 10.sp)
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                (0..5).forEach { lvl ->
                    FilterChip(
                        selected = lvl in selectedConfidenceLevels,
                        onClick = {
                            selectedConfidenceLevels = if (lvl in selectedConfidenceLevels) selectedConfidenceLevels - lvl else selectedConfidenceLevels + lvl
                        },
                        label = { Text("${lvl * 20}%", fontSize = 9.sp) }
                    )
                }
            }
        }

        Box(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
            OutlinedButton(
                onClick = { lastPracticedFilterExpanded = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Last Practiced Filter: $lastPracticedFilter")
                Icon(Icons.Default.ArrowDropDown, "Select Period")
            }
            DropdownMenu(
                expanded = lastPracticedFilterExpanded,
                onDismissRequest = { lastPracticedFilterExpanded = false },
                modifier = Modifier.fillMaxWidth(0.9f)
            ) {
                listOf(
                    "Any", "More than a day ago", "More than a week ago",
                    "More than a month ago", "Never practiced"
                ).forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            lastPracticedFilter = option
                            lastPracticedFilterExpanded = false
                        }
                    )
                }
            }
        }

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
            onDismiss = { showAddGrammarDialog = false },
            onConfirm = { name, desc, details ->
                viewModel.addGrammarRule(name, desc, details)
                showAddGrammarDialog = false
            }
        )
    }

    // Edit dialog
    if (grammarRuleToEdit != null) {
        val r = grammarRuleToEdit!!
        AddGrammarRuleDialog(
            initialName = r.name,
            initialDescription = r.description,
            initialDetails = r.details,
            isEditMode = true,
            onDismiss = { grammarRuleToEdit = null },
            onConfirm = { name, desc, details ->
                viewModel.updateGrammarRule(r.copy(name = name, description = desc, details = details))
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
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.height(IntrinsicSize.Max)
            ) {
                Text(
                    text = rule.name,
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

                OutlinedButton(
                    onClick = onEdit,
                    contentPadding = PaddingValues(horizontal = 8.dp),
                    modifier = Modifier.fillMaxHeight()
                ) {
                    Text("Edit")
                }
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.fillMaxHeight()
                ) {
                    Text("✖", style = MaterialTheme.typography.bodyMedium)
                }
            }

            if (isExpanded) {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    thickness = DividerDefaults.Thickness,
                    color = DividerDefaults.color
                )

                Text(
                    text = rule.description,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Text(
                    text = rule.details,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Practice Stats Display
                val formattedConfidencePct = rule.confidence * 20
                val dateStr = if (rule.lastPracticed == null) {
                    "Never"
                } else {
                    java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", LocalLocale.current.platformLocale).format(java.util.Date(rule.lastPracticed))
                }
                Text("Confidence: $formattedConfidencePct% | Last Practiced: $dateStr", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
