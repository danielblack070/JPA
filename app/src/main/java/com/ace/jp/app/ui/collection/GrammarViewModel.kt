package com.ace.jp.app.ui.collection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ace.jp.app.data.Repository
import com.ace.jp.app.data.model.GrammarRule
import com.ace.jp.app.data.model.ExampleSentence
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class GrammarViewModel(
    private val repository: Repository
) : ViewModel() {
    private val _grammarRules = MutableStateFlow<List<GrammarRule>>(emptyList())
    val grammarRules: StateFlow<List<GrammarRule>> = _grammarRules

    private val _exampleSentences = MutableStateFlow<Map<Int, List<ExampleSentence>>>(emptyMap())
    val exampleSentences: StateFlow<Map<Int, List<ExampleSentence>>> = _exampleSentences

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _grammarRules.value = repository.getAllGrammarRules()
            val allExamples = repository.getAllExamples()
            _exampleSentences.value = allExamples.groupBy { it.grammarRuleId }
        }
    }

    // --- GRAMMAR RULE ACTIONS ---
    fun addGrammarRule(
        name: String,
        englishRule: String,
        japaneseRule: String,
        readingRule: String?,
        notes: String?
    ) {
        viewModelScope.launch {
            repository.insertGrammarRule(
                GrammarRule(
                    name = name,
                    englishRule = englishRule,
                    japaneseRule = japaneseRule,
                    readingRule = readingRule,
                    notes = notes
                )
            )
            loadData()
        }
    }

    fun updateGrammarRule(grammarRule: GrammarRule) {
        viewModelScope.launch {
            repository.updateGrammarRule(grammarRule)
            loadData()
        }
    }

    fun deleteGrammarRule(grammarRule: GrammarRule) {
        viewModelScope.launch {
            repository.deleteGrammarRule(grammarRule)
            loadData()
        }
    }

    fun deleteAllGrammarRules() {
        viewModelScope.launch {
            repository.deleteAllGrammarRules()
            loadData()
        }
    }

    // --- EXAMPLE SENTENCE ACTIONS ---
    fun addExampleSentence(
        grammarRuleId: Int,
        english: String,
        japanese: String,
        reading: String?
    ) {
        viewModelScope.launch {
            repository.insertExample(
                ExampleSentence(
                    grammarRuleId = grammarRuleId,
                    english = english,
                    japanese = japanese,
                    reading = reading
                )
            )
            loadData()
        }
    }

    fun deleteExampleSentence(exampleSentence: ExampleSentence) {
        viewModelScope.launch {
            repository.deleteExample(exampleSentence)
            loadData()
        }
    }
}
