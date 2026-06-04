package com.ace.jp.japanesepractice.ui.collection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ace.jp.japanesepractice.data.Repository
import com.ace.jp.japanesepractice.data.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CollectionViewModel(private val repository: Repository) : ViewModel() {
    private val _words = MutableStateFlow<List<Word>>(emptyList())
    val words: StateFlow<List<Word>> = _words

    private val _wordLists = MutableStateFlow<List<WordList>>(emptyList())
    val wordLists: StateFlow<List<WordList>> = _wordLists

    private val _masterRules = MutableStateFlow<List<MasterRule>>(emptyList())
    val masterRules: StateFlow<List<MasterRule>> = _masterRules

    private val _grammarRules = MutableStateFlow<List<GrammarRule>>(emptyList())
    val grammarRules: StateFlow<List<GrammarRule>> = _grammarRules

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _words.value = repository.getAllWords()
            _wordLists.value = repository.getAllWordLists()
            _masterRules.value = repository.getAllMasterRules()
            _grammarRules.value = repository.getAllGrammarRules()
        }
    }

    fun addWordList(name: String) {
        viewModelScope.launch {
            repository.insertWordList(WordList(name = name))
            _wordLists.value = repository.getAllWordLists()
        }
    }

    fun addWord(wordListId: Int, japanese: String, reading: String?, english: String, type: Type, notes: String?) {
        viewModelScope.launch {
            repository.insertWord(Word(wordListId = wordListId, japanese = japanese, reading = reading, english = english, type = type, notes = notes))
            _words.value = repository.getAllWords()
        }
    }

    fun addMasterRule(name: String, type: Type) {
        viewModelScope.launch {
            repository.insertMasterRule(MasterRule(name = name, type = type))
            _masterRules.value = repository.getAllMasterRules()
        }
    }

    fun addSubRule(masterRuleId: Int, description: String, originalEnding: String, newEnding: String, isUnique: Boolean) {
        viewModelScope.launch {
            repository.insertSubRule(SubRule(masterRuleId = masterRuleId, description = description, originalEnding = originalEnding, newEnding = newEnding, isUnique = isUnique))
            loadData()
        }
    }

    fun deleteAllWordLists() {
        viewModelScope.launch {
            repository.deleteAllWordLists()
            loadData()
        }
    }

    fun deleteAllMasterRules() {
        viewModelScope.launch {
            repository.deleteAllMasterRules()
            loadData()
        }
    }

    fun deleteAllGrammarRules() {
        viewModelScope.launch {
            repository.deleteAllGrammarRules()
            loadData()
        }
    }

    fun addGrammarRule(description: String, example: String?, rule: List<GrammarObject>) {
        viewModelScope.launch {
            repository.insertGrammarRule(GrammarRule(description = description, example = example, rule = rule))
            _grammarRules.value = repository.getAllGrammarRules()
        }
    }
}
