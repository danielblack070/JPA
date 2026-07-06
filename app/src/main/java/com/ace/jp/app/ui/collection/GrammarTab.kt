package com.ace.jp.app.ui.collection

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ace.jp.app.data.model.GrammarRule
import com.ace.jp.app.data.model.ExampleSentence
import com.ace.jp.app.ui.collection.dialogs.AddGrammarRuleDialog
import com.ace.jp.app.ui.collection.dialogs.ConfirmationDialog
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import com.ace.jp.app.ui.AutoScalingText

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun GrammarTabContent(viewModel: GrammarViewModel) {

    LaunchedEffect(Unit) {
        viewModel.loadData()
    }

    val grammarRules by viewModel.grammarRules.collectAsState()
    val exampleSentencesMap by viewModel.exampleSentences.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedConfidenceLevels by remember { mutableStateOf(setOf(0, 1, 2, 3, 4, 5)) }
    var lastPracticedFilter by remember { mutableStateOf("Any") }
    var lastPracticedFilterExpanded by remember { mutableStateOf(false) }

    val filteredRules = grammarRules.filter {
        val matchesSearch = it.name.contains(searchQuery, ignoreCase = true) ||
                it.englishRule.contains(searchQuery, ignoreCase = true) ||
                it.japaneseRule.contains(searchQuery, ignoreCase = true) ||
                (it.readingRule?.contains(searchQuery, ignoreCase = true) ?: false) ||
                (it.notes?.contains(searchQuery, ignoreCase = true) ?: false)
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
            label = { Text("Search name, rules or notes...") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        )

        // Confidence levels (column layout)
        Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
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
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val initialFontSize = 12.sp
                var unifiedFontSize by remember { mutableStateOf(initialFontSize) }

                (0..5).forEach { lvl ->
                    FilterChip(
                        // Crucial: weight(1f) forces all 6 chips to divide the screen width exactly equally
                        modifier = Modifier.weight(1f),
                        selected = lvl in selectedConfidenceLevels,
                        onClick = {
                            selectedConfidenceLevels = if (lvl in selectedConfidenceLevels) selectedConfidenceLevels - lvl else selectedConfidenceLevels + lvl
                        },
                        label = {
                            AutoScalingText(
                                text = "${lvl * 20}%",
                                modifier = Modifier.fillMaxWidth(),
                                style = TextStyle(textAlign = TextAlign.Center),
                                initialFontSize = initialFontSize,
                                // Pass the currently decided unified size down
                                currentUnifiedSize = unifiedFontSize,
                                // When this specific chip shrinks, it reports its size back here
                                onSizeShrunk = { determinedSize ->
                                    if (determinedSize < unifiedFontSize) {
                                        unifiedFontSize = determinedSize
                                    }
                                }
                            )
                        }
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
                    examples = exampleSentencesMap[rule.id] ?: emptyList(),
                    onToggleEnabled = { isEnabled ->
                        viewModel.updateGrammarRule(rule.copy(isEnabled = isEnabled))
                    },
                    onEdit = { grammarRuleToEdit = rule },
                    onDelete = { grammarRuleToDelete = rule },
                    onAddExample = { english, japanese, reading ->
                        viewModel.addExampleSentence(rule.id, english, japanese, reading)
                    },
                    onDeleteExample = { example ->
                        viewModel.deleteExampleSentence(example)
                    }
                )
            }
        }
    }

    // Add dialog
    if (showAddGrammarDialog) {
        AddGrammarRuleDialog(
            onDismiss = { showAddGrammarDialog = false },
            onConfirm = { name, englishRule, japaneseRule, readingRule, notes ->
                viewModel.addGrammarRule(
                    name = name,
                    englishRule = englishRule,
                    japaneseRule = japaneseRule,
                    readingRule = readingRule,
                    notes = notes
                )
                showAddGrammarDialog = false
            }
        )
    }

    // Edit dialog
    if (grammarRuleToEdit != null) {
        val r = grammarRuleToEdit!!
        AddGrammarRuleDialog(
            initialName = r.name,
            initialEnglishRule = r.englishRule,
            initialJapaneseRule = r.japaneseRule,
            initialReadingRule = r.readingRule ?: "",
            initialNotes = r.notes ?: "",
            isEditMode = true,
            onDismiss = { grammarRuleToEdit = null },
            onConfirm = { name, englishRule, japaneseRule, readingRule, notes ->
                viewModel.updateGrammarRule(
                    r.copy(
                        name = name,
                        englishRule = englishRule,
                        japaneseRule = japaneseRule,
                        readingRule = readingRule,
                        notes = notes
                    )
                )
                grammarRuleToEdit = null
            }
        )
    }

    // Deletion targets
    if (grammarRuleToDelete != null) {
        val r = grammarRuleToDelete!!
        ConfirmationDialog(
            title = "Delete Grammar Rule",
            text = "Are you sure you want to delete '${r.name}'?",
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GrammarRuleRow(
    rule: GrammarRule,
    examples: List<ExampleSentence>,
    onToggleEnabled: (Boolean) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onAddExample: (english: String, japanese: String, reading: String?) -> Unit,
    onDeleteExample: (ExampleSentence) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    var showAddExampleDialog by remember { mutableStateOf(false) }

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
                    Text(
                        text = rule.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Green/Red styled Toggle Switch
                val switchThumbColor = if (rule.isEnabled) Color(0xFF4CAF50) else Color(0xFFF44336)
                Switch(
                    checked = rule.isEnabled,
                    onCheckedChange = onToggleEnabled,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = switchThumbColor,
                        uncheckedThumbColor = switchThumbColor,
                        checkedTrackColor = Color(0xFFC8E6C9),
                        uncheckedTrackColor = Color(0xFFFFCDD2)
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

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Japanese Rule Box
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            Text(
                                "Japanese Rule",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                rule.japaneseRule,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }

                    // English Rule
                    Column {
                        Text(
                            "English Rule",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(rule.englishRule, style = MaterialTheme.typography.bodyMedium)
                    }

                    // Reading Rule (Optional)
                    if (!rule.readingRule.isNullOrBlank()) {
                        Column {
                            Text(
                                "Reading Rule",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(rule.readingRule, style = MaterialTheme.typography.bodyMedium)
                        }
                    }

                    // Notes (Optional)
                    if (!rule.notes.isNullOrBlank()) {
                        Column {
                            Text(
                                "Notes",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.secondary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(rule.notes, style = MaterialTheme.typography.bodyMedium)
                        }
                    }

                    // Examples Section (Similar to sub-rules, added/removed inside the expanded item view)
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Example Sentences (${examples.size})",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.secondary,
                                fontWeight = FontWeight.Bold
                            )
                            TextButton(
                                onClick = { showAddExampleDialog = true }
                            ) {
                                Text("+ Add Example", style = MaterialTheme.typography.labelMedium)
                            }
                        }

                        if (examples.isEmpty()) {
                            Text(
                                "No example sentences added yet.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        } else {
                            examples.forEach { example ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = example.japanese,
                                                style = MaterialTheme.typography.bodyLarge,
                                                fontWeight = FontWeight.Medium
                                            )
                                            if (!example.reading.isNullOrBlank()) {
                                                Text(
                                                    text = "[ ${example.reading} ]",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    fontFamily = FontFamily.Monospace
                                                )
                                            }
                                            Text(
                                                text = example.english,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        IconButton(
                                            onClick = { onDeleteExample(example) }
                                        ) {
                                            Text("✖", color = MaterialTheme.colorScheme.error)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Practice Stats Display
                    val formattedConfidencePct = rule.confidence * 20
                    val dateStr = if (rule.lastPracticed == null) {
                        "Never"
                    } else {
                        java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", LocalLocale.current.platformLocale).format(java.util.Date(rule.lastPracticed))
                    }
                    Text(
                        "Confidence: $formattedConfidencePct% | Last Practiced: $dateStr",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }

    if (showAddExampleDialog) {
        AddExampleSentenceDialog(
            onDismiss = { showAddExampleDialog = false },
            onConfirm = { english, japanese, reading ->
                onAddExample(english, japanese, reading)
                showAddExampleDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExampleSentenceDialog(
    onDismiss: () -> Unit,
    onConfirm: (english: String, japanese: String, reading: String?) -> Unit
) {
    var english by remember { mutableStateOf("") }
    var japanese by remember { mutableStateOf("") }
    var reading by remember { mutableStateOf("") }

    val isFormValid = english.trim().isNotEmpty() && japanese.trim().isNotEmpty()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Example Sentence") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = japanese,
                    onValueChange = { japanese = it },
                    label = { Text("Japanese *") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = english,
                    onValueChange = { english = it },
                    label = { Text("English *") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = reading,
                    onValueChange = { reading = it },
                    label = { Text("Reading (Optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (isFormValid) {
                        onConfirm(english.trim(), japanese.trim(), reading.trim().takeIf { it.isNotEmpty() })
                    }
                },
                enabled = isFormValid
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
