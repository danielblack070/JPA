package com.ace.jp.japanesepractice.ui.practice

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
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
import androidx.compose.ui.platform.LocalFocusManager
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
    val mode by viewModel.selectedMode.collectAsState()
    val easy by viewModel.easyMode.collectAsState()
    val countInput by viewModel.itemCountInput.collectAsState()
    val selectedDirection by viewModel.selectedDirection.collectAsState()
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Grammar Practice Setup",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            // Live Selected Count Indicator matching Vocabulary tab exactly
            Text(
                text = "Currently Selected Items for Practice: $selectedCount",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(vertical = 2.dp)
            )

            // 1. Selector Mode buttons
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = "Practice Mode", style = MaterialTheme.typography.labelMedium)
                Row(
                    modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PracticeMode.entries.filterNot {it == PracticeMode.MultipleChoice}
                        .forEach { m -> m != PracticeMode.MultipleChoice
                        val isSel = mode == m
                        Button(
                            onClick = { viewModel.setMode(m) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = if (isSel) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            modifier = Modifier.weight(1f).fillMaxHeight()
                        ) {
                            val displayTxt = if (m == PracticeMode.MultipleChoice) "Multiple Choice" else if (m == PracticeMode.Flashcards) "Flashcard" else m.name
                            Text(text = displayTxt, fontSize = 12.sp, fontWeight = FontWeight.Bold, softWrap = true, textAlign = TextAlign.Center)
                        }
                    }
                }
            }

            // 2. Direction Selector (Only if NOT Multiple Choice)
            if (mode != PracticeMode.MultipleChoice) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "Direction Selector", style = MaterialTheme.typography.labelMedium)
                    Row(
                        modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        PracticeDirection.entries.forEach { direction ->
                            val isSelected = selectedDirection == direction
                            Button(
                                onClick = { viewModel.setDirection(direction) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                modifier = Modifier.weight(1f).fillMaxHeight()
                            ) {
                                val displayTxt = if (direction == PracticeDirection.JapaneseToEnglish) "Japanese ➔ English" else "English ➔ Japanese"
                                Text(text = displayTxt, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // 3. Last Practiced Filter dropdown
            Box(modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)) {
                OutlinedButton(
                    onClick = { lastPracticedExpanded = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Last Practiced Filter: $lpFilter")
                    Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = "Select Period")
                }

                DropdownMenu(
                    expanded = lastPracticedExpanded,
                    onDismissRequest = { lastPracticedExpanded = false },
                    modifier = Modifier.fillMaxWidth(0.9f)
                ) {
                    listOf(
                        "Any", "More than a day ago", "More than a week ago",
                        "More than a month ago", "Never practiced"
                    ).forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                viewModel.setLastPracticedFilter(option)
                                lastPracticedExpanded = false
                            }
                        )
                    }
                }
            }

            // 4. Confidence levels
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
                    modifier = Modifier.fillMaxWidth(),
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

            // 5. Item input & Easy Mode
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

                var showEasyModeHelp by remember { mutableStateOf(false) }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(text = "Easy Mode", style = MaterialTheme.typography.bodyMedium)
                    IconButton(onClick = { showEasyModeHelp = true }, modifier = Modifier.size(24.dp)) {
                        Text("❓", style = MaterialTheme.typography.bodySmall)
                    }
                    Switch(
                        checked = easy,
                        onCheckedChange = { viewModel.setEasyMode(it) }
                    )
                }

                if (showEasyModeHelp) {
                    AlertDialog(
                        onDismissRequest = { showEasyModeHelp = false },
                        title = { Text("Easy Mode Help") },
                        text = {
                            Text(
                                "Enables helpful readings pairing with grammar rules/answers, and allows typing reading kana as accepted answers."
                            )
                        },
                        confirmButton = {
                            Button(onClick = { showEasyModeHelp = false }) { Text("OK") }
                        }
                    )
                }
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
    val mode by viewModel.selectedMode.collectAsState()
    val easy by viewModel.easyMode.collectAsState()
    val currentItem by viewModel.currentGrammarItem.collectAsState()
    val comp by viewModel.grammarCompletedCount.collectAsState()
    val tot by viewModel.grammarTotalRoundCount.collectAsState()
    val mis by viewModel.grammarMistakesCount.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { viewModel.leaveGrammarSession() }) { Icon(Icons.Default.Close, null) }
            Text("Progress: $comp of $tot", style = MaterialTheme.typography.titleMedium)
            Text("Mistakes: $mis", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
        }
        LinearProgressIndicator(
            progress = { if (tot > 0) comp.toFloat() / tot else 0f },
            modifier = Modifier.fillMaxWidth(),
            strokeCap = StrokeCap.Round
        )

        if (currentItem != null) {
            Box(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                when (mode) {
                    PracticeMode.Flashcards -> GrammarFlashcardLayout(viewModel, currentItem!!, easy)
                    PracticeMode.MultipleChoice -> GrammarMultipleChoiceLayout(viewModel, currentItem!!)
                    PracticeMode.Typing -> GrammarTypingLayout(viewModel, currentItem!!, easy)
                }
            }
        }
    }
}

@Composable
fun GrammarFlashcardLayout(viewModel: PracticeViewModel, item: GrammarRule, easy: Boolean) {
    val isRevealed by viewModel.isGrammarFlashcardRevealed.collectAsState()
    val direction by viewModel.selectedDirection.collectAsState()

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
                    Text("FLASHCARD PRACTICE", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)

                    Text(item.name, fontSize = 24.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)

                    Spacer(modifier = Modifier.height(12.dp))

                    if (!isRevealed) {
                        if (direction == PracticeDirection.EnglishToJapanese) {
                            Text("English Rule:", fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
                            Text(item.englishRule, fontSize = 20.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
                        } else {
                            Text("Japanese Rule:", fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
                            Text(item.japaneseRule, fontSize = 20.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
                            if (easy && !item.readingRule.isNullOrEmpty()) {
                                Text("(${item.readingRule})", fontSize = 16.sp, color = MaterialTheme.colorScheme.primary, textAlign = TextAlign.Center)
                            }
                        }
                    } else {
                        if (direction == PracticeDirection.EnglishToJapanese) {
                            Text("Japanese Rule:", fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
                            Text(item.japaneseRule, fontSize = 20.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
                            if (easy && !item.readingRule.isNullOrEmpty()) {
                                Text("(${item.readingRule})", fontSize = 16.sp, color = MaterialTheme.colorScheme.primary, textAlign = TextAlign.Center)
                            }
                        } else {
                            Text("English Rule:", fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
                            Text(item.englishRule, fontSize = 20.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
                        }

                        if (!item.notes.isNullOrEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("Notes:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                                    Text(item.notes, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        if (!isRevealed) {
            Button(onClick = { viewModel.revealGrammarFlashcard() }, modifier = Modifier.fillMaxWidth()) {
                Text("Reveal Answer")
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
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
fun GrammarMultipleChoiceLayout(viewModel: PracticeViewModel, item: GrammarRule) {
    val isCh by viewModel.isGrammarAnswerChecked.collectAsState()
    val isCorr by viewModel.isGrammarCorrect.collectAsState()
    val options by viewModel.grammarMCOptions.collectAsState()
    val sel by viewModel.selectedGrammarMCOption.collectAsState()

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
                    Text("CHOOSE CORRECT JAPANESE GRAMMAR RULE FOR:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                    Text(item.englishRule, fontSize = 24.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)

                    Spacer(modifier = Modifier.height(8.dp))

                    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        options.forEach { opt ->
                            val corr = opt.id == item.id
                            val isS = sel?.id == opt.id
                            val col = when {
                                isCh && corr -> ButtonDefaults.buttonColors(containerColor = Color(0xFFE8F5E9), contentColor = Color(0xFF2E7D32))
                                isCh && isS -> ButtonDefaults.buttonColors(containerColor = Color(0xFFFFEBEE), contentColor = Color(0xFFC62828))
                                else -> ButtonDefaults.outlinedButtonColors()
                            }
                            OutlinedButton(
                                onClick = { if (!isCh) viewModel.checkGrammarMCOption(opt) },
                                colors = col,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(opt.japaneseRule)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        if (isCh) {
            val exp = item.japaneseRule
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (isCorr) "Correct!" else "Incorrect. Expected: $exp",
                    color = if (isCorr) Color(0xFF2E7D32) else Color(0xFFC62828),
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Button(onClick = { viewModel.moveGrammarToNext() }, modifier = Modifier.fillMaxWidth()) {
                    Text("Next Question")
                }
            }
        }
    }
}

@Composable
fun GrammarTypingLayout(viewModel: PracticeViewModel, item: GrammarRule, easy: Boolean) {
    val example by viewModel.currentGrammarExample.collectAsState()
    val isCh by viewModel.isGrammarAnswerChecked.collectAsState()
    val isCorr by viewModel.isGrammarCorrect.collectAsState()
    val direction by viewModel.selectedDirection.collectAsState()
    var input by remember { mutableStateOf("") }
    val focus = LocalFocusManager.current

    LaunchedEffect(item, example) { input = "" }

    Column(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (example != null) {
                val ex = example!!
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
                        Text("TYPE TRANSLATION FOR EXAMPLE SENTENCE", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)

                        Text("Grammar Rule: ${item.name}", fontSize = 16.sp, fontWeight = FontWeight.Bold)

                        Spacer(modifier = Modifier.height(8.dp))

                        if (direction == PracticeDirection.EnglishToJapanese) {
                            Text("English Example:", fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
                            Text(ex.english, fontSize = 20.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)

                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = input,
                                onValueChange = { if (!isCh) input = it },
                                placeholder = { Text(if (easy && !ex.reading.isNullOrEmpty()) "Type Japanese OR Reading" else "Type Japanese sentence") },
                                singleLine = false,
                                maxLines = 4,
                                enabled = !isCh,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                                keyboardActions = KeyboardActions(onSend = { focus.clearFocus(); viewModel.checkGrammarTypingAnswer(input) }),
                                modifier = Modifier.fillMaxWidth()
                            )
                        } else {
                            val qText = if (easy && !ex.reading.isNullOrEmpty()) ex.reading else ex.japanese
                            val label = if (easy && !ex.reading.isNullOrEmpty()) "Reading Example:" else "Japanese Example:"

                            Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
                            Text(qText, fontSize = 20.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)

                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = input,
                                onValueChange = { if (!isCh) input = it },
                                placeholder = { Text("Translate into English") },
                                singleLine = false,
                                maxLines = 4,
                                enabled = !isCh,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                                keyboardActions = KeyboardActions(onSend = { focus.clearFocus(); viewModel.checkGrammarTypingAnswer(input) }),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("Notes / Help:", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                                if (direction == PracticeDirection.EnglishToJapanese) {
                                    Text("• Answer should match the Japanese example exactly.", fontSize = 10.sp)
                                    if (easy && !ex.reading.isNullOrEmpty()) {
                                        Text("• Easy Mode: You can also type the reading of the example!", fontSize = 10.sp, color = Color(0xFF2E7D32))
                                    }
                                } else {
                                    Text("• Answer should match the English translation exactly.", fontSize = 10.sp)
                                }
                            }
                        }
                    }
                }
            } else {
                Text("No example sentence found for rule ${item.name}", color = MaterialTheme.colorScheme.error)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        if (example != null) {
            val ex = example!!
            if (!isCh) {
                Button(onClick = { focus.clearFocus(); viewModel.checkGrammarTypingAnswer(input) }, modifier = Modifier.fillMaxWidth()) {
                    Text("Submit")
                }
            } else {
                val exp = if (direction == PracticeDirection.EnglishToJapanese) {
                    if (easy && !ex.reading.isNullOrEmpty()) "${ex.japanese} (Reading: ${ex.reading})" else ex.japanese
                } else {
                    ex.english
                }
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (isCorr) "Correct!" else "Incorrect. Expected: $exp",
                        color = if (isCorr) Color(0xFF2E7D32) else Color(0xFFC62828),
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Button(onClick = { viewModel.moveGrammarToNext() }, modifier = Modifier.fillMaxWidth()) {
                        Text("Next Question")
                    }
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
                    Text(text = "GRAMMAR RULES COMPLETED", style = MaterialTheme.typography.labelSmall)
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
