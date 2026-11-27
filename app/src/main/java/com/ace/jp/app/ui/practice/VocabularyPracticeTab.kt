package com.ace.jp.app.ui.practice

import com.ace.jp.app.ui.AutoScalingText
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ace.jp.app.data.model.Type
import com.ace.jp.app.data.model.Word
import com.ace.jp.app.data.model.formatDisplay

@Composable
fun VocabularyPracticeTab(viewModel: PracticeViewModel) {
    val isActiveSession by viewModel.isActiveSession.collectAsState()
    val showSummary by viewModel.showSummary.collectAsState()

    // Automatically load/refresh latest database elements when navigating to this tab or returning
    LaunchedEffect(isActiveSession, showSummary) {
        if (!isActiveSession && !showSummary) {
            viewModel.loadData()
        }
    }

    Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        when {
            showSummary -> {
                PracticeSummaryScreen(viewModel = viewModel)
            }
            isActiveSession -> {
                ActivePracticeSessionScreen(viewModel = viewModel)
            }
            else -> {
                PracticeSetupScreen(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PracticeSetupScreen(viewModel: PracticeViewModel) {
    val selectedMode by viewModel.selectedMode.collectAsState()
    val itemCountInput by viewModel.itemCountInput.collectAsState()
    val selectedDirection by viewModel.selectedDirection.collectAsState()
    val selectedFilters by viewModel.selectedTypeFilter.collectAsState()
    val lastPracticedFilter by viewModel.lastPracticedFilter.collectAsState()
    val selectedItemsCount by viewModel.selectedItemsCount.collectAsState()
    val selectedConfidenceLevels by viewModel.selectedConfidenceLevels.collectAsState()

    LaunchedEffect(selectedMode) {
        if (selectedMode == PracticeMode.MultipleChoice) {
            viewModel.setMode(PracticeMode.Flashcards)
        }
    }
    var dropdownExpanded by remember { mutableStateOf(false) }
    var lastPracticedDropdownExpanded by remember { mutableStateOf(false) }

    val allSubFilters = remember { Type.entries.map { it.toDisplayString() }.toSet() }

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
                text = "Vocabulary Practice Setup",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            // Live Selected Count Indicator
            Text(
                text = "Currently Selected Items for Practice: $selectedItemsCount",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(vertical = 2.dp)
            )

            // 1. Practice Mode Selection
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = "Practice Mode", style = MaterialTheme.typography.labelMedium)
                Row(
                    modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PracticeMode.entries.filter { it != PracticeMode.MultipleChoice }.forEach { mode ->
                        val isSelected = selectedMode == mode
                        Button(
                            onClick = { viewModel.setMode(mode) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            modifier = Modifier.weight(1f).fillMaxHeight()
                        ) {
                            val displayTxt = if (mode == PracticeMode.MultipleChoice) "Multiple Choice" else if (mode == PracticeMode.Flashcards) "Flashcard" else mode.name
                            Text(text = displayTxt, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, softWrap = true, textAlign = TextAlign.Center)
                        }
                    }
                }
            }

            // 2. Practice Direction Selection
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
                            val displayTxt = if (direction == PracticeDirection.EnglishToJapanese) "English ➔ Japanese" else "Japanese ➔ English"
                            Text(text = displayTxt, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            // Word Type Filter Selector (Uses UI matching the all words list screen)
            Box(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                OutlinedButton(
                    onClick = { dropdownExpanded = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val buttonText = when {
                        selectedFilters.containsAll(allSubFilters) -> "Type: All"
                        selectedFilters.isEmpty() -> "Type: None"
                        else -> "Type: ${selectedFilters.joinToString(", ")}"
                    }

                    Text(text = buttonText, modifier = Modifier.weight(1f))
                    Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = "Select Filters")
                }

                DropdownMenu(
                    expanded = dropdownExpanded,
                    onDismissRequest = { dropdownExpanded = false },
                    modifier = Modifier.fillMaxWidth(0.9f)
                ) {
                    // 1. "All" Option (No Checkbox)
                    DropdownMenuItem(
                        text = {
                            val textLabel = if (selectedFilters.containsAll(allSubFilters)) "Deselect All" else "Select All"
                            Text(textLabel, style = MaterialTheme.typography.bodyMedium)
                        },
                        onClick = { viewModel.toggleTypeFilter("All") }
                    )

                    // 2. "Verb" Group Option (No Checkbox)
                    DropdownMenuItem(
                        text = { Text("Toggle Verbs (U, Ru, Irr)") },
                        onClick = { viewModel.toggleTypeFilter("Verb") }
                    )

                    // 3. "Adjective" Group Option (No Checkbox)
                    DropdownMenuItem(
                        text = { Text("Toggle Adjectives (I, Na, Irr)") },
                        onClick = { viewModel.toggleTypeFilter("Adjective") }
                    )

                    HorizontalDivider()

                    // 4. Individual Type Entries (With Checkboxes)
                    Type.entries.forEach { option ->
                        val displayString = option.toDisplayString()
                        DropdownMenuItem(
                            text = { Text(displayString) },
                            trailingIcon = {
                                Checkbox(
                                    checked = selectedFilters.contains(displayString),
                                    onCheckedChange = null
                                )
                            },
                            onClick = { viewModel.toggleTypeFilter(displayString) }
                        )
                    }
                }
            }

            // Last Practiced Filter Selector (Uses UI matching the all words list screen)
            Box(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                OutlinedButton(
                    onClick = { lastPracticedDropdownExpanded = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Last Practiced Filter: $lastPracticedFilter")
                    Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = "Select Period")
                }

                DropdownMenu(
                    expanded = lastPracticedDropdownExpanded,
                    onDismissRequest = { lastPracticedDropdownExpanded = false },
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
                                lastPracticedDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            // Confidence Filter Selector (Uses UI matching the grammar practice screen)
            Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Selected Confidence Levels", style = MaterialTheme.typography.labelMedium)
                    Button(
                        onClick = {
                            val newLevels = if (selectedConfidenceLevels.size == 6) emptySet() else setOf(0, 1, 2, 3, 4, 5)
                            viewModel.setConfidenceLevels(newLevels)
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
                                val newLevels = if (lvl in selectedConfidenceLevels) {
                                    selectedConfidenceLevels - lvl
                                } else {
                                    selectedConfidenceLevels + lvl
                                }
                                viewModel.setConfidenceLevels(newLevels)
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

            // 6. Input Configuration field
            OutlinedTextField(
                value = itemCountInput,
                onValueChange = { viewModel.setItemCountInput(it) },
                label = { Text("Items per round") },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Start Practice Trigger - floating/pinned at the bottom!
        Button(
            onClick = { viewModel.startPracticeSession() },
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
            enabled = selectedItemsCount > 0,
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
fun ActivePracticeSessionScreen(viewModel: PracticeViewModel) {
    val selectedMode by viewModel.selectedMode.collectAsState()
    val appSettings by viewModel.appSettings.collectAsState()
    val currentWord by viewModel.currentWord.collectAsState()
    val completedCount by viewModel.completedCount.collectAsState()
    val totalRoundCount by viewModel.totalRoundCount.collectAsState()
    val mistakesCount by viewModel.mistakesCount.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Active Sub Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { viewModel.leaveSession() }) {
                Icon(imageVector = Icons.Default.Close, contentDescription = "Close session")
            }
            Text(
                text = "Progress: $completedCount of $totalRoundCount",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Mistakes: $mistakesCount",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
                fontWeight = FontWeight.Bold
            )
        }

        // Progress bar indicator
        val progress = if (totalRoundCount > 0) completedCount.toFloat() / totalRoundCount else 0f
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth(),
            strokeCap = StrokeCap.Round
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Active practice cards according to selected Mode
        if (currentWord != null) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                when (selectedMode) {
                    PracticeMode.Flashcards -> {
                        FlashcardPracticeLayout(viewModel = viewModel, word = currentWord!!, katakanaFreeMode = appSettings.katakanaFreeMode)
                    }
                    PracticeMode.MultipleChoice -> {
                        MultipleChoicePracticeLayout(viewModel = viewModel, word = currentWord!!, katakanaFreeMode = appSettings.katakanaFreeMode)
                    }
                    PracticeMode.Typing -> {
                        TypingPracticeLayout(viewModel = viewModel, word = currentWord!!, katakanaFreeMode = appSettings.katakanaFreeMode)
                    }
                }
            }
        }
    }
}

@Composable
fun FlashcardPracticeLayout(viewModel: PracticeViewModel, word: Word, katakanaFreeMode: Boolean) {
    val selectedDirection by viewModel.selectedDirection.collectAsState()
    val isRevealed by viewModel.isFlashcardRevealed.collectAsState()

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
                    Text(text = "FLASHCARD", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)

                    // Front side of card
                    val frontMeanings = if (selectedDirection == PracticeDirection.EnglishToJapanese) word.english else word.japanese
                    val frontText = frontMeanings.joinToString("\n") {
                        it.formatDisplay(
                            showPronunciation = !it.kanjiMode || isRevealed,
                            katakanaFreeMode = katakanaFreeMode
                        )
                    }
                    Text(
                        text = frontText,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    if (isRevealed) {
                        Spacer(modifier = Modifier.height(8.dp))
                        // Back side answer display
                        val backMeanings = if (selectedDirection == PracticeDirection.EnglishToJapanese) word.japanese else word.english
                        val backText = backMeanings.joinToString("\n") {
                            it.formatDisplay(showPronunciation = true, katakanaFreeMode = katakanaFreeMode)
                        }

                        Text(
                            text = backText,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center
                        )
                        // Extra detailed descriptions
                        val typeLabel = word.type.toDisplayString()
                        val notesText = word.notes ?: "No notes"
                        Text(
                            text = "Type: $typeLabel | Notes: $notesText",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        // Action buttons are fixed at the bottom outside the scrollable area!
        Spacer(modifier = Modifier.height(8.dp))
        if (!isRevealed) {
            Button(
                onClick = { viewModel.revealFlashcard() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Reveal Answer")
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { viewModel.gradeFlashcard(false) },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.weight(1f).fillMaxHeight()
                ) {
                    Text("Forgot")
                }
                Button(
                    onClick = { viewModel.gradeFlashcard(true) },
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
fun MultipleChoicePracticeLayout(viewModel: PracticeViewModel, word: Word, katakanaFreeMode: Boolean) {
    val selectedDirection by viewModel.selectedDirection.collectAsState()
    val mcOptions by viewModel.mcOptions.collectAsState()
    val selectedMCOption by viewModel.selectedMCOption.collectAsState()
    val isAnswerChecked by viewModel.isAnswerChecked.collectAsState()
    val isCorrect by viewModel.isCorrect.collectAsState()

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
                    Text(text = "MULTIPLE CHOICE", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)

                    // Display Question
                    val qMeanings = if (selectedDirection == PracticeDirection.EnglishToJapanese) word.english else word.japanese
                    val qText = qMeanings.joinToString("\n") {
                        it.formatDisplay(showPronunciation = !it.kanjiMode || isAnswerChecked, katakanaFreeMode = katakanaFreeMode)
                    }
                    Text(
                        text = qText,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    // Display Answers Option List
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        mcOptions.forEach { option ->
                            val isCorrectIdx = option.id == word.id
                            val isSelectedIdx = selectedMCOption?.id == option.id

                            val choiceColor = when {
                                isAnswerChecked && isCorrectIdx -> ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFE8F5E9),
                                    contentColor = Color(0xFF2E7D32)
                                )
                                isAnswerChecked && isSelectedIdx -> ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFFFEBEE),
                                    contentColor = Color(0xFFC62828)
                                )
                                isAnswerChecked -> ButtonDefaults.buttonColors(
                                    containerColor = Color.White,
                                    contentColor = MaterialTheme.colorScheme.onSurface
                                )
                                else -> ButtonDefaults.outlinedButtonColors(
                                    containerColor = Color.Transparent,
                                    contentColor = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            val choiceBorder = when {
                                isAnswerChecked && isCorrectIdx -> BorderStroke(1.5.dp, Color(0xFF2E7D32))
                                isAnswerChecked && isSelectedIdx -> BorderStroke(1.5.dp, Color(0xFFC62828))
                                else -> BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                            }

                            OutlinedButton(
                                onClick = { if (!isAnswerChecked) viewModel.checkMCOption(option) },
                                colors = choiceColor,
                                modifier = Modifier.fillMaxWidth(),
                                border = choiceBorder
                            ) {
                                val choiceText = if (selectedDirection == PracticeDirection.EnglishToJapanese) {
                                    option.japanese.joinToString(", ") { it.formatDisplay(showPronunciation = !it.kanjiMode || isAnswerChecked, katakanaFreeMode = katakanaFreeMode) }
                                } else {
                                    option.english.joinToString(", ") { it.formatDisplay(showPronunciation = true, katakanaFreeMode = katakanaFreeMode) }
                                }

                                Text(
                                    text = choiceText,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }

        // Bottom status and action buttons fixed at the bottom
        if (isAnswerChecked) {
            Spacer(modifier = Modifier.height(8.dp))
            val boxColor = if (isCorrect) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
            val labelColor = if (isCorrect) Color(0xFF2E7D32) else Color(0xFFC62828)

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(boxColor, RoundedCornerShape(8.dp))
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val outputString = if (isCorrect) {
                        "Correct! Excellent Work! ✨"
                    } else {
                        val correctString = if (selectedDirection == PracticeDirection.EnglishToJapanese) {
                            word.japanese.joinToString(", ") { it.formatDisplay(showPronunciation = true, katakanaFreeMode = katakanaFreeMode) }
                        } else {
                            word.english.joinToString(", ") { it.formatDisplay(showPronunciation = true, katakanaFreeMode = katakanaFreeMode) }
                        }
                        "Incorrect. Correct answer: $correctString"
                    }

                    Text(
                        text = outputString,
                        color = labelColor,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }

                Button(
                    onClick = { viewModel.moveToNext() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Next Question")
                }
            }
        }
    }
}

@Composable
fun TypingPracticeLayout(viewModel: PracticeViewModel, word: Word, katakanaFreeMode: Boolean) {
    val selectedDirection by viewModel.selectedDirection.collectAsState()
    val isAnswerChecked by viewModel.isAnswerChecked.collectAsState()
    val isCorrect by viewModel.isCorrect.collectAsState()

    var answerDrafts by remember(word) { mutableStateOf(listOf(TypingAnswerDraft())) }
    var currentDraftIndex by remember(word) { mutableIntStateOf(0) }
    val focusManager = LocalFocusManager.current

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
                    Text(text = "TYPING PRACTICE", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)

                    // Display question
                    val questionMeanings = if (selectedDirection == PracticeDirection.EnglishToJapanese) word.english else word.japanese
                    val promptText = questionMeanings.joinToString("\n") {
                        it.formatDisplay(
                            showPronunciation = !it.kanjiMode || isAnswerChecked,
                            katakanaFreeMode = katakanaFreeMode
                        )
                    }
                    Text(
                        text = promptText,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    // 4-button multi-meaning controls: [X] [←] [→] [+]
                    val currentDraft = answerDrafts.getOrElse(currentDraftIndex) { TypingAnswerDraft() }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                if (answerDrafts.size > 1) {
                                    val updated = answerDrafts.filterIndexed { i, _ -> i != currentDraftIndex }
                                    answerDrafts = updated
                                    currentDraftIndex = currentDraftIndex.coerceAtMost(updated.lastIndex)
                                }
                            },
                            enabled = !isAnswerChecked && answerDrafts.size > 1,
                            colors = IconButtonDefaults.iconButtonColors(
                                disabledContentColor = Color.Gray.copy(alpha = 0.4f)
                            )
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Delete meaning")
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = { if (currentDraftIndex > 0) currentDraftIndex -= 1 },
                                enabled = currentDraftIndex > 0,
                                colors = IconButtonDefaults.iconButtonColors(
                                    disabledContentColor = Color.Gray.copy(alpha = 0.4f)
                                )
                            ) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous meaning")
                            }

                            Text(
                                text = "Meaning ${currentDraftIndex + 1}/${answerDrafts.size}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold
                            )

                            IconButton(
                                onClick = { if (currentDraftIndex < answerDrafts.lastIndex) currentDraftIndex += 1 },
                                enabled = currentDraftIndex < answerDrafts.lastIndex,
                                colors = IconButtonDefaults.iconButtonColors(
                                    disabledContentColor = Color.Gray.copy(alpha = 0.4f)
                                )
                            ) {
                                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next meaning")
                            }
                        }

                        IconButton(
                            onClick = {
                                val updated = answerDrafts + TypingAnswerDraft()
                                answerDrafts = updated
                                currentDraftIndex = updated.lastIndex
                            },
                            enabled = !isAnswerChecked,
                            colors = IconButtonDefaults.iconButtonColors(
                                disabledContentColor = Color.Gray.copy(alpha = 0.4f)
                            )
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add meaning")
                        }
                    }

                    // Text field (both directions)
                    OutlinedTextField(
                        value = currentDraft.text,
                        onValueChange = { newText ->
                            if (!isAnswerChecked) {
                                answerDrafts = answerDrafts.mapIndexed { i, draft ->
                                    if (i == currentDraftIndex) draft.copy(text = newText) else draft
                                }
                            }
                        },
                        label = { Text("Text") },
                        singleLine = true,
                        enabled = !isAnswerChecked,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Pronunciation field (English -> Japanese only)
                    if (selectedDirection == PracticeDirection.EnglishToJapanese) {
                        OutlinedTextField(
                            value = currentDraft.pronunciation,
                            onValueChange = { newPron ->
                                if (!isAnswerChecked) {
                                    answerDrafts = answerDrafts.mapIndexed { i, draft ->
                                        if (i == currentDraftIndex) draft.copy(pronunciation = newPron) else draft
                                    }
                                }
                            },
                            label = { Text("Pronunciation") },
                            singleLine = true,
                            enabled = !isAnswerChecked,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Sense field (both directions)
                    OutlinedTextField(
                        value = currentDraft.sense,
                        onValueChange = { newSense ->
                            if (!isAnswerChecked) {
                                answerDrafts = answerDrafts.mapIndexed { i, draft ->
                                    if (i == currentDraftIndex) draft.copy(sense = newSense) else draft
                                }
                            }
                        },
                        label = { Text("Sense") },
                        singleLine = true,
                        enabled = !isAnswerChecked,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(onSend = {
                            focusManager.clearFocus()
                            viewModel.checkTypingAnswer(answerDrafts)
                        }),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Bottom actions at the absolute bottom
        Spacer(modifier = Modifier.height(8.dp))
        if (!isAnswerChecked) {
            Button(
                onClick = {
                    focusManager.clearFocus()
                    viewModel.checkTypingAnswer(answerDrafts)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Submit")
            }
        } else {
            val boxColor = if (isCorrect) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
            val labelColor = if (isCorrect) Color(0xFF2E7D32) else Color(0xFFC62828)

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(boxColor, RoundedCornerShape(8.dp))
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val answerMeanings = if (selectedDirection == PracticeDirection.JapaneseToEnglish) word.english else word.japanese
                    val target = answerMeanings.joinToString("\n") {
                        it.formatDisplay(showPronunciation = true, katakanaFreeMode = katakanaFreeMode)
                    }
                    var outputString = if (isCorrect) {
                        "Correct! Answers:\n$target"
                    } else {
                        "Incorrect. Correct answers:\n$target"
                    }
                    outputString += if (!word.notes.isNullOrEmpty()) "\n\nNotes: ${word.notes}" else ""

                    Text(
                        text = outputString,
                        color = labelColor,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }

                Button(
                    onClick = { viewModel.moveToNext() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Next Question")
                }
            }
        }
    }
}

@Composable
fun PracticeSummaryScreen(viewModel: PracticeViewModel) {
    val completedCount by viewModel.completedCount.collectAsState()
    val mistakesCount by viewModel.mistakesCount.collectAsState()

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
                .fillMaxWidth()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(Color(0xFFE8F5E9), RoundedCornerShape(32.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Success",
                    tint = Color(0xFF2E7D32),
                    modifier = Modifier.size(36.dp)
                )
            }

            Text(
                text = "Round Completed!",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Text(
                text = "You managed to clear all selected practice cards in your registry round. Great progress!",
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
                    Text(text = "ITEMS COMPLETED", style = MaterialTheme.typography.labelSmall)
                    Text(text = "$completedCount", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "MISTAKES LOGGED", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
                    Text(text = "$mistakesCount", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.error)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = { viewModel.leaveSession() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Finish Round")
            }
        }
    }
}
