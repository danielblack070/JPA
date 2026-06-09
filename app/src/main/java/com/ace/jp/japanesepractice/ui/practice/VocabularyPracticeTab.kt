package com.ace.jp.japanesepractice.ui.practice

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
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
import com.ace.jp.japanesepractice.data.model.Word

@Composable
fun VocabularyPracticeTab(viewModel: PracticeViewModel) {
    val isActiveSession by viewModel.isActiveSession.collectAsState()
    val showSummary by viewModel.showSummary.collectAsState()

    // Automatically load/refresh latest database elements when navigating to this tab
    LaunchedEffect(Unit) {
        viewModel.loadData()
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
    val easyMode by viewModel.easyMode.collectAsState()
    val itemCountInput by viewModel.itemCountInput.collectAsState()
    val selectedDirection by viewModel.selectedDirection.collectAsState()
    val selectedTypeFilter by viewModel.selectedTypeFilter.collectAsState()
    val lastPracticedFilter by viewModel.lastPracticedFilter.collectAsState()
    val selectedItemsCount by viewModel.selectedItemsCount.collectAsState()
    val selectedConfidenceLevels by viewModel.selectedConfidenceLevels.collectAsState()

    var dropdownExpanded by remember { mutableStateOf(false) }
    var lastPracticedDropdownExpanded by remember { mutableStateOf(false) }
    var showEasyModeHelp by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Practice Setup",
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
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PracticeMode.entries.forEach { mode ->
                    val isSelected = selectedMode == mode
                    Button(
                        onClick = { viewModel.setMode(mode) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = mode.name, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        // 2. Practice Direction Selection
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = "Direction Selector", style = MaterialTheme.typography.labelMedium)
            Row(
                modifier = Modifier.fillMaxWidth(),
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
                        modifier = Modifier.weight(1f)
                    ) {
                        val displayTxt = if (direction == PracticeDirection.EnglishToJapanese) "Eng ➔ Jp" else "Jp ➔ Eng"
                        Text(text = displayTxt, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        // 3. Word Type Filter Selector (Dropdown Menu combining Broad types + concrete Type entries)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = "Word Type Filter", style = MaterialTheme.typography.labelMedium)
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(
                    onClick = { dropdownExpanded = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = selectedTypeFilter)
                        Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = "Expand type dropdown")
                    }
                }

                DropdownMenu(
                    expanded = dropdownExpanded,
                    onDismissRequest = { dropdownExpanded = false },
                    modifier = Modifier.fillMaxWidth(0.9f)
                ) {
                    DropdownMenuItem(
                        text = { Text("All") },
                        onClick = {
                            viewModel.setTypeFilter("All")
                            dropdownExpanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Verb") },
                        onClick = {
                            viewModel.setTypeFilter("Verb")
                            dropdownExpanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Adjective") },
                        onClick = {
                            viewModel.setTypeFilter("Adjective")
                            dropdownExpanded = false
                        }
                    )
                    HorizontalDivider()
                    Type.entries.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option.toDisplayString()) },
                            onClick = {
                                viewModel.setTypeFilter(option.toDisplayString())
                                dropdownExpanded = false
                            }
                        )
                    }
                }
            }
        }

        // 4. Last Practiced Filter Selector
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = "Last Practiced Filter", style = MaterialTheme.typography.labelMedium)
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(
                    onClick = { lastPracticedDropdownExpanded = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = lastPracticedFilter)
                        Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = "Expand last practiced dropdown")
                    }
                }

                DropdownMenu(
                    expanded = lastPracticedDropdownExpanded,
                    onDismissRequest = { lastPracticedDropdownExpanded = false },
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
                                viewModel.setLastPracticedFilter(option)
                                lastPracticedDropdownExpanded = false
                            }
                        )
                    }
                }
            }
        }

        // 5. Confidence Filter Selector
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Conf:", style = MaterialTheme.typography.bodySmall)
            Button(
                onClick = {
                    val newLevels = if (selectedConfidenceLevels.size == 6) emptySet() else setOf(0, 1, 2, 3, 4, 5)
                    viewModel.setConfidenceLevels(newLevels)
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
                            val newLevels = if (lvl in selectedConfidenceLevels) {
                                selectedConfidenceLevels - lvl
                            } else {
                                selectedConfidenceLevels + lvl
                            }
                            viewModel.setConfidenceLevels(newLevels)
                        },
                        label = { Text("${lvl * 20}%", fontSize = 10.sp) }
                    )
                }
            }
        }

        // 6. Input Configuration field & Easy Mode with Help Tooltip
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = itemCountInput,
                onValueChange = { viewModel.setItemCountInput(it) },
                label = { Text("Items per round") },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                ),
                modifier = Modifier.weight(1f)
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(text = "Easy Mode", style = MaterialTheme.typography.bodyMedium)
                IconButton(onClick = { showEasyModeHelp = true }, modifier = Modifier.size(24.dp)) {
                    Text("❓", style = MaterialTheme.typography.bodySmall)
                }
                Switch(
                    checked = easyMode,
                    onCheckedChange = { viewModel.setEasyMode(it) }
                )
            }
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

        Spacer(modifier = Modifier.height(16.dp))

        // Start Practice Trigger
        Button(
            onClick = { viewModel.startPracticeSession() },
            modifier = Modifier.fillMaxWidth(),
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
    val easyMode by viewModel.easyMode.collectAsState()
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
                        FlashcardPracticeLayout(viewModel = viewModel, word = currentWord!!, easyMode = easyMode)
                    }
                    PracticeMode.MultipleChoice -> {
                        MultipleChoicePracticeLayout(viewModel = viewModel, word = currentWord!!, easyMode = easyMode)
                    }
                    PracticeMode.Typing -> {
                        TypingPracticeLayout(viewModel = viewModel, word = currentWord!!, easyMode = easyMode)
                    }
                }
            }
        }
    }
}

@Composable
fun FlashcardPracticeLayout(viewModel: PracticeViewModel, word: Word, easyMode: Boolean) {
    val selectedDirection by viewModel.selectedDirection.collectAsState()
    val isRevealed by viewModel.isFlashcardRevealed.collectAsState()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(350.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "FLASHCARD", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)

            // Front side of card
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.weight(1f)
            ) {
                val frontText = if (selectedDirection == PracticeDirection.EnglishToJapanese) word.english else word.japanese
                Text(
                    text = frontText,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                // Easy mode hint, or revealed reading show
                if (selectedDirection == PracticeDirection.JapaneseToEnglish && (easyMode || isRevealed) && !word.reading.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "(${word.reading})",
                        fontSize = 18.sp,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Interactive Answers Reveal Frame
            if (!isRevealed) {
                Button(
                    onClick = { viewModel.revealFlashcard() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Reveal Answer")
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Back side answer display
                    val backText = if (selectedDirection == PracticeDirection.EnglishToJapanese) {
                        "${word.japanese} ${if (!word.reading.isNullOrEmpty()) "(${word.reading})" else ""}"
                    } else {
                        word.english
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

                    // Card Manual grader button layout
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { viewModel.gradeFlashcard(false) },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Forgot")
                        }
                        Button(
                            onClick = { viewModel.gradeFlashcard(true) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Correct")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MultipleChoicePracticeLayout(viewModel: PracticeViewModel, word: Word, easyMode: Boolean) {
    val selectedDirection by viewModel.selectedDirection.collectAsState()
    val mcOptions by viewModel.mcOptions.collectAsState()
    val selectedMCOption by viewModel.selectedMCOption.collectAsState()
    val isAnswerChecked by viewModel.isAnswerChecked.collectAsState()
    val isCorrect by viewModel.isCorrect.collectAsState()

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
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                val qText = if (selectedDirection == PracticeDirection.EnglishToJapanese) word.english else word.japanese
                Text(
                    text = qText,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                if (selectedDirection == PracticeDirection.JapaneseToEnglish && (easyMode || isAnswerChecked) && !word.reading.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "(${word.reading})",
                        fontSize = 18.sp,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

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
                            val readingPart = if (easyMode && !option.reading.isNullOrEmpty()) " (${option.reading})" else ""
                            "${option.japanese}$readingPart"
                        } else {
                            option.english
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

            // High impact validation result banner
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
                                "${word.japanese} ${if (!word.reading.isNullOrEmpty()) "(${word.reading})" else ""}"
                            } else {
                                word.english
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
}

@Composable
fun TypingPracticeLayout(viewModel: PracticeViewModel, word: Word, easyMode: Boolean) {
    val selectedDirection by viewModel.selectedDirection.collectAsState()
    val isAnswerChecked by viewModel.isAnswerChecked.collectAsState()
    val isCorrect by viewModel.isCorrect.collectAsState()

    var textInputState by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    // Automatically empty text on new question
    LaunchedEffect(word) {
        textInputState = ""
    }

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
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                val promptText = if (selectedDirection == PracticeDirection.EnglishToJapanese) word.english else word.japanese
                Text(
                    text = promptText,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                if (selectedDirection == PracticeDirection.JapaneseToEnglish && (easyMode || isAnswerChecked) && !word.reading.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "(${word.reading})",
                        fontSize = 18.sp,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Input TextField frame
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                OutlinedTextField(
                    value = textInputState,
                    onValueChange = { if (!isAnswerChecked) textInputState = it },
                    placeholder = {
                        val helpText = if (selectedDirection == PracticeDirection.EnglishToJapanese) {
                            if (easyMode) "Reading (Kana)" else "Exact Kanji or Reading"
                        } else {
                            "Exact English"
                        }
                        Text(text = "Type answer ($helpText)...")
                    },
                    singleLine = true,
                    enabled = !isAnswerChecked,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = {
                        focusManager.clearFocus()
                        viewModel.checkTypingAnswer(textInputState)
                    }),
                    modifier = Modifier.fillMaxWidth()
                )

                // Submit Button
                if (!isAnswerChecked) {
                    Button(
                        onClick = {
                            focusManager.clearFocus()
                            viewModel.checkTypingAnswer(textInputState)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Submit")
                    }
                } else {
                    // Answer Graded Response Overlay
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
                                val target = if (selectedDirection == PracticeDirection.JapaneseToEnglish) {
                                    word.english
                                } else {
                                    "${word.japanese} ${if (!word.reading.isNullOrEmpty()) "(${word.reading})" else ""}"
                                }
                                "Incorrect. Correct: $target"
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
