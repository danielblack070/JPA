package com.ace.jp.app.data

import androidx.room.*
import com.ace.jp.app.data.model.*

@Dao
interface MainDao {
    @Query("SELECT * FROM Word ORDER BY sortOrder ASC, id ASC")
    suspend fun getAllWords(): List<Word>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWord(word: Word)

    @Update
    suspend fun updateWord(word: Word)

    @Update
    suspend fun updateWords(words: List<Word>)

    @Delete
    suspend fun deleteWord(word: Word)

    @Query("DELETE FROM Word WHERE wordListId = :listId")
    suspend fun deleteWordsFromList(listId: Int)

    @Query("SELECT * FROM Folder ORDER BY sortOrder ASC, id ASC")
    suspend fun getAllFolders(): List<Folder>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFolder(folder: Folder)

    @Update
    suspend fun updateFolder(folder: Folder)

    @Update
    suspend fun updateFolders(folders: List<Folder>)

    @Delete
    suspend fun deleteFolder(folder: Folder)

    @Query("SELECT * FROM WordList ORDER BY sortOrder ASC, id ASC")
    suspend fun getAllWordLists(): List<WordList>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWordList(wordList: WordList)

    @Update
    suspend fun updateWordList(wordList: WordList)

    @Update
    suspend fun updateWordLists(wordLists: List<WordList>)

    @Delete
    suspend fun deleteWordList(wordList: WordList)
    @Query("SELECT * FROM MasterRule")
    suspend fun getAllMasterRules(): List<MasterRule>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMasterRule(masterRule: MasterRule)

    @Update
    suspend fun updateMasterRule(masterRule: MasterRule)

    @Delete
    suspend fun deleteMasterRule(masterRule: MasterRule)

    @Query("SELECT * FROM SubRule")
    suspend fun getAllSubRules(): List<SubRule>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubRule(subRule: SubRule)

    @Update
    suspend fun updateSubRule(subRule: SubRule)

    @Delete
    suspend fun deleteSubRule(subRule: SubRule)

    // GrammarRule
    @Query("SELECT * FROM GrammarRule")
    suspend fun getAllGrammarRules(): List<GrammarRule>

    @Insert
    suspend fun insertGrammarRule(grammarRule: GrammarRule)

    @Update
    suspend fun updateGrammarRule(grammarRule: GrammarRule)

    @Delete
    suspend fun deleteGrammarRule(grammarRule: GrammarRule)

    // ExampleSentence (Added here to use the existing MainDao instead of creating a new one)
    @Query("SELECT * FROM ExampleSentence")
    suspend fun getAllExamples(): List<ExampleSentence>

    @Insert
    suspend fun insertExample(example: ExampleSentence)

    @Update
    suspend fun updateExample(example: ExampleSentence)

    @Delete
    suspend fun deleteExample(example: ExampleSentence)
}