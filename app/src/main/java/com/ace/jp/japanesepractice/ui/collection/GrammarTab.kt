package com.ace.jp.japanesepractice.ui.collection

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
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
import com.ace.jp.japanesepractice.data.model.FixedObject
import com.ace.jp.japanesepractice.data.model.GrammarRule
import com.ace.jp.japanesepractice.ui.collection.dialogs.AddGrammarRuleDialog
import com.ace.jp.japanesepractice.ui.collection.dialogs.ConfirmationDialog
import androidx.compose.ui.platform.LocalLocale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun GrammarTabContent(viewModel: GrammarViewModel) {
    val grammarRules by viewModel.grammarRules.collectAsState()
    val allMasterRules by viewModel.masterRules.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedConfidenceLevels by remember { mutableStateOf(setOf(0, 1, 2, 3, 4, 5)) }
    var lastPracticedFilter by remember { mutableStateOf("Any") }
    var lastPracticedFilterExpanded by remember { mutableStateOf(false) }

    val filteredRules = grammarRules.filter {
        val matchesSearch = it.description.contains(searchQuery, ignoreCase = true) ||
                (it.englishExample?.contains(searchQuery, ignoreCase = true) == true) ||
                (it.japaneseExample?.contains(searchQuery, ignoreCase = true) == true)
        val matchesConf = it.confidence in selectedConfidenceLevels
        val matchesLastPracticed = when (lastPracticedFilter) {
            "Any" -> true
            "Never" -> it.lastPracticed == null
            else -> {
                val durationMs = when (lastPracticedFilter) {
                    "Before 1 minute ago" -> 60_000L
                    "Before 1 hour ago" -> 3600_000L
                    "Before 12 hours ago" -> 12 * 3600_000L
                    "Before 1 day ago" -> 24 * 3600_000L
                    "Before 3 days ago" -> 3 * 24 * 3600_000L
                    "Before 1 week ago" -> 7 * 24 * 3600_000L
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
                .padding(bottom = 8.dp)
        )

        // Filters Row
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Conf:", style = MaterialTheme.typography.bodySmall)
            Button(
                onClick = {
                    selectedConfidenceLevels = if (selectedConfidenceLevels.size == 6) emptySet() else setOf(0, 1, 2, 3, 4, 5)
                },
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                colors = ButtonDefaults.outlinedButtonColors()
            ) {
                Text(if (selectedConfidenceLevels.size == 6) "None" else "All", fontSize = 10.sp)
            }

            Row(
                modifier = Modifier
                    .weight(1f)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                (0..5).forEach { lvl ->
                    FilterChip(
                        selected = lvl in selectedConfidenceLevels,
                        onClick = {
                            selectedConfidenceLevels = if (lvl in selectedConfidenceLevels) selectedConfidenceLevels - lvl else selectedConfidenceLevels + lvl
                        },
                        label = { Text("${lvl * 20}%", fontSize = 10.sp) }
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
                    "Any", "Before 1 minute ago", "Before 1 hour ago",
                    "Before 12 hours ago", "Before 1 day ago", "Before 3 days ago",
                    "Before 1 week ago", "Never practiced"
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
            allMasterRules = allMasterRules,
            onDismiss = { showAddGrammarDialog = false },
            onConfirm = { desc, engEx, jpEx, list ->
                viewModel.addGrammarRule(desc, engEx, jpEx, list)
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
            initialEnglishExample = r.englishExample,
            initialJapaneseExample = r.japaneseExample,
            initialRuleObjects = r.rule,
            isEditMode = true,
            onDismiss = { grammarRuleToEdit = null },
            onConfirm = { desc, engEx, jpEx, list ->
                viewModel.updateGrammarRule(r.copy(description = desc, englishExample = engEx, japaneseExample = jpEx, rule = list))
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

@OptIn(ExperimentalLayoutApi::class)
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

                // Display English and Japanese Examples
                if (!rule.englishExample.isNullOrBlank()) {
                    Text("English Example: ${rule.englishExample}", style = MaterialTheme.typography.bodyMedium)
                }
                if (!rule.japaneseExample.isNullOrBlank()) {
                    Text("Japanese Example: ${rule.japaneseExample}", style = MaterialTheme.typography.bodyMedium)
                }

                // Practice Stats Display
                val formattedConfidencePct = rule.confidence * 20
                val dateStr = if (rule.lastPracticed == null) {
                    "Never"
                } else {
                    java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", LocalLocale.current.platformLocale).format(java.util.Date(rule.lastPracticed))
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text("Confidence: $formattedConfidencePct% | Last Practiced: $dateStr", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                Spacer(modifier = Modifier.height(8.dp))
                Text("Rule Element Formats (Sequence):", style = MaterialTheme.typography.titleSmall)

                // Inline FlowRow Layout for elements sequence
                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    rule.rule.forEach { obj ->
                        val cardColor = if (obj is FixedObject) {
                            MaterialTheme.colorScheme.secondaryContainer
                        } else {
                            MaterialTheme.colorScheme.tertiaryContainer
                        }
                        Card(
                            colors = CardDefaults.cardColors(containerColor = cardColor),
                            modifier = Modifier.padding(1.dp)
                        ) {
                            Text(
                                text = obj.name,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
