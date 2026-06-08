package com.ace.jp.japanesepractice.ui.practice

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ace.jp.japanesepractice.data.Repository
import com.ace.jp.japanesepractice.data.model.Type
import com.ace.jp.japanesepractice.data.model.Word
import com.ace.jp.japanesepractice.data.model.WordList
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class PracticeMode {
    Flashcards, MultipleChoice, Typing
}

enum class PracticeDirection {
    EnglishToJapanese, JapaneseToEnglish
}

class PracticeViewModel(private val repository: Repository) : ViewModel() {

    private val _words = MutableStateFlow<List<Word>>(emptyList())
    private val _wordLists = MutableStateFlow<List<WordList>>(emptyList())

    // Setup configuration states
    private val _selectedMode = MutableStateFlow(PracticeMode.Flashcards)
    val selectedMode: StateFlow<PracticeMode> = _selectedMode.asStateFlow()

    private val _easyMode = MutableStateFlow(false)
    val easyMode: StateFlow<Boolean> = _easyMode.asStateFlow()

    private val _itemCountInput = MutableStateFlow("10")
    val itemCountInput: StateFlow<String> = _itemCountInput.asStateFlow()

    private val _selectedDirection = MutableStateFlow(PracticeDirection.JapaneseToEnglish)
    val selectedDirection: StateFlow<PracticeDirection> = _selectedDirection.asStateFlow()

    private val _selectedTypeFilter = MutableStateFlow("All")
    val selectedTypeFilter: StateFlow<String> = _selectedTypeFilter.asStateFlow()

    // Active session status control
    private val _isActiveSession = MutableStateFlow(false)
    val isActiveSession: StateFlow<Boolean> = _isActiveSession.asStateFlow()

    private val _wordsQueue = MutableStateFlow<List<Word>>(emptyList())
    val wordsQueue: StateFlow<List<Word>> = _wordsQueue.asStateFlow()

    private val _currentWord = MutableStateFlow<Word?>(null)
    val currentWord: StateFlow<Word?> = _currentWord.asStateFlow()

    private val _totalRoundCount = MutableStateFlow(0)
    val totalRoundCount: StateFlow<Int> = _totalRoundCount.asStateFlow()

    private val _completedCount = MutableStateFlow(0)
    val completedCount: StateFlow<Int> = _completedCount.asStateFlow()

    private val _mistakesCount = MutableStateFlow(0)
    val mistakesCount: StateFlow<Int> = _mistakesCount.asStateFlow()

    private val _showSummary = MutableStateFlow(false)
    val showSummary: StateFlow<Boolean> = _showSummary.asStateFlow()

    // Flashcard: Card flip visibility state
    private val _isFlashcardRevealed = MutableStateFlow(false)
    val isFlashcardRevealed: StateFlow<Boolean> = _isFlashcardRevealed.asStateFlow()

    // Multiple Choice options and selection state
    private val _mcOptions = MutableStateFlow<List<Word>>(emptyList())
    val mcOptions: StateFlow<List<Word>> = _mcOptions.asStateFlow()

    private val _selectedMCOption = MutableStateFlow<Word?>(null)
    val selectedMCOption: StateFlow<Word?> = _selectedMCOption.asStateFlow()

    // Automatic feedback states for MC & Typing
    private val _isAnswerChecked = MutableStateFlow(false)
    val isAnswerChecked: StateFlow<Boolean> = _isAnswerChecked.asStateFlow()

    private val _isCorrect = MutableStateFlow(false)
    val isCorrect: StateFlow<Boolean> = _isCorrect.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _words.value = repository.getAllWords()
            _wordLists.value = repository.getAllWordLists()
        }
    }

    fun setMode(mode: PracticeMode) {
        _selectedMode.value = mode
    }

    fun setEasyMode(enabled: Boolean) {
        _easyMode.value = enabled
    }

    fun setItemCountInput(input: String) {
        _itemCountInput.value = input
    }

    fun setDirection(direction: PracticeDirection) {
        _selectedDirection.value = direction
    }

    fun setTypeFilter(filter: String) {
        _selectedTypeFilter.value = filter
    }

    fun startPracticeSession() {
        viewModelScope.launch {
            _words.value = repository.getAllWords()
            _wordLists.value = repository.getAllWordLists()

            val activeListIds = _wordLists.value.filter { it.isEnabled }.map { it.id }.toSet()
            val allActiveWords = _words.value.filter { it.isEnabled && it.wordListId in activeListIds }

            // Apply selected Type filter (including Verb and Adjective board groupings)
            val filteredWords = allActiveWords.filter { word ->
                when (val filter = _selectedTypeFilter.value) {
                    "All" -> true
                    "Verb" -> word.type == Type.UVerb || word.type == Type.RuVerb || word.type == Type.IrrVerb
                    "Adjective" -> word.type == Type.IAdjective || word.type == Type.NaAdjective || word.type == Type.YoAdjective
                    else -> {
                        val resolvedType = runCatching { Type.fromDisplayString(filter) }.getOrNull()
                        resolvedType == null || word.type == resolvedType
                    }
                }
            }

            if (filteredWords.isEmpty()) return@launch

            val targetSize = _itemCountInput.value.toIntOrNull()?.coerceAtLeast(1) ?: 10
            val shuffledPool = filteredWords.shuffled()
            val roundWords = shuffledPool.take(targetSize)

            _wordsQueue.value = roundWords
            _totalRoundCount.value = roundWords.size
            _completedCount.value = 0
            _mistakesCount.value = 0
            _showSummary.value = false
            _isActiveSession.value = true

            nextWord()
        }
    }

    private fun nextWord() {
        val currentQueue = _wordsQueue.value
        if (currentQueue.isEmpty()) {
            _currentWord.value = null
            _showSummary.value = true
            return
        }

        val word = currentQueue.first()
        _currentWord.value = word
        _isFlashcardRevealed.value = false
        _selectedMCOption.value = null
        _isAnswerChecked.value = false
        _isCorrect.value = false

        if (_selectedMode.value == PracticeMode.MultipleChoice) {
            generateMCOptions(word)
        }
    }

    private fun generateMCOptions(correctWord: Word) {
        val activeListIds = _wordLists.value.filter { it.isEnabled }.map { it.id }.toSet()
        val pool = _words.value.filter { it.isEnabled && it.wordListId in activeListIds }

        val distractors = pool.filter { it.id != correctWord.id }
            .shuffled()
            .take(3)

        _mcOptions.value = (distractors + correctWord).shuffled()
    }

    fun gradeFlashcard(correct: Boolean) {
        val current = _currentWord.value ?: return
        val currentQueue = _wordsQueue.value.toMutableList()

        if (correct) {
            currentQueue.removeAt(0)
            _completedCount.value += 1
        } else {
            _mistakesCount.value += 1
            currentQueue.removeAt(0)
            currentQueue.add(current) // Appended to end of queue
        }

        _wordsQueue.value = currentQueue
        nextWord()
    }

    fun checkMCOption(selected: Word) {
        if (_isAnswerChecked.value) return
        _selectedMCOption.value = selected
        _isAnswerChecked.value = true

        val current = _currentWord.value ?: return
        val isCorrectAnswer = selected.id == current.id
        _isCorrect.value = isCorrectAnswer

        viewModelScope.launch {
            delay(1200)
            val currentQueue = _wordsQueue.value.toMutableList()
            if (isCorrectAnswer) {
                currentQueue.removeAt(0)
                _completedCount.value += 1
            } else {
                _mistakesCount.value += 1
                currentQueue.removeAt(0)
                currentQueue.add(current)
            }
            _wordsQueue.value = currentQueue
            nextWord()
        }
    }

    fun checkTypingAnswer(userText: String) {
        if (_isAnswerChecked.value) return
        val current = _currentWord.value ?: return
        _isAnswerChecked.value = true

        val cleanAnswer = userText.trim().lowercase()

        val isCorrectAnswer = if (_selectedDirection.value == PracticeDirection.JapaneseToEnglish) {
            cleanAnswer == current.english.trim().lowercase()
        } else {
            val cleanJp = current.japanese.trim().lowercase()
            val cleanRd = current.reading?.trim()?.lowercase() ?: ""
            cleanAnswer == cleanJp || cleanAnswer == cleanRd
        }

        _isCorrect.value = isCorrectAnswer

        viewModelScope.launch {
            delay(1800)
            val currentQueue = _wordsQueue.value.toMutableList()
            if (isCorrectAnswer) {
                currentQueue.removeAt(0)
                _completedCount.value += 1
            } else {
                _mistakesCount.value += 1
                currentQueue.removeAt(0)
                currentQueue.add(current)
            }
            _wordsQueue.value = currentQueue
            nextWord()
        }
    }

    fun revealFlashcard() {
        _isFlashcardRevealed.value = true
    }

    fun leaveSession() {
        _isActiveSession.value = false
        _showSummary.value = false
        _wordsQueue.value = emptyList()
        _currentWord.value = null
    }
}