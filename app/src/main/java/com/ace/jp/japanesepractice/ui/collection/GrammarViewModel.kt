package com.ace.jp.japanesepractice.ui.collection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ace.jp.japanesepractice.data.Repository
import com.ace.jp.japanesepractice.data.model.GrammarRule
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class GrammarViewModel(private val repository: Repository) : ViewModel() {
    private val _grammarRules = MutableStateFlow<List<GrammarRule>>(emptyList())
    val grammarRules: StateFlow<List<GrammarRule>> = _grammarRules

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _grammarRules.value = repository.getAllGrammarRules()
        }
    }

    // --- GRAMMAR RULE ACTIONS ---
    fun addGrammarRule(description: String, details: String) {
        viewModelScope.launch {
            repository.insertGrammarRule(
                GrammarRule(
                    description = description,
                    details = details
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
