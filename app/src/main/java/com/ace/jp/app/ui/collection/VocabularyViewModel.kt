package com.ace.jp.app.ui.collection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ace.jp.app.data.Repository
import com.ace.jp.app.data.model.Type
import com.ace.jp.app.data.model.Word
import com.ace.jp.app.data.model.WordList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class VocabularyViewModel(private val repository: Repository) : ViewModel() {
    private val _words = MutableStateFlow<List<Word>>(emptyList())
    val words: StateFlow<List<Word>> = _words

    private val _wordLists = MutableStateFlow<List<WordList>>(emptyList())
    val wordLists: StateFlow<List<WordList>> = _wordLists

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _words.value = repository.getAllWords()
            _wordLists.value = repository.getAllWordLists()
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
            repository.insertWord(
                Word(
                    wordListId = wordListId,
                    japanese = japanese,
                    reading = reading,
                    english = english,
                    type = type,
                    notes = notes
                )
            )
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
                        repository.insertWord(
                            Word(
                                wordListId = wordListId,
                                japanese = japanese,
                                reading = reading,
                                english = english,
                                type = type,
                                notes = notes
                            )
                        )
                    }
                }
            }
            loadData()
        }
    }
}