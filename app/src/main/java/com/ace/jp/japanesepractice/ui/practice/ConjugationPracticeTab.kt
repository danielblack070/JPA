package com.ace.jp.japanesepractice.ui.practice

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ace.jp.japanesepractice.data.model.Type

@Composable
fun ConjugationPracticeTab(viewModel: PracticeViewModel) {
    val isSession by viewModel.isConjugationActiveSession.collectAsState()
    val showSum by viewModel.conjugationShowSummary.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadData() }

    Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        when {
            showSum -> { ConjugationPracticeSummaryScreen(viewModel) }
            isSession -> { ActiveConjugationPracticeSessionScreen(viewModel) }
            else -> { ConjugationPracticeSetupScreen(viewModel) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConjugationPracticeSetupScreen(viewModel: PracticeViewModel) {
    val mode by viewModel.selectedMode.collectAsState()
    val easy by viewModel.easyMode.collectAsState()
    val countInput by viewModel.itemCountInput.collectAsState()
    val typeFilter by viewModel.selectedTypeFilter.collectAsState()
    val lpFilter by viewModel.lastPracticedFilter.collectAsState()
    val conf by viewModel.selectedConfidenceLevels.collectAsState()
    val selectedCount by viewModel.selectedConjugationItemsCount.collectAsState()

    var typeFilterExpanded by remember { mutableStateOf(false) }
    var lastPracticedExpanded by remember { mutableStateOf(false) }
    val isFlashcards = mode == PracticeMode.Flashcards

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Conjugation Practice", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text("Currently Selected Items: $selectedCount", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

        // 1. Selector Mode buttons
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Practice Mode", style = MaterialTheme.typography.labelMedium)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PracticeMode.entries.forEach { m ->
                    val isSel = mode == m
                    Button(
                        onClick = { viewModel.setMode(m) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (isSel) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(if (m == PracticeMode.MultipleChoice) "MC" else m.name, fontSize = 11.sp, maxLines = 1)
                    }
                }
            }
        }

        // 2. Type Dropdown Filter (Disabled for Flashcard lists)
        if (!isFlashcards) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Word Type Filter", style = MaterialTheme.typography.labelMedium)
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(onClick = { typeFilterExpanded = true }, modifier = Modifier.fillMaxWidth()) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(typeFilter)
                            Icon(Icons.Default.ArrowDropDown, null)
                        }
                    }
                    DropdownMenu(expanded = typeFilterExpanded, onDismissRequest = { typeFilterExpanded = false }, modifier = Modifier.fillMaxWidth(0.9f)) {
                        DropdownMenuItem(text = { Text("All") }, onClick = { viewModel.setTypeFilter("All"); typeFilterExpanded = false })
                        DropdownMenuItem(text = { Text("Verb") }, onClick = { viewModel.setTypeFilter("Verb"); typeFilterExpanded = false })
                        DropdownMenuItem(text = { Text("Adjective") }, onClick = { viewModel.setTypeFilter("Adjective"); typeFilterExpanded = false })
                        HorizontalDivider()
                        Type.entries.forEach { tp ->
                            DropdownMenuItem(text = { Text(tp.toDisplayString()) }, onClick = { viewModel.setTypeFilter(tp.toDisplayString()); typeFilterExpanded = false })
                        }
                    }
                }
            }
        }

        // 3. Last Practiced Filter dropdown (Connected to rules)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Last Practiced (Filter on Rules)", style = MaterialTheme.typography.labelMedium)
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(onClick = { lastPracticedExpanded = true }, modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(lpFilter)
                        Icon(Icons.Default.ArrowDropDown, null)
                    }
                }
                DropdownMenu(expanded = lastPracticedExpanded, onDismissRequest = { lastPracticedExpanded = false }) {
                    listOf("Any", "More than a day ago", "More than a week ago", "More than a month ago", "Never practiced").forEach { option ->
                        DropdownMenuItem(text = { Text(option) }, onClick = { viewModel.setLastPracticedFilter(option); lastPracticedExpanded = false })
                    }
                }
            }
        }

        // 4. Confidence levels
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("Confidence: ", style = MaterialTheme.typography.bodySmall)
            Button(onClick = { viewModel.setConfidenceLevels(if (conf.size == 6) emptySet() else setOf(0,1,2,3,4,5)) }, contentPadding = PaddingValues(horizontal = 8.dp)) {
                Text(if (conf.size == 6) "None" else "All", fontSize = 10.sp)
            }
            Row(modifier = Modifier.weight(1f).horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                (0..5).forEach { lvl ->
                    val has = lvl in conf
                    FilterChip(selected = has, onClick = { viewModel.setConfidenceLevels(if (has) conf - lvl else conf + lvl) }, label = { Text("${lvl * 20}%") })
                }
            }
        }

        // 5. Item input & Easy Mode
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(value = countInput, onValueChange = { viewModel.setItemCountInput(it) }, label = { Text("Items Count") }, modifier = Modifier.weight(1f))
            if (!isFlashcards) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Easy Mode", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.width(4.dp))
                    Switch(checked = easy, onCheckedChange = { viewModel.setEasyMode(it) })
                }
            }
        }

        Button(onClick = { viewModel.startConjugationPracticeSession() }, modifier = Modifier.fillMaxWidth().padding(top = 16.dp), enabled = selectedCount > 0) {
            Icon(Icons.Default.PlayArrow, null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Start Conjugation Round", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ActiveConjugationPracticeSessionScreen(viewModel: PracticeViewModel) {
    val mode by viewModel.selectedMode.collectAsState()
    val easy by viewModel.easyMode.collectAsState()
    val currentItem by viewModel.currentConjugationItem.collectAsState()
    val comp by viewModel.conjugationCompletedCount.collectAsState()
    val tot by viewModel.conjugationTotalRoundCount.collectAsState()
    val mis by viewModel.conjugationMistakesCount.collectAsState()

    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { viewModel.leaveConjugationSession() }) { Icon(Icons.Default.Close, null) }
            Text("Progress: $comp of $tot", style = MaterialTheme.typography.titleMedium)
            Text("Mistakes: $mis", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
        }
        LinearProgressIndicator(progress = { if (tot > 0) comp.toFloat() / tot else 0f }, modifier = Modifier.fillMaxWidth(), strokeCap = StrokeCap.Round)

        if (currentItem != null) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                when (mode) {
                    PracticeMode.Flashcards -> ConjugationFlashcardLayout(viewModel, currentItem as ConjugationQuizItem.FlashcardItem)
                    PracticeMode.MultipleChoice -> ConjugationMultipleChoiceLayout(viewModel, currentItem as ConjugationQuizItem.InteractiveItem, easy)
                    PracticeMode.Typing -> ConjugationTypingLayout(viewModel, currentItem as ConjugationQuizItem.InteractiveItem, easy)
                }
            }
        }
    }
}

@Composable
fun ConjugationFlashcardLayout(viewModel: PracticeViewModel, item: ConjugationQuizItem.FlashcardItem) {
    val isRevealed by viewModel.isConjugationFlashcardRevealed.collectAsState()

    Card(modifier = Modifier.fillMaxWidth().height(420.dp), shape = RoundedCornerShape(16.dp)) {
        Column(modifier = Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.SpaceBetween, horizontalAlignment = Alignment.CenterHorizontally) {
            Text("FLASHCARD PRACTICE", style = MaterialTheme.typography.labelSmall)
            Text(item.masterRule.name, fontSize = 28.sp, fontWeight = FontWeight.Bold)

            if (!isRevealed) {
                Button(onClick = { viewModel.revealConjugationFlashcard() }, modifier = Modifier.fillMaxWidth()) { Text("Reveal Subrules") }
            } else {
                Column(modifier = Modifier.fillMaxWidth().weight(1f).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Subrules pattern values:", fontWeight = FontWeight.Bold)
                    item.subRules.forEach { sub ->
                        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant), modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text("Type: ${sub.type.toDisplayString()}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text("Rule Pattern: ${sub.originalEnding ?: "Any"} ➔ ${sub.newEnding}", fontSize = 12.sp)
                                Text("isUnique: ${if (sub.isUnique) "Yes" else "No"}", fontSize = 11.sp)
                            }
                        }
                    }
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { viewModel.gradeConjugationFlashcard(false) }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error), modifier = Modifier.weight(1f)) { Text("Forgot") }
                    Button(onClick = { viewModel.gradeConjugationFlashcard(true) }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)), modifier = Modifier.weight(1f)) { Text("Correct") }
                }
            }
        }
    }
}

@Composable
fun ConjugationMultipleChoiceLayout(viewModel: PracticeViewModel, item: ConjugationQuizItem.InteractiveItem, easy: Boolean) {
    val options by viewModel.conjugationMCOptions.collectAsState()
    val sel by viewModel.selectedConjugationMCOption.collectAsState()
    val isCh by viewModel.isConjugationAnswerChecked.collectAsState()
    val isCorr by viewModel.isConjugationCorrect.collectAsState()

    Card(modifier = Modifier.fillMaxWidth().wrapContentHeight()) {
        Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("CHOOSE CORRECT CONJUGATION", style = MaterialTheme.typography.labelSmall)
            Text(item.word.japanese, fontSize = 32.sp, fontWeight = FontWeight.Bold)

            // Dynamic read block shows reading on checked or easy mode active
            if ((easy || isCh) && !item.word.reading.isNullOrEmpty()) {
                Text("(${item.word.reading})", fontSize = 18.sp, fontFamily = FontFamily.Monospace, color = MaterialTheme.colorScheme.primary)
            }
            if (isCh) {
                Text("Type: ${item.word.type.toDisplayString()}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            Text("Conjugation Rule of: ${item.masterRule.name}", fontSize = 14.sp, color = MaterialTheme.colorScheme.secondary)

            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                options.forEach { opt ->
                    val corr = opt.japanese == item.conjugatedWord.japanese
                    val isS = sel?.japanese == opt.japanese
                    val col = when {
                        isCh && corr -> ButtonDefaults.buttonColors(containerColor = Color(0xFFE8F5E9), contentColor = Color(0xFF2E7D32))
                        isCh && isS -> ButtonDefaults.buttonColors(containerColor = Color(0xFFFFEBEE), contentColor = Color(0xFFC62828))
                        else -> ButtonDefaults.outlinedButtonColors()
                    }
                    OutlinedButton(
                        onClick = { if (!isCh) viewModel.checkConjugationMCOption(opt) },
                        colors = col,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val reading = if ((easy || isCh) && !opt.reading.isNullOrEmpty()) " (${opt.reading})" else ""
                        Text("${opt.japanese}$reading")
                    }
                }
            }

            if (isCh) {
                val exp = if (!item.conjugatedWord.reading.isNullOrEmpty()) "${item.conjugatedWord.japanese} (${item.conjugatedWord.reading})" else item.conjugatedWord.japanese
                Text(
                    text = if (isCorr) "Correct!" else "Incorrect. Expected: $exp",
                    color = if (isCorr) Color(0xFF2E7D32) else Color(0xFFC62828),
                    fontWeight = FontWeight.Bold
                )
                Button(onClick = { viewModel.moveConjugationToNext() }, modifier = Modifier.fillMaxWidth()) { Text("Next") }
            }
        }
    }
}

@Composable
fun ConjugationTypingLayout(viewModel: PracticeViewModel, item: ConjugationQuizItem.InteractiveItem, easy: Boolean) {
    val isCh by viewModel.isConjugationAnswerChecked.collectAsState()
    val isCorr by viewModel.isConjugationCorrect.collectAsState()
    var input by remember { mutableStateOf("") }
    val focus = LocalFocusManager.current

    LaunchedEffect(item) { input = "" }

    Card(modifier = Modifier.fillMaxWidth().wrapContentHeight()) {
        Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("TYPE CORRECT CONJUGATION", style = MaterialTheme.typography.labelSmall)
            Text(item.word.japanese, fontSize = 32.sp, fontWeight = FontWeight.Bold)

            // Dynamic reads
            if ((easy || isCh) && !item.word.reading.isNullOrEmpty()) {
                Text("(${item.word.reading})", fontSize = 18.sp, color = MaterialTheme.colorScheme.primary)
            }
            if (isCh) {
                Text("Type: ${item.word.type.toDisplayString()}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            Text("Desired Rule: ${item.masterRule.name}", fontSize = 14.sp)

            OutlinedTextField(
                value = input,
                onValueChange = { if (!isCh) input = it },
                placeholder = { Text(if (easy) "Typing pattern: Reading OR JP + space + read" else "Typing pattern: Japanese + space + reading") },
                singleLine = true,
                enabled = !isCh,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { focus.clearFocus(); viewModel.checkConjugationTypingAnswer(input) }),
                modifier = Modifier.fillMaxWidth()
            )

            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text("Answer Format Rules:", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                    Text("• Format: Japanese + [space] + Reading. (e.g. \"来ます きます\")", fontSize = 10.sp)
                    if (easy) Text("• Easy Mode: You can also just type only the reading kana! (e.g. \"きます\")", fontSize = 10.sp, color = Color(0xFF2E7D32))
                }
            }

            if (!isCh) {
                Button(onClick = { focus.clearFocus(); viewModel.checkConjugationTypingAnswer(input) }, modifier = Modifier.fillMaxWidth()) { Text("Submit") }
            } else {
                val exp = if (!item.conjugatedWord.reading.isNullOrEmpty()) "${item.conjugatedWord.japanese} ${item.conjugatedWord.reading}" else item.conjugatedWord.japanese
                Text(
                    text = if (isCorr) "Correct!" else "Incorrect. Expected: $exp",
                    color = if (isCorr) Color(0xFF2E7D32) else Color(0xFFC62828),
                    fontWeight = FontWeight.Bold
                )
                Button(onClick = { viewModel.moveConjugationToNext() }, modifier = Modifier.fillMaxWidth()) { Text("Next") }
            }
        }
    }
}

@Composable
fun ConjugationPracticeSummaryScreen(viewModel: PracticeViewModel) {
    val tot by viewModel.conjugationTotalRoundCount.collectAsState()
    val comp by viewModel.conjugationCompletedCount.collectAsState()
    val mis by viewModel.conjugationMistakesCount.collectAsState()

    Card(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        Column(modifier = Modifier.padding(24.dp).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Session Completed!", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            HorizontalDivider()
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Total Target Round Count:")
                Text("$tot Items", fontWeight = FontWeight.Bold)
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Successfully Completed:")
                Text("$comp", fontWeight = FontWeight.Bold, color = Color(0xFF4CAF50))
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Repetitive Mistakes: ")
                Text("$mis", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { viewModel.leaveConjugationSession() }, modifier = Modifier.fillMaxWidth()) { Text("Back to Setup") }
        }
    }
}