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

    private val _subRules = MutableStateFlow<List<SubRule>>(emptyList())
    val subRules: StateFlow<List<SubRule>> = _subRules

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
            _subRules.value = repository.getAllSubRules()
            _grammarRules.value = repository.getAllGrammarRules()
        }
    }

    // --- WORD LIST ACTIONS ---
    fun addWordList(name: String) {
        viewModelScope.launch {
            repository.insertWordList(WordList(name = name))
            loadData()
        }
    }

    fun updateWordList(wordList: WordList) {
        viewModelScope.launch {
            repository.updateWordList(wordList)
            loadData()
        }
    }

    fun deleteWordList(wordList: WordList) {
        viewModelScope.launch {
            repository.deleteWordList(wordList)
            loadData()
        }
    }

    fun deleteAllWordLists() {
        viewModelScope.launch {
            repository.deleteAllWordLists()
            loadData()
        }
    }

    // --- WORD ACTIONS ---
    fun addWord(wordListId: Int, japanese: String, reading: String?, english: String, type: Type, notes: String?) {
        viewModelScope.launch {
            repository.insertWord(Word(wordListId = wordListId, japanese = japanese, reading = reading, english = english, type = type, notes = notes))
            loadData()
        }
    }

    fun updateWord(word: Word) {
        viewModelScope.launch {
            repository.updateWord(word)
            loadData()
        }
    }

    fun deleteWord(word: Word) {
        viewModelScope.launch {
            repository.deleteWord(word)
            loadData()
        }
    }

    fun deleteWordsFromList(listId: Int) {
        viewModelScope.launch {
            repository.deleteWordsFromList(listId)
            loadData()
        }
    }

    // --- MASTER RULE ACTIONS ---
    fun addMasterRule(name: String) {
        viewModelScope.launch {
            repository.insertMasterRule(MasterRule(name = name))
            loadData()
        }
    }

    fun updateMasterRule(masterRule: MasterRule) {
        viewModelScope.launch {
            repository.updateMasterRule(masterRule)
            loadData()
        }
    }

    fun deleteMasterRule(masterRule: MasterRule) {
        viewModelScope.launch {
            repository.deleteMasterRule(masterRule)
            loadData()
        }
    }

    fun deleteAllMasterRules() {
        viewModelScope.launch {
            repository.deleteAllMasterRules()
            loadData()
        }
    }

    // --- SUB-RULE ACTIONS ---
    fun addSubRule(masterRuleId: Int, description: String, type: Type, originalEnding: String, newEnding: String, isUnique: Boolean) {
        viewModelScope.launch {
            repository.insertSubRule(SubRule(masterRuleId = masterRuleId, description = description, type = type, originalEnding = originalEnding, newEnding = newEnding, isUnique = isUnique))
            loadData()
        }
    }

    fun updateSubRule(subRule: SubRule) {
        viewModelScope.launch {
            repository.updateSubRule(subRule)
            loadData()
        }
    }

    fun deleteSubRule(subRule: SubRule) {
        viewModelScope.launch {
            repository.deleteSubRule(subRule)
            loadData()
        }
    }

    fun deleteSubRulesForMasterRule(masterRuleId: Int) {
        viewModelScope.launch {
            repository.deleteSubRulesForMasterRule(masterRuleId)
            loadData()
        }
    }

    // --- GRAMMAR RULE ACTIONS ---
    fun addGrammarRule(description: String, example: String?, rule: List<GrammarObject>) {
        viewModelScope.launch {
            repository.insertGrammarRule(GrammarRule(description = description, example = example, rule = rule))
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

    // --- CSV / TXT IMPORT ENGINE ---
    fun importWordsFromContent(wordListId: Int, textContent: String) {
        viewModelScope.launch {
            val lines = textContent.split("\n")
            for (line in lines) {
                if (line.isBlank()) continue

                // Matches either comma or semicolon splits
                val parts = line.split(Regex("[,;]")).map { it.trim() }
                if (parts.size >= 2) {
                    val japanese = parts[0]
                    var reading: String? = null
                    var english = ""
                    var type = Type.Noun
                    var notes: String? = null

                    if (parts.size == 2) {
                        english = parts[1]
                    } else if (parts.size == 3) {
                        // Check if 3rd item is an Enum display
                        val parsedType = runCatching { Type.fromDisplayString(parts[2]) }.getOrNull()
                        if (parsedType != null) {
                            english = parts[1]
                            type = parsedType
                        } else {
                            reading = parts[1].ifBlank { null }
                            english = parts[2]
                        }
                    } else if (parts.size == 4) {
                        reading = parts[1].ifBlank { null }
                        english = parts[2]
                        type = runCatching { Type.fromDisplayString(parts[3]) }.getOrNull() ?: Type.Noun
                    } else if (parts.size >= 5) {
                        reading = parts[1].ifBlank { null }
                        english = parts[2]
                        type = runCatching { Type.fromDisplayString(parts[3]) }.getOrNull() ?: Type.Noun
                        notes = parts[4].ifBlank { null }
                    }

                    if (japanese.isNotBlank() && english.isNotBlank()) {
                        repository.insertWord(Word(
                            wordListId = wordListId,
                            japanese = japanese,
                            reading = reading,
                            english = english,
                            type = type,
                            notes = notes
                        ))
                    }
                }
            }
            loadData()
        }
    }
}