package com.ace.jp.japanesepractice.ui.collection

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ace.jp.japanesepractice.data.model.MasterRule
import com.ace.jp.japanesepractice.data.model.SubRule
import com.ace.jp.japanesepractice.data.model.Type
import com.ace.jp.japanesepractice.ui.collection.dialogs.*
import androidx.compose.ui.platform.LocalLocale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConjugationTabContent(viewModel: ConjugationViewModel) {
    LaunchedEffect(Unit) {
        viewModel.loadData()
    }

    val masterRules by viewModel.masterRules.collectAsState()
    val subRules by viewModel.subRules.collectAsState()

    var ruleSearchQuery by remember { mutableStateOf("") }
    var selectedConfidenceLevels by remember { mutableStateOf(setOf(0, 1, 2, 3, 4, 5)) }
    var lastPracticedFilter by remember { mutableStateOf("Any") }
    var lastPracticedFilterExpanded by remember { mutableStateOf(false) }

    val filteredMasterRules = masterRules.filter {
        val matchesSearch = it.name.contains(ruleSearchQuery, ignoreCase = true)
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

    var showAddMasterDialog by remember { mutableStateOf(false) }
    var masterRuleToEdit by remember { mutableStateOf<MasterRule?>(null) }
    var masterRuleToDelete by remember { mutableStateOf<MasterRule?>(null) }
    var showDeleteAllMasterRulesDialog by remember { mutableStateOf(false) }

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
                onClick = { showAddMasterDialog = true }
            ) { Text("Add Form") }

            Button(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                onClick = { showDeleteAllMasterRulesDialog = true }
            ) { Text("Delete All") }
        }

        OutlinedTextField(
            value = ruleSearchQuery,
            onValueChange = { ruleSearchQuery = it },
            label = { Text("Search for conjugation form names...") },
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
            items(filteredMasterRules) { masterRule ->
                val nestedSubrules = subRules.filter { it.masterRuleId == masterRule.id }
                MasterRuleRow(
                    masterRule = masterRule,
                    subRules = nestedSubrules,
                    onToggleEnabled = { isEnabled ->
                        viewModel.updateMasterRule(masterRule.copy(isEnabled = isEnabled))
                    },
                    onEdit = { masterRuleToEdit = masterRule },
                    onDelete = { masterRuleToDelete = masterRule },
                    onAddSubRule = { tp, orig, new, unique ->
                        viewModel.addSubRule(masterRule.id, tp, orig, new, unique)
                    },
                    onDeleteAllSubrules = {
                        viewModel.deleteSubRulesForMasterRule(masterRule.id)
                    },
                    onDeleteSubrule = { sub ->
                        viewModel.deleteSubRule(sub)
                    },
                    onUpdateSubrule = { sub, t, o, n, u ->
                        viewModel.updateSubRule(sub.copy(type = t, originalEnding = o, newEnding = n, isUnique = u))
                    }
                )
            }
        }
    }

    // Add Overlay
    if (showAddMasterDialog) {
        AddMasterRuleDialog(
            existingRules = masterRules.map { it.name },
            onDismiss = { showAddMasterDialog = false },
            onConfirm = { name ->
                viewModel.addMasterRule(name)
                showAddMasterDialog = false
            }
        )
    }

    // Edit Overlay
    if (masterRuleToEdit != null) {
        val mr = masterRuleToEdit!!
        AddMasterRuleDialog(
            existingRules = masterRules.map { it.name },
            initialName = mr.name,
            onDismiss = { masterRuleToEdit = null },
            onConfirm = { name ->
                viewModel.updateMasterRule(mr.copy(name = name))
                masterRuleToEdit = null
            }
        )
    }

    // Single master deletion
    if (masterRuleToDelete != null) {
        val mr = masterRuleToDelete!!
        ConfirmationDialog(
            title = "Delete Form",
            text = "Are you sure you want to delete '${mr.name}' and all rules nested inside it?",
            onDismiss = { masterRuleToDelete = null },
            onConfirm = { viewModel.deleteMasterRule(mr); masterRuleToDelete = null }
        )
    }

    // Delete all master rules
    if (showDeleteAllMasterRulesDialog) {
        ConfirmationDialog(
            title = "Delete All Forms",
            text = "Are you sure you want to delete all forms and their rules? This cannot be undone.",
            onDismiss = { showDeleteAllMasterRulesDialog = false },
            onConfirm = { viewModel.deleteAllMasterRules(); showDeleteAllMasterRulesDialog = false }
        )
    }
}

@Composable
fun MasterRuleRow(
    masterRule: MasterRule,
    subRules: List<SubRule>,
    onToggleEnabled: (Boolean) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onAddSubRule: (Type, String?, String, Boolean) -> Unit,
    onDeleteAllSubrules: () -> Unit,
    onDeleteSubrule: (SubRule) -> Unit,
    onUpdateSubrule: (SubRule, Type, String?, String, Boolean) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    var showAddSubruleDialog by remember { mutableStateOf(false) }
    var showDeleteAllSubrulesConfirm by remember { mutableStateOf(false) }

    var selectedSubruleDetails by remember { mutableStateOf<SubRule?>(null) }
    var subRuleToEdit by remember { mutableStateOf<SubRule?>(null) }
    var subRuleToDelete by remember { mutableStateOf<SubRule?>(null) }

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
                Column(modifier = Modifier.weight(1f)) {
                    Text(masterRule.name, style = MaterialTheme.typography.titleMedium)
                    Text("Rules: ${subRules.size}", style = MaterialTheme.typography.bodySmall)
                }

                // Green/Red styled toggle switch
                val toggleThumbColor = if (masterRule.isEnabled) Color(0xFF4CAF50) else Color(0xFFF44336)
                Switch(
                    checked = masterRule.isEnabled,
                    onCheckedChange = onToggleEnabled,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = toggleThumbColor,
                        uncheckedThumbColor = toggleThumbColor,
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

                // Render confidence % and relative practiced info
                val formattedConfidencePct = masterRule.confidence * 20
                val dateStr = if (masterRule.lastPracticed == null) {
                    "Never"
                } else {
                    java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", LocalLocale.current.platformLocale).format(java.util.Date(masterRule.lastPracticed))
                }
                Text("Confidence: $formattedConfidencePct% | Last Practiced: $dateStr", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Max)
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Button(
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        onClick = { showAddSubruleDialog = true }
                    ) { Text("Add Rule") }

                    Button(
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        onClick = { showDeleteAllSubrulesConfirm = true }
                    ) { Text("Clear All") }
                }

                if (subRules.isEmpty()) {
                    Text("No rules defined under this conjugation form.", style = MaterialTheme.typography.bodyMedium)
                } else {
                    subRules.forEach { subRule ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp)
                                .clickable { selectedSubruleDetails = subRule }
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    // Removed subRule description and toggle switch from previews.
                                    // Instead, showing type and original ending.
                                    Text("Type Constraint: ${subRule.type.toDisplayString()}", style = MaterialTheme.typography.bodyMedium)
                                    Text("Original Ending: ${subRule.originalEnding ?: "None"}", style = MaterialTheme.typography.bodySmall)
                                }

                                OutlinedButton(
                                    onClick = { subRuleToEdit = subRule },
                                    contentPadding = PaddingValues(horizontal = 4.dp)
                                ) {
                                    Text("Edit")
                                }

                                IconButton(onClick = { subRuleToDelete = subRule }) {
                                    Text("✖", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Add nested
    if (showAddSubruleDialog) {
        AddSubRuleDialog(
            existingSubRules = subRules,
            isEditMode = false,
            onDismiss = { showAddSubruleDialog = false },
            onConfirm = { tp, orig, new, unique ->
                onAddSubRule(tp, orig, new, unique)
                showAddSubruleDialog = false
            }
        )
    }

    // Edit nested
    if (subRuleToEdit != null) {
        val s = subRuleToEdit!!
        AddSubRuleDialog(
            existingSubRules = subRules,
            editingSubRuleId = s.id,
            initialOriginalEnding = s.originalEnding ?: "",
            initialNewEnding = s.newEnding,
            initialType = s.type,
            initialIsUnique = s.isUnique,
            isEditMode = true,
            onDismiss = { subRuleToEdit = null },
            onConfirm = { tp, orig, new, unique ->
                onUpdateSubrule(s, tp, orig, new, unique)
                subRuleToEdit = null
            }
        )
    }

    // Delete nested single
    if (subRuleToDelete != null) {
        val s = subRuleToDelete!!
        ConfirmationDialog(
            title = "Delete Rule",
            text = "Are you sure you want to delete this rule?",
            onDismiss = { subRuleToDelete = null },
            onConfirm = { onDeleteSubrule(s); subRuleToDelete = null }
        )
    }

    // Delete all nested subrules of rule
    if (showDeleteAllSubrulesConfirm) {
        ConfirmationDialog(
            title = "Clear All Rules",
            text = "Delete all Rules inside '${masterRule.name}'?",
            onDismiss = { showDeleteAllSubrulesConfirm = false },
            onConfirm = { onDeleteAllSubrules(); showDeleteAllSubrulesConfirm = false }
        )
    }

    // Detailed popup cards of a single clickable Subrule
    if (selectedSubruleDetails != null) {
        val s = selectedSubruleDetails!!
        AlertDialog(
            onDismissRequest = { selectedSubruleDetails = null },
            title = { Text("Rule Details") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Type: ${s.type.toDisplayString()}", style = MaterialTheme.typography.bodyLarge)
                    Text("Original Ending: ${s.originalEnding ?: "None"}", style = MaterialTheme.typography.bodyLarge)
                    Text("Replacement Ending: ${s.newEnding}", style = MaterialTheme.typography.bodyLarge)
                    Text("Is Unique: ${if (s.isUnique) "Yes" else "No"}", style = MaterialTheme.typography.bodyLarge)
                }
            },
            confirmButton = {
                Button(onClick = { selectedSubruleDetails = null }) { Text("Close") }
            }
        )
    }
}
