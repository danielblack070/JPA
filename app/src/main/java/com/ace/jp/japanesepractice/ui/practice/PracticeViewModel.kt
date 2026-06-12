package com.ace.jp.japanesepractice.ui.practice

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ace.jp.japanesepractice.data.Repository
import com.ace.jp.japanesepractice.data.model.Type
import com.ace.jp.japanesepractice.data.model.Word
import com.ace.jp.japanesepractice.data.model.WordList
import com.ace.jp.japanesepractice.data.model.MasterRule
import com.ace.jp.japanesepractice.data.model.SubRule
import com.ace.jp.japanesepractice.data.model.GrammarRule
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

enum class PracticeMode {
    Flashcards, MultipleChoice, Typing
}

enum class PracticeDirection {
    EnglishToJapanese, JapaneseToEnglish
}

sealed class ConjugationQuizItem {
    data class FlashcardItem(val masterRule: MasterRule, val subRules: List<SubRule>) : ConjugationQuizItem()
    data class InteractiveItem(val word: Word, val masterRule: MasterRule, val subRule: SubRule, val conjugatedWord: ConjugatedWord) : ConjugationQuizItem()
}

data class ConjugatedWord(val japanese: String, val reading: String?)

class PracticeViewModel(private val repository: Repository) : ViewModel() {

    private val _words = MutableStateFlow<List<Word>>(emptyList())
    private val _wordLists = MutableStateFlow<List<WordList>>(emptyList())
    private val _masterRules = MutableStateFlow<List<MasterRule>>(emptyList())
    private val _subRules = MutableStateFlow<List<SubRule>>(emptyList())
    private val _grammarRules = MutableStateFlow<List<GrammarRule>>(emptyList())
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

    private val _lastPracticedFilter = MutableStateFlow("Any")
    val lastPracticedFilter: StateFlow<String> = _lastPracticedFilter.asStateFlow()

    private val _selectedConfidenceLevels = MutableStateFlow(setOf(0, 1, 2, 3, 4, 5))
    val selectedConfidenceLevels: StateFlow<Set<Int>> = _selectedConfidenceLevels.asStateFlow()

    // Vocab Select Counter
    val selectedItemsCount: StateFlow<Int> = combine(
        _words, _wordLists, _selectedTypeFilter, _lastPracticedFilter, _selectedConfidenceLevels
    ) { words, lists, typeFilter, lpFilter, confFilter ->
        val activeIds = lists.filter { it.isEnabled }.map { it.id }.toSet()
        words.filter { it.isEnabled && it.wordListId in activeIds }.count { word ->
            matchesTypeFilter(word, typeFilter) &&
                    matchesLastPracticed(word.lastPracticed, lpFilter) &&
                    word.confidence in confFilter
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Conjugation Select Counter
    val selectedConjugationItemsCount: StateFlow<Int> = combine(
        _masterRules, _subRules, _words, _wordLists, _selectedMode, _selectedTypeFilter, _lastPracticedFilter,  _selectedConfidenceLevels
    ) { flowsArray ->
        val mrs = (flowsArray[0] as? List<*>)?.filterIsInstance<MasterRule>() ?: emptyList()
        val subRules = (flowsArray[1] as? List<*>)?.filterIsInstance<SubRule>() ?: emptyList()
        val words = (flowsArray[2] as? List<*>)?.filterIsInstance<Word>() ?: emptyList()
        val lists = (flowsArray[3] as? List<*>)?.filterIsInstance<WordList>() ?: emptyList()
        val mode = flowsArray[4] as PracticeMode
        val typeFilter = flowsArray[5] as String
        val lpFilter = flowsArray[6] as String
        val conf = (flowsArray[7] as? Set<*>)?.filterIsInstance<Int>()?.toSet() ?: emptySet()
        val activeMrs = mrs.filter { it.isEnabled && it.confidence in conf && matchesLastPracticed(it.lastPracticed, lpFilter) }
        if (mode == PracticeMode.Flashcards) {
            activeMrs.size
        } else {
            val activeIds = lists.filter { it.isEnabled }.map { it.id }.toSet()
            val activeWords = words.filter { it.isEnabled && it.wordListId in activeIds && matchesTypeFilter(it, typeFilter) }
            activeWords.count { word -> activeMrs.any { findApplicableSubRule(word, it.id, subRules) != null } }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Grammar Select Counter
    val selectedGrammarRulesCount: StateFlow<Int> = combine(
        _grammarRules, _lastPracticedFilter, _selectedConfidenceLevels
    ) { rules, lpFilter, conf ->
        rules.filter { it.isEnabled && it.confidence in conf && matchesLastPracticed(it.lastPracticed, lpFilter) }.size
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Vocabulary tracking states
    private val _isActiveSession = MutableStateFlow(false)
    val isActiveSession: StateFlow<Boolean> = _isActiveSession.asStateFlow()
    private val _wordsQueue = MutableStateFlow<List<Word>>(emptyList())
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
    private val _isFlashcardRevealed = MutableStateFlow(false)
    val isFlashcardRevealed: StateFlow<Boolean> = _isFlashcardRevealed.asStateFlow()
    private val _mcOptions = MutableStateFlow<List<Word>>(emptyList())
    val mcOptions: StateFlow<List<Word>> = _mcOptions.asStateFlow()
    private val _selectedMCOption = MutableStateFlow<Word?>(null)
    val selectedMCOption: StateFlow<Word?> = _selectedMCOption.asStateFlow()
    private val _isAnswerChecked = MutableStateFlow(false)
    val isAnswerChecked: StateFlow<Boolean> = _isAnswerChecked.asStateFlow()
    private val _isCorrect = MutableStateFlow(false)
    val isCorrect: StateFlow<Boolean> = _isCorrect.asStateFlow()
    private val _sessionWordMistakes = MutableStateFlow<Map<Int, Int>>(emptyMap())

    // Conjugation tracking states
    private val _isConjugationActiveSession = MutableStateFlow(false)
    val isConjugationActiveSession: StateFlow<Boolean> = _isConjugationActiveSession.asStateFlow()
    private val _conjugationQuizQueue = MutableStateFlow<List<ConjugationQuizItem>>(emptyList())
    private val _currentConjugationItem = MutableStateFlow<ConjugationQuizItem?>(null)
    val currentConjugationItem: StateFlow<ConjugationQuizItem?> = _currentConjugationItem.asStateFlow()
    private val _conjugationTotalRoundCount = MutableStateFlow(0)
    val conjugationTotalRoundCount: StateFlow<Int> = _conjugationTotalRoundCount.asStateFlow()
    private val _conjugationCompletedCount = MutableStateFlow(0)
    val conjugationCompletedCount: StateFlow<Int> = _conjugationCompletedCount.asStateFlow()
    private val _conjugationMistakesCount = MutableStateFlow(0)
    val conjugationMistakesCount: StateFlow<Int> = _conjugationMistakesCount.asStateFlow()
    private val _conjugationShowSummary = MutableStateFlow(false)
    val conjugationShowSummary: StateFlow<Boolean> = _conjugationShowSummary.asStateFlow()
    private val _isConjugationFlashcardRevealed = MutableStateFlow(false)
    val isConjugationFlashcardRevealed: StateFlow<Boolean> = _isConjugationFlashcardRevealed.asStateFlow()
    private val _conjugationMCOptions = MutableStateFlow<List<ConjugatedWord>>(emptyList())
    val conjugationMCOptions: StateFlow<List<ConjugatedWord>> = _conjugationMCOptions.asStateFlow()
    private val _selectedConjugationMCOption = MutableStateFlow<ConjugatedWord?>(null)
    val selectedConjugationMCOption: StateFlow<ConjugatedWord?> = _selectedConjugationMCOption.asStateFlow()
    private val _isConjugationAnswerChecked = MutableStateFlow(false)
    val isConjugationAnswerChecked: StateFlow<Boolean> = _isConjugationAnswerChecked.asStateFlow()
    private val _isConjugationCorrect = MutableStateFlow(false)
    val isConjugationCorrect: StateFlow<Boolean> = _isConjugationCorrect.asStateFlow()
    private val _sessionConjugationMistakes = MutableStateFlow<Map<Int, Int>>(emptyMap())

    init { loadData() }

    private suspend fun loadDataSuspend() {
        _words.value = repository.getAllWords()
        _wordLists.value = repository.getAllWordLists()
        _masterRules.value = repository.getAllMasterRules()
        _subRules.value = repository.getAllSubRules()
        _grammarRules.value = repository.getAllGrammarRules()
    }

    fun loadData() { viewModelScope.launch { loadDataSuspend() } }

    fun setMode(mode: PracticeMode) { _selectedMode.value = mode }
    fun setEasyMode(enabled: Boolean) { _easyMode.value = enabled }
    fun setItemCountInput(input: String) { _itemCountInput.value = input }
    fun setDirection(dir: PracticeDirection) { _selectedDirection.value = dir }
    fun setTypeFilter(f: String) { _selectedTypeFilter.value = f }
    fun setLastPracticedFilter(f: String) { _lastPracticedFilter.value = f }
    fun setConfidenceLevels(lvls: Set<Int>) { _selectedConfidenceLevels.value = lvls }

    private fun matchesTypeFilter(word: Word, filter: String): Boolean {
        return when (filter) {
            "All" -> true
            "Verb" -> word.type == Type.UVerb || word.type == Type.RuVerb || word.type == Type.IrrVerb
            "Adjective" -> word.type == Type.IAdjective || word.type == Type.NaAdjective || word.type == Type.IrrAdjective
            else -> runCatching { Type.fromDisplayString(filter) }.getOrNull() == null || word.type == Type.fromDisplayString(filter)
        }
    }

    private fun matchesLastPracticed(last: Long?, filter: String): Boolean {
        return when (filter) {
            "Any" -> true
            "Never", "Never practiced" -> last == null
            else -> {
                val cut = System.currentTimeMillis() - when (filter) {
                    "More than a day ago" -> 24 * 3600_000L
                    "More than a week ago" -> 7 * 24 * 3600_000L
                    "More than a month ago" -> 30 * 24 * 3600_000L
                    else -> 0L
                }
                last != null && last <= cut
            }
        }
    }

    // Vocab Round Initiator
    fun startPracticeSession() {
        viewModelScope.launch {
            loadDataSuspend()
            delay(50.milliseconds)
            val activeIds = _wordLists.value.filter { it.isEnabled }.map { it.id }.toSet()
            val filtered = _words.value.filter {
                it.isEnabled && it.wordListId in activeIds &&
                        matchesTypeFilter(it, _selectedTypeFilter.value) &&
                        matchesLastPracticed(it.lastPracticed, _lastPracticedFilter.value) &&
                        it.confidence in _selectedConfidenceLevels.value
            }
            if (filtered.isEmpty()) return@launch
            val countLimit = _itemCountInput.value.toIntOrNull()?.coerceAtLeast(1) ?: 10
            val picked = filtered.shuffled().take(countLimit)

            _sessionWordMistakes.value = emptyMap()
            _wordsQueue.value = picked
            _totalRoundCount.value = picked.size
            _completedCount.value = 0
            _mistakesCount.value = 0
            _showSummary.value = false
            _isActiveSession.value = true
            nextWord()
        }
    }

    private fun nextWord() {
        val q = _wordsQueue.value
        if (q.isEmpty()) {
            _currentWord.value = null
            _showSummary.value = true
            return
        }
        val w = q.first()
        _currentWord.value = w
        _isFlashcardRevealed.value = false
        _selectedMCOption.value = null
        _isAnswerChecked.value = false
        _isCorrect.value = false
        if (_selectedMode.value == PracticeMode.MultipleChoice) { generateMCOptions(w) }
    }

    private fun generateMCOptions(correct: Word) {
        val activeIds = _wordLists.value.filter { it.isEnabled }.map { it.id }.toSet()
        val pool = _words.value.filter { it.isEnabled && it.wordListId in activeIds }
        val dist = pool.filter { it.id != correct.id }.shuffled().take(3)
        _mcOptions.value = (dist + correct).shuffled()
    }

    private fun calculateUpdatedWord(current: Word, correct: Boolean): Word {
        val mistakes = _sessionWordMistakes.value.getOrDefault(current.id, 0)
        return if (correct) {
            current.copy(confidence = (current.confidence + 1).coerceAtMost(5), lastPracticed = System.currentTimeMillis())
        } else {
            _sessionWordMistakes.value += (current.id to mistakes + 1)
            val diff = if (mistakes == 0) 2 else 1
            current.copy(confidence = (current.confidence - diff).coerceAtLeast(0), lastPracticed = System.currentTimeMillis())
        }
    }

    fun gradeFlashcard(correct: Boolean) {
        val word = _currentWord.value ?: return
        val q = _wordsQueue.value.toMutableList()
        val updated = calculateUpdatedWord(word, correct)
        viewModelScope.launch { repository.updateWord(updated); loadDataSuspend() }
        q.removeAt(0)
        if (correct) _completedCount.value += 1 else { _mistakesCount.value += 1; q.add(updated) }
        _wordsQueue.value = q
        nextWord()
    }

    fun checkMCOption(selected: Word) {
        if (_isAnswerChecked.value) return
        _selectedMCOption.value = selected
        _isAnswerChecked.value = true
        val current = _currentWord.value ?: return
        val ok = selected.id == current.id
        _isCorrect.value = ok
        val updated = calculateUpdatedWord(current, ok)
        _currentWord.value = updated
        val q = _wordsQueue.value.toMutableList()
        if (q.isNotEmpty()) { q[0] = updated; _wordsQueue.value = q }
        viewModelScope.launch { repository.updateWord(updated); loadDataSuspend() }
    }

    fun checkTypingAnswer(userText: String) {
        if (_isAnswerChecked.value) return
        _isAnswerChecked.value = true
        val current = _currentWord.value ?: return
        val text = userText.trim().lowercase()
        val ok = if (_selectedDirection.value == PracticeDirection.JapaneseToEnglish) {
            text == current.english.trim().lowercase()
        } else {
            text == current.japanese.trim().lowercase() || text == (current.reading?.trim()?.lowercase() ?: "")
        }
        _isCorrect.value = ok
        val updated = calculateUpdatedWord(current, ok)
        _currentWord.value = updated
        val q = _wordsQueue.value.toMutableList()
        if (q.isNotEmpty()) { q[0] = updated; _wordsQueue.value = q }
        viewModelScope.launch { repository.updateWord(updated); loadDataSuspend() }
    }

    fun moveToNext() {
        val word = _currentWord.value ?: return
        val ok = _isCorrect.value
        val q = _wordsQueue.value.toMutableList()
        q.removeAt(0)
        if (ok) _completedCount.value += 1 else { _mistakesCount.value += 1; q.add(word) }
        _wordsQueue.value = q
        nextWord()
    }

    fun revealFlashcard() { _isFlashcardRevealed.value = true }
    fun leaveSession() {
        _isActiveSession.value = false
        _showSummary.value = false
        _wordsQueue.value = emptyList()
        _currentWord.value = null
    }

    // ==========================================
    // Conjugation Formula Engine & Unique exceptions
    // ==========================================

    fun findApplicableSubRule(word: Word, masterRuleId: Int, allSubRules: List<SubRule>): SubRule? {
        val matches = allSubRules.filter { it.masterRuleId == masterRuleId && it.type == word.type }
        if (matches.isEmpty()) return null

        fun SubRule.matchLength(text: String): Int? {
            val orig = originalEnding ?: ""
            if (orig.isEmpty()) return 0
            return if (text.endsWith(orig)) orig.length else null
        }

        // 1. Matches from unique sub-rules (isUnique = true) sorted by matching originalEnding length descending
        val uniqueMatches = matches.filter { it.isUnique }
            .mapNotNull { rule -> rule.matchLength(word.japanese)?.let { len -> rule to len } }
            .sortedByDescending { it.second }
        if (uniqueMatches.isNotEmpty()) return uniqueMatches.first().first

        // 2. Matches from regular sub-rules (isUnique = false) sorted by lengths
        val regularMatches = matches.filter { !it.isUnique }
            .mapNotNull { rule -> rule.matchLength(word.japanese)?.let { len -> rule to len } }
            .sortedByDescending { it.second }
        if (regularMatches.isNotEmpty()) return regularMatches.first().first

        return null
    }

    fun conjugate(word: Word, subRule: SubRule): ConjugatedWord {
        val orig = subRule.originalEnding ?: ""
        val replacement = subRule.newEnding
        var jp = word.japanese
        var rd = word.reading ?: ""

        if (jp.endsWith("来る")) {
            jp = word.japanese.dropLast(1)
            if (replacement.length > 1) jp += replacement.substring(1)
            rd = if (rd.length >= 2) rd.dropLast(2) + replacement else replacement
        } else {
            if (orig.isNotEmpty()) {
                jp = if (jp.endsWith(orig)) jp.substring(0, jp.length - orig.length) + replacement else jp + replacement
                if (rd.isNotEmpty()) {
                    rd = if (rd.endsWith(orig)) rd.substring(0, rd.length - orig.length) + replacement else rd.dropLast(orig.length) + replacement
                }
            } else {
                jp += replacement
                if (rd.isNotEmpty()) rd += replacement
            }
        }
        return ConjugatedWord(japanese = jp, reading = rd.ifEmpty { null })
    }

    // ==========================================
    // Conjugation Session and Evaluation logic
    // ==========================================

    fun startConjugationPracticeSession() {
        viewModelScope.launch {
            loadDataSuspend()
            delay(50.milliseconds)
            val activeMrs = _masterRules.value.filter {
                it.isEnabled && it.confidence in _selectedConfidenceLevels.value && matchesLastPracticed(it.lastPracticed, _lastPracticedFilter.value)
            }
            if (activeMrs.isEmpty()) return@launch

            val targetSize = _itemCountInput.value.toIntOrNull()?.coerceAtLeast(1) ?: 10
            val subRulesList = _subRules.value

            val items = if (_selectedMode.value == PracticeMode.Flashcards) {
                activeMrs.shuffled().take(targetSize).map { mr ->
                    ConjugationQuizItem.FlashcardItem(mr, subRulesList.filter { it.masterRuleId == mr.id })
                }
            } else {
                val activeIds = _wordLists.value.filter { it.isEnabled }.map { it.id }.toSet()
                val activeWords = _words.value.filter { it.isEnabled && it.wordListId in activeIds && matchesTypeFilter(it, _selectedTypeFilter.value) }

                val questionsList = mutableListOf<ConjugationQuizItem.InteractiveItem>()
                for (word in activeWords) {
                    for (mr in activeMrs) {
                        val sub = findApplicableSubRule(word, mr.id, subRulesList)
                        if (sub != null) {
                            questionsList.add(ConjugationQuizItem.InteractiveItem(word, mr, sub, conjugate(word, sub)))
                        }
                    }
                }
                if (questionsList.isEmpty()) return@launch
                questionsList.shuffled().take(targetSize)
            }

            _sessionConjugationMistakes.value = emptyMap()
            _conjugationQuizQueue.value = items
            _conjugationTotalRoundCount.value = items.size
            _conjugationCompletedCount.value = 0
            _conjugationMistakesCount.value = 0
            _conjugationShowSummary.value = false
            _isConjugationActiveSession.value = true
            nextConjugationQuestion()
        }
    }

    private fun nextConjugationQuestion() {
        val q = _conjugationQuizQueue.value
        if (q.isEmpty()) {
            _currentConjugationItem.value = null
            _conjugationShowSummary.value = true
            return
        }
        val next = q.first()
        _currentConjugationItem.value = next
        _isConjugationFlashcardRevealed.value = false
        _selectedConjugationMCOption.value = null
        _isConjugationAnswerChecked.value = false
        _isConjugationCorrect.value = false

        if (_selectedMode.value == PracticeMode.MultipleChoice && next is ConjugationQuizItem.InteractiveItem) {
            generateConjugationMCOptions(next)
        }
    }

    private fun generateConjugationMCOptions(item: ConjugationQuizItem.InteractiveItem) {
        val corr = item.conjugatedWord
        val allMr = _masterRules.value
        val allSr = _subRules.value

        val options = allMr.mapNotNull { mr ->
            val sub = findApplicableSubRule(item.word, mr.id, allSr)
            if (sub != null) conjugate(item.word, sub) else null
        }.distinctBy { it.japanese }

        val dist = options.filter { it.japanese != corr.japanese }.shuffled()
        _conjugationMCOptions.value = (dist.take(3) + corr).shuffled()
    }

    private fun calculateUpdatedMasterRule(current: MasterRule, correct: Boolean): MasterRule {
        val mistakes = _sessionConjugationMistakes.value.getOrDefault(current.id, 0)
        return if (correct) {
            current.copy(confidence = (current.confidence + 1).coerceAtMost(5), lastPracticed = System.currentTimeMillis())
        } else {
            _sessionConjugationMistakes.value += (current.id to mistakes + 1)
            val diff = if (mistakes == 0) 2 else 1
            current.copy(confidence = (current.confidence - diff).coerceAtLeast(0), lastPracticed = System.currentTimeMillis())
        }
    }

    fun gradeConjugationFlashcard(correct: Boolean) {
        val item = _currentConjugationItem.value as? ConjugationQuizItem.FlashcardItem ?: return
        val q = _conjugationQuizQueue.value.toMutableList()
        val updated = calculateUpdatedMasterRule(item.masterRule, correct)
        viewModelScope.launch { repository.updateMasterRule(updated); loadDataSuspend() }
        q.removeAt(0)
        if (correct) _conjugationCompletedCount.value += 1 else { _conjugationMistakesCount.value += 1; q.add(item.copy(masterRule = updated)) }
        _conjugationQuizQueue.value = q
        nextConjugationQuestion()
    }

    fun checkConjugationMCOption(selected: ConjugatedWord) {
        if (_isConjugationAnswerChecked.value) return
        _selectedConjugationMCOption.value = selected
        _isConjugationAnswerChecked.value = true
        val item = _currentConjugationItem.value as? ConjugationQuizItem.InteractiveItem ?: return
        val ok = selected.japanese == item.conjugatedWord.japanese
        _isConjugationCorrect.value = ok
        val updated = calculateUpdatedMasterRule(item.masterRule, ok)
        _currentConjugationItem.value = item.copy(masterRule = updated)
        val q = _conjugationQuizQueue.value.toMutableList()
        if (q.isNotEmpty()) { q[0] = item.copy(masterRule = updated); _conjugationQuizQueue.value = q }
        viewModelScope.launch { repository.updateMasterRule(updated); loadDataSuspend() }
    }

    fun checkConjugationTypingAnswer(userText: String) {
        if (_isConjugationAnswerChecked.value) return
        _isConjugationAnswerChecked.value = true
        val item = _currentConjugationItem.value as? ConjugationQuizItem.InteractiveItem ?: return
        val raw = userText.trim()
        val correctJp = item.conjugatedWord.japanese.trim()
        val correctRd = item.conjugatedWord.reading?.trim() ?: ""
        val expectedFull = if (correctRd.isNotEmpty()) "$correctJp $correctRd" else correctJp

        val ok = if (_easyMode.value) {
            raw.equals(expectedFull, ignoreCase = true) || (correctRd.isNotEmpty() && raw.equals(correctRd, ignoreCase = true))
        } else {
            raw.equals(expectedFull, ignoreCase = true)
        }
        _isConjugationCorrect.value = ok
        val updated = calculateUpdatedMasterRule(item.masterRule, ok)
        _currentConjugationItem.value = item.copy(masterRule = updated)
        val q = _conjugationQuizQueue.value.toMutableList()
        if (q.isNotEmpty()) { q[0] = item.copy(masterRule = updated); _conjugationQuizQueue.value = q }
        viewModelScope.launch { repository.updateMasterRule(updated); loadDataSuspend() }
    }

    fun moveConjugationToNext() {
        val item = _currentConjugationItem.value ?: return
        val ok = _isConjugationCorrect.value
        val q = _conjugationQuizQueue.value.toMutableList()
        q.removeAt(0)
        if (ok) _conjugationCompletedCount.value += 1 else { _conjugationMistakesCount.value += 1; q.add(item) }
        _conjugationQuizQueue.value = q
        nextConjugationQuestion()
    }

    fun revealConjugationFlashcard() { _isConjugationFlashcardRevealed.value = true }
    fun leaveConjugationSession() {
        _isConjugationActiveSession.value = false
        _conjugationShowSummary.value = false
        _conjugationQuizQueue.value = emptyList()
        _currentConjugationItem.value = null
    }

    // ==========================================
    // Grammar Session and Evaluation logic
    // ==========================================

    private val _isGrammarActiveSession = MutableStateFlow(false)
    val isGrammarActiveSession: StateFlow<Boolean> = _isGrammarActiveSession.asStateFlow()
    private val _grammarQuizQueue = MutableStateFlow<List<GrammarRule>>(emptyList())
    private val _currentGrammarItem = MutableStateFlow<GrammarRule?>(null)
    val currentGrammarItem: StateFlow<GrammarRule?> = _currentGrammarItem.asStateFlow()
    private val _grammarTotalRoundCount = MutableStateFlow(0)
    val grammarTotalRoundCount: StateFlow<Int> = _grammarTotalRoundCount.asStateFlow()
    private val _grammarCompletedCount = MutableStateFlow(0)
    val grammarCompletedCount: StateFlow<Int> = _grammarCompletedCount.asStateFlow()
    private val _grammarMistakesCount = MutableStateFlow(0)
    val grammarMistakesCount: StateFlow<Int> = _grammarMistakesCount.asStateFlow()
    private val _grammarShowSummary = MutableStateFlow(false)
    val grammarShowSummary: StateFlow<Boolean> = _grammarShowSummary.asStateFlow()
    private val _isGrammarFlashcardRevealed = MutableStateFlow(false)
    val isGrammarFlashcardRevealed: StateFlow<Boolean> = _isGrammarFlashcardRevealed.asStateFlow()
    private val _sessionGrammarMistakes = MutableStateFlow<Map<Int, Int>>(emptyMap())

    fun startGrammarPracticeSession() {
        viewModelScope.launch {
            loadDataSuspend()
            delay(50.milliseconds)
            val filtered = _grammarRules.value.filter {
                it.isEnabled &&
                        matchesLastPracticed(it.lastPracticed, _lastPracticedFilter.value) &&
                        it.confidence in _selectedConfidenceLevels.value
            }
            if (filtered.isEmpty()) return@launch
            val countLimit = _itemCountInput.value.toIntOrNull()?.coerceAtLeast(1) ?: 10
            val picked = filtered.shuffled().take(countLimit)

            _sessionGrammarMistakes.value = emptyMap()
            _grammarQuizQueue.value = picked
            _grammarTotalRoundCount.value = picked.size
            _grammarCompletedCount.value = 0
            _grammarMistakesCount.value = 0
            _grammarShowSummary.value = false
            _isGrammarActiveSession.value = true
            nextGrammarQuestion()
        }
    }

    private fun nextGrammarQuestion() {
        val q = _grammarQuizQueue.value
        if (q.isEmpty()) {
            _currentGrammarItem.value = null
            _grammarShowSummary.value = true
            return
        }
        val next = q.first()
        _currentGrammarItem.value = next
        _isGrammarFlashcardRevealed.value = false
    }

    private fun calculateUpdatedGrammarRule(current: GrammarRule, correct: Boolean): GrammarRule {
        val mistakes = _sessionGrammarMistakes.value.getOrDefault(current.id, 0)
        return if (correct) {
            current.copy(confidence = (current.confidence + 1).coerceAtMost(5), lastPracticed = System.currentTimeMillis())
        } else {
            _sessionGrammarMistakes.value += (current.id to mistakes + 1)
            val diff = if (mistakes == 0) 2 else 1
            current.copy(confidence = (current.confidence - diff).coerceAtLeast(0), lastPracticed = System.currentTimeMillis())
        }
    }

    fun gradeGrammarFlashcard(correct: Boolean) {
        val item = _currentGrammarItem.value ?: return
        val q = _grammarQuizQueue.value.toMutableList()
        val updated = calculateUpdatedGrammarRule(item, correct)
        viewModelScope.launch { repository.updateGrammarRule(updated); loadDataSuspend() }
        q.removeAt(0)
        if (correct) {
            _grammarCompletedCount.value += 1
        } else {
            _grammarMistakesCount.value += 1
            q.add(updated)
        }
        _grammarQuizQueue.value = q
        nextGrammarQuestion()
    }

    fun revealGrammarFlashcard() { _isGrammarFlashcardRevealed.value = true }
    fun leaveGrammarSession() {
        _isGrammarActiveSession.value = false
        _grammarShowSummary.value = false
        _grammarQuizQueue.value = emptyList()
        _currentGrammarItem.value = null
    }
}