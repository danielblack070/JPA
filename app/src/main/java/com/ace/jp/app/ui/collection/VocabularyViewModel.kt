package com.ace.jp.app.ui.collection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ace.jp.app.data.Repository
import com.ace.jp.app.data.model.Folder
import com.ace.jp.app.data.model.Type
import com.ace.jp.app.data.model.Meaning
import com.ace.jp.app.data.model.Word
import com.ace.jp.app.data.model.WordList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class FolderEntry {
    abstract val sortOrder: Int
    abstract val key: String

    data class FolderItem(val folder: Folder) : FolderEntry() {
        override val sortOrder: Int get() = folder.sortOrder
        override val key: String get() = "folder_${folder.id}"
    }

    data class WordListItem(val wordList: WordList) : FolderEntry() {
        override val sortOrder: Int get() = wordList.sortOrder
        override val key: String get() = "list_${wordList.id}"
    }
}

class VocabularyViewModel(private val repository: Repository) : ViewModel() {
    private val _words = MutableStateFlow<List<Word>>(emptyList())
    val words: StateFlow<List<Word>> = _words

    private val _wordLists = MutableStateFlow<List<WordList>>(emptyList())
    val wordLists: StateFlow<List<WordList>> = _wordLists

    private val _folders = MutableStateFlow<List<Folder>>(emptyList())
    val folders: StateFlow<List<Folder>> = _folders

    private val _currentFolderId = MutableStateFlow<Int?>(null)
    val currentFolderId: StateFlow<Int?> = _currentFolderId

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _words.value = repository.getAllWords()
            _wordLists.value = repository.getAllWordLists()
            _folders.value = repository.getAllFolders()
            val activeFolderId = _currentFolderId.value
            if (activeFolderId != null && _folders.value.none { it.id == activeFolderId }) {
                _currentFolderId.value = null
            }
        }
    }

    fun openFolder(folderId: Int?) {
        _currentFolderId.value = folderId
    }

    fun navigateUpFolder() {
        val currentId = _currentFolderId.value ?: return
        val parentId = _folders.value.firstOrNull { it.id == currentId }?.parentFolderId
        _currentFolderId.value = parentId
    }

    private fun nextSortOrderInFolder(parentFolderId: Int?): Int {
        val maxFolder = _folders.value
            .filter { it.parentFolderId == parentFolderId }
            .maxOfOrNull { it.sortOrder } ?: -1
        val maxList = _wordLists.value
            .filter { it.folderId == parentFolderId }
            .maxOfOrNull { it.sortOrder } ?: -1
        return maxOf(maxFolder, maxList) + 1
    }

    // --- FOLDER ACTIONS ---
    fun addFolder(name: String, parentFolderId: Int?) {
        viewModelScope.launch {
            repository.insertFolder(
                Folder(
                    name = name,
                    parentFolderId = parentFolderId,
                    sortOrder = nextSortOrderInFolder(parentFolderId)
                )
            )
            loadData()
        }
    }

    fun updateFolder(folder: Folder) {
        viewModelScope.launch {
            val existing = _folders.value.firstOrNull { it.id == folder.id }
            val finalFolder = if (existing != null && existing.parentFolderId != folder.parentFolderId) {
                folder.copy(sortOrder = nextSortOrderInFolder(folder.parentFolderId))
            } else {
                folder
            }
            repository.updateFolder(finalFolder)
            loadData()
        }
    }

    fun deleteFolder(folder: Folder) {
        viewModelScope.launch {
            repository.deleteFolder(folder)
            loadData()
        }
    }

    // --- WORD LIST ACTIONS ---
    fun addWordList(name: String, folderId: Int? = _currentFolderId.value) {
        viewModelScope.launch {
            repository.insertWordList(
                WordList(
                    name = name,
                    folderId = folderId,
                    sortOrder = nextSortOrderInFolder(folderId)
                )
            )
            loadData()
        }
    }

    fun updateWordList(wordList: WordList) {
        viewModelScope.launch {
            val existing = _wordLists.value.firstOrNull { it.id == wordList.id }
            val finalList = if (existing != null && existing.folderId != wordList.folderId) {
                wordList.copy(sortOrder = nextSortOrderInFolder(wordList.folderId))
            } else {
                wordList
            }
            repository.updateWordList(finalList)
            loadData()
        }
    }

    fun deleteWordList(wordList: WordList) {
        viewModelScope.launch {
            repository.deleteWordList(wordList)
            loadData()
        }
    }

    fun reorderFolderEntries(reorderedEntries: List<FolderEntry>) {
        viewModelScope.launch {
            val updatedFolders = mutableListOf<Folder>()
            val updatedLists = mutableListOf<WordList>()
            reorderedEntries.forEachIndexed { index, entry ->
                when (entry) {
                    is FolderEntry.FolderItem -> updatedFolders.add(entry.folder.copy(sortOrder = index))
                    is FolderEntry.WordListItem -> updatedLists.add(entry.wordList.copy(sortOrder = index))
                }
            }
            if (updatedFolders.isNotEmpty()) repository.updateFolders(updatedFolders)
            if (updatedLists.isNotEmpty()) repository.updateWordLists(updatedLists)
            loadData()
        }
    }

    // --- WORD ACTIONS ---
    fun addWord(wordListId: Int, japanese: List<Meaning>, english: List<Meaning>, type: Type, notes: String?) {
        viewModelScope.launch {
            val nextOrder = (_words.value.filter { it.wordListId == wordListId }.maxOfOrNull { it.sortOrder } ?: -1) + 1
            repository.insertWord(
                Word(
                    wordListId = wordListId,
                    japanese = japanese,
                    english = english,
                    type = type,
                    notes = notes,
                    sortOrder = nextOrder
                )
            )
            loadData()
        }
    }

    fun updateWord(word: Word) {
        viewModelScope.launch {
            val existing = _words.value.firstOrNull { it.id == word.id }
            val finalWord = if (existing != null && existing.wordListId != word.wordListId) {
                val nextOrder = (_words.value.filter { it.wordListId == word.wordListId }.maxOfOrNull { it.sortOrder } ?: -1) + 1
                word.copy(sortOrder = nextOrder)
            } else {
                word
            }
            repository.updateWord(finalWord)
            loadData()
        }
    }

    fun reorderWords(reorderedWords: List<Word>) {
        viewModelScope.launch {
            val updated = reorderedWords.mapIndexed { index, word ->
                word.copy(sortOrder = index)
            }
            if (updated.isNotEmpty()) {
                repository.updateWords(updated)
            }
            loadData()
        }
    }

    fun deleteWord(word: Word) {
        viewModelScope.launch {
            repository.deleteWord(word)
            loadData()
        }
    }

    // --- CSV / TXT IMPORT ENGINE ---
    fun importWordsFromContent(wordListId: Int, textContent: String) {
        viewModelScope.launch {
            var nextOrder = (_words.value.filter { it.wordListId == wordListId }.maxOfOrNull { it.sortOrder } ?: -1) + 1
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
                                japanese = listOf(Meaning(text = japanese, pronunciation = reading)),
                                english = listOf(Meaning(text = english)),
                                type = type,
                                notes = notes,
                                sortOrder = nextOrder++
                            )
                        )
                    }
                }
            }
            loadData()
        }
    }
}