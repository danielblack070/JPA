package com.ace.jp.japanesepractice.ui.collection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ace.jp.japanesepractice.data.Repository
import com.ace.jp.japanesepractice.data.model.GrammarObject
import com.ace.jp.japanesepractice.data.model.GrammarRule
import com.ace.jp.japanesepractice.data.model.MasterRule
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class GrammarViewModel(private val repository: Repository) : ViewModel() {
    private val _grammarRules = MutableStateFlow<List<GrammarRule>>(emptyList())
    val grammarRules: StateFlow<List<GrammarRule>> = _grammarRules

    // Required by grammar dialog to match with available Conjugations/MasterRules
    private val _masterRules = MutableStateFlow<List<MasterRule>>(emptyList())
    val masterRules: StateFlow<List<MasterRule>> = _masterRules

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _grammarRules.value = repository.getAllGrammarRules()
            _masterRules.value = repository.getAllMasterRules()
        }
    }

    // --- GRAMMAR RULE ACTIONS ---
    fun addGrammarRule(description: String, englishExample: String?, japaneseExample: String?, rule: List<GrammarObject>) {
        viewModelScope.launch {
            repository.insertGrammarRule(
                GrammarRule(
                    description = description,
                    englishExample = englishExample,
                    japaneseExample = japaneseExample,
                    rule = rule
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
}