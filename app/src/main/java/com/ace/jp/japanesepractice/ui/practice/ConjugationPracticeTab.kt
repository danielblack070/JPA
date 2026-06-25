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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ace.jp.japanesepractice.data.model.Type

@Composable
fun ConjugationPracticeTab(viewModel: PracticeViewModel) {
    val isSession by viewModel.isConjugationActiveSession.collectAsState()
    val showSum by viewModel.conjugationShowSummary.collectAsState()

    // Refresh database whenever navigating here or returning from a completed session
    LaunchedEffect(isSession, showSum) {
        if (!isSession && !showSum) {
            viewModel.loadData()
        }
    }

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
                text = "Conjugation Practice Setup",
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
                    PracticeMode.entries.forEach { m ->
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
                            Text(text = displayTxt, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, softWrap = true, textAlign = TextAlign.Center)
                        }
                    }
                }
            }

            // Type Dropdown Filter (Disabled for Flashcard lists) (Uses UI matching the all words list screen)
            if (!isFlashcards) {
                Box(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                    OutlinedButton(
                        onClick = { typeFilterExpanded = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Type: $typeFilter")
                        Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = "Select Filters")
                    }

                    DropdownMenu(
                        expanded = typeFilterExpanded,
                        onDismissRequest = { typeFilterExpanded = false },
                        modifier = Modifier.fillMaxWidth(0.9f)
                    ) {
                        DropdownMenuItem(
                            text = { Text("All") },
                            onClick = {
                                viewModel.setTypeFilter("All")
                                typeFilterExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Verb") },
                            onClick = {
                                viewModel.setTypeFilter("Verb")
                                typeFilterExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Adjective") },
                            onClick = {
                                viewModel.setTypeFilter("Adjective")
                                typeFilterExpanded = false
                            }
                        )
                        HorizontalDivider()
                        Type.entries.forEach { tp ->
                            DropdownMenuItem(
                                text = { Text(tp.toDisplayString()) },
                                onClick = {
                                    viewModel.setTypeFilter(tp.toDisplayString())
                                    typeFilterExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Last Practiced Filter dropdown (Connected to rules) (Uses UI matching the all words list screen)
            Box(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
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

            // Confidence levels (Uses UI matching the grammar practice screen)
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
                if (!isFlashcards) {
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
                            text = { Text("Display Reading in questions and accept them as answers") },
                            confirmButton = {
                                Button(onClick = { showEasyModeHelp = false }) { Text("OK") }
                            }
                        )
                    }
                }
            }
        }

        // Start Practice Trigger - floating/pinned at the bottom!
        Button(
            onClick = { viewModel.startConjugationPracticeSession() },
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
                    Text(item.masterRule.name, fontSize = 28.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)

                    if (isRevealed) {
                        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Form pattern values:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                            item.subRules.forEach { sub ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text("Type: ${sub.type.toDisplayString()}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        Text("Rule Pattern: ${sub.originalEnding ?: "Any"} ➔ ${sub.newEnding}", fontSize = 12.sp)
                                        Text("isUnique: ${if (sub.isUnique) "Yes" else "No"}", fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        if (!isRevealed) {
            Button(onClick = { viewModel.revealConjugationFlashcard() }, modifier = Modifier.fillMaxWidth()) {
                Text("Reveal Rules")
            }
        } else {
            Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { viewModel.gradeConjugationFlashcard(false) },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.weight(1f).fillMaxHeight()
                ) {
                    Text("Forgot")
                }
                Button(
                    onClick = { viewModel.gradeConjugationFlashcard(true) },
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
fun ConjugationMultipleChoiceLayout(viewModel: PracticeViewModel, item: ConjugationQuizItem.InteractiveItem, easy: Boolean) {
    val options by viewModel.conjugationMCOptions.collectAsState()
    val sel by viewModel.selectedConjugationMCOption.collectAsState()
    val isCh by viewModel.isConjugationAnswerChecked.collectAsState()
    val isCorr by viewModel.isConjugationCorrect.collectAsState()

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
                    Text("CHOOSE CORRECT CONJUGATION", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                    Text(item.word.japanese, fontSize = 32.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)

                    // Dynamic read block shows reading on checked or easy mode active
                    if ((easy || isCh) && !item.word.reading.isNullOrEmpty()) {
                        Text("(${item.word.reading})", fontSize = 18.sp, fontFamily = FontFamily.Monospace, color = MaterialTheme.colorScheme.primary)
                    }
                    if (isCh) {
                        Text("Type: ${item.word.type.toDisplayString()}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Text("Conjugation Form of: ${item.masterRule.name}", fontSize = 14.sp, color = MaterialTheme.colorScheme.secondary)

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
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        if (isCh) {
            val exp = if (!item.conjugatedWord.reading.isNullOrEmpty()) "${item.conjugatedWord.japanese} (${item.conjugatedWord.reading})" else item.conjugatedWord.japanese
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (isCorr) "Correct!" else "Incorrect. Expected: $exp",
                    color = if (isCorr) Color(0xFF2E7D32) else Color(0xFFC62828),
                    fontWeight = FontWeight.Bold
                )
                Button(onClick = { viewModel.moveConjugationToNext() }, modifier = Modifier.fillMaxWidth()) {
                    Text("Next Question")
                }
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
                    Text("TYPE CORRECT CONJUGATION", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                    Text(item.word.japanese, fontSize = 32.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)

                    // Dynamic reads
                    if ((easy || isCh) && !item.word.reading.isNullOrEmpty()) {
                        Text("(${item.word.reading})", fontSize = 18.sp, color = MaterialTheme.colorScheme.primary)
                    }
                    if (isCh) {
                        Text("Type: ${item.word.type.toDisplayString()}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Text("Desired Form: ${item.masterRule.name}", fontSize = 14.sp)

                    OutlinedTextField(
                        value = input,
                        onValueChange = { if (!isCh) input = it },
                        placeholder = { Text(if (easy) "Typing pattern: Reading OR Japanese + space + Reading" else "Typing pattern: Japanese + space + Reading") },
                        singleLine = true,
                        enabled = !isCh,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(onSend = { focus.clearFocus(); viewModel.checkConjugationTypingAnswer(input) }),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Answer Format Rules:", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                            Text("• Format: Japanese + [space] + Reading. (e.g. \"来ます きます\")", fontSize = 10.sp)
                            if (easy) Text("• Easy Mode: You can also just type only the reading kana! (e.g. \"きます\")", fontSize = 10.sp, color = Color(0xFF2E7D32))
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        if (!isCh) {
            Button(onClick = { focus.clearFocus(); viewModel.checkConjugationTypingAnswer(input) }, modifier = Modifier.fillMaxWidth()) {
                Text("Submit")
            }
        } else {
            val exp = if (!item.conjugatedWord.reading.isNullOrEmpty()) "${item.conjugatedWord.japanese} ${item.conjugatedWord.reading}" else item.conjugatedWord.japanese
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (isCorr) "Correct!" else "Incorrect. Expected: $exp",
                    color = if (isCorr) Color(0xFF2E7D32) else Color(0xFFC62828),
                    fontWeight = FontWeight.Bold
                )
                Button(onClick = { viewModel.moveConjugationToNext() }, modifier = Modifier.fillMaxWidth()) {
                    Text("Next Question")
                }
            }
        }
    }
}

@Composable
fun ConjugationPracticeSummaryScreen(viewModel: PracticeViewModel) {
    val comp by viewModel.conjugationCompletedCount.collectAsState()
    val mis by viewModel.conjugationMistakesCount.collectAsState()

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
                    imageVector = Icons.Default.PlayArrow, // Reused icon or check
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
                text = "You managed to clear all selected conjugation rules in your active practice session. Awesome progress!",
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
                    Text(text = "CONJUGATIONS COMPLETED", style = MaterialTheme.typography.labelSmall)
                    Text(text = "$comp", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF2E7D32))
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "REPETITIVE MISTAKES", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
                    Text(text = "$mis", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.error)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(onClick = { viewModel.leaveConjugationSession() }, modifier = Modifier.fillMaxWidth()) {
                Text("Back to Setup")
            }
        }
    }
}
