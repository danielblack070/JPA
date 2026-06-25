package com.ace.jp.japanesepractice.ui.practice

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ace.jp.japanesepractice.data.model.GrammarRule

@Composable
fun GrammarPracticeTab(viewModel: PracticeViewModel) {
    val isSession by viewModel.isGrammarActiveSession.collectAsState()
    val showSum by viewModel.grammarShowSummary.collectAsState()

    // Refresh database whenever navigating here or returning from a completed session
    LaunchedEffect(isSession, showSum) {
        if (!isSession && !showSum) {
            viewModel.loadData()
        }
    }

    Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        when {
            showSum -> { GrammarPracticeSummaryScreen(viewModel) }
            isSession -> { ActiveGrammarPracticeSessionScreen(viewModel) }
            else -> { GrammarPracticeSetupScreen(viewModel) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GrammarPracticeSetupScreen(viewModel: PracticeViewModel) {
    val countInput by viewModel.itemCountInput.collectAsState()
    val lpFilter by viewModel.lastPracticedFilter.collectAsState()
    val conf by viewModel.selectedConfidenceLevels.collectAsState()
    val selectedCount by viewModel.selectedGrammarRulesCount.collectAsState()

    var lastPracticedExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Grammar Practice Setup",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            // Live Selected Count Indicator
            Text(
                text = "Currently Selected Items for Practice: $selectedCount",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(vertical = 2.dp)
            )

            // Replace mode selector with static title telling user mode is flashcards
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(text = "Practice Mode", style = MaterialTheme.typography.labelMedium)
                Card(
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Flashcards Only",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            // Last Practiced Filter dropdown (Connected to grammar rules) (Uses UI matching the all words list screen)
            Box(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                OutlinedButton(
                    onClick = { lastPracticedExpanded = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Last Practiced Filter: ${lastPracticedFilterText(lpFilter)}")
                    Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = "Select Period")
                }

                DropdownMenu(
                    expanded = lastPracticedExpanded,
                    onDismissRequest = { lastPracticedExpanded = false },
                    modifier = Modifier.fillMaxWidth(0.9f)
                ) {
                    listOf("Any", "More than a day ago", "More than a week ago", "More than a month ago", "Never").forEach { opt ->
                        DropdownMenuItem(
                            text = { Text(lastPracticedFilterText(opt)) },
                            onClick = {
                                viewModel.setLastPracticedFilter(opt)
                                lastPracticedExpanded = false
                            }
                        )
                    }
                }
            }

            // 2. Confidence Level Select Multi-Chip row
            Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Selected Confidence Levels", style = MaterialTheme.typography.labelMedium)
                    Button(
                        onClick = {
                            viewModel.setConfidenceLevels(if (conf.size == 6) emptySet() else setOf(0, 1, 2, 3, 4, 5))
                        },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        colors = ButtonDefaults.outlinedButtonColors()
                    ) {
                        Text(if (conf.size == 6) "Clear All" else "Select All", fontSize = 10.sp)
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    (0..5).forEach { lvl ->
                        FilterChip(
                            selected = lvl in conf,
                            onClick = {
                                viewModel.setConfidenceLevels(if (lvl in conf) conf - lvl else conf + lvl)
                            },
                            label = { Text("${lvl * 20}%", fontSize = 9.sp) }
                        )
                    }
                }
            }

            // 3. Item count input
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = countInput,
                    onValueChange = { viewModel.setItemCountInput(it) },
                    label = { Text("Items per round") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Start Practice Trigger
        Button(
            onClick = { viewModel.startGrammarPracticeSession() },
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
            enabled = selectedCount > 0,
            shape = RoundedCornerShape(12.dp),
            contentPadding = PaddingValues(16.dp)
        ) {
            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Start practice symbol")
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Start Practice Round", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ActiveGrammarPracticeSessionScreen(viewModel: PracticeViewModel) {
    val currentItem by viewModel.currentGrammarItem.collectAsState()
    val comp by viewModel.grammarCompletedCount.collectAsState()
    val tot by viewModel.grammarTotalRoundCount.collectAsState()
    val mis by viewModel.grammarMistakesCount.collectAsState()

    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { viewModel.leaveGrammarSession() }) { Icon(Icons.Default.Close, null) }
            Text("Progress: $comp of $tot", style = MaterialTheme.typography.titleMedium)
            Text("Mistakes: $mis", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
        }
        LinearProgressIndicator(progress = { if (tot > 0) comp.toFloat() / tot else 0f }, modifier = Modifier.fillMaxWidth(), strokeCap = StrokeCap.Round)

        if (currentItem != null) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                GrammarFlashcardLayout(viewModel, currentItem!!)
            }
        }
    }
}

@Composable
fun GrammarFlashcardLayout(viewModel: PracticeViewModel, rule: GrammarRule) {
    val isRevealed by viewModel.isGrammarFlashcardRevealed.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("GRAMMAR FLASHCARD", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)

                    // Question Side displays name and description
                    Text(rule.name, fontSize = 24.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                    Text(rule.description, fontSize = 16.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)

                    if (isRevealed) {
                        HorizontalDivider(
                            thickness = DividerDefaults.Thickness,
                            color = Modifier.padding(vertical = 4.dp).let { DividerDefaults.color }
                        )
                        Text("Details", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)

                        // Answer Side displays details
                        Text(
                            text = rule.details,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        if (!isRevealed) {
            Button(onClick = { viewModel.revealGrammarFlashcard() }, modifier = Modifier.fillMaxWidth()) {
                Text("Reveal Details")
            }
        } else {
            Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { viewModel.gradeGrammarFlashcard(false) },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.weight(1f).fillMaxHeight()
                ) {
                    Text("Forgot")
                }
                Button(
                    onClick = { viewModel.gradeGrammarFlashcard(true) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                    modifier = Modifier.weight(1f).fillMaxHeight()
                ) {
                    Text("Correct")
                }
            }
        }
    }
}

@Composable
fun GrammarPracticeSummaryScreen(viewModel: PracticeViewModel) {
    val comp by viewModel.grammarCompletedCount.collectAsState()
    val mis by viewModel.grammarMistakesCount.collectAsState()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier
                .padding(28.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(Color(0xFFE8F5E9), RoundedCornerShape(32.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Success",
                    tint = Color(0xFF2E7D32),
                    modifier = Modifier.size(36.dp)
                )
            }

            Text(
                text = "Session Completed!",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Text(
                text = "You managed to clear all selected grammar rules in your active practice session. Awesome progress!",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            HorizontalDivider()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "GRAMMAR RULES CLEARED", style = MaterialTheme.typography.labelSmall)
                    Text(text = "$comp", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF2E7D32))
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "REPETITIVE MISTAKES", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
                    Text(text = "$mis", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.error)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(onClick = { viewModel.leaveGrammarSession() }, modifier = Modifier.fillMaxWidth()) {
                Text("Back to Setup")
            }
        }
    }
}

private fun lastPracticedFilterText(p: String): String {
    return when (p) {
        "Any" -> "Any"
        "Never" -> "Never practiced"
        "More than a day ago" -> "More than a day ago"
        "More than a week ago" -> "More than a week ago"
        "More than a month ago" -> "More than a month ago"
        else -> p
    }
}
