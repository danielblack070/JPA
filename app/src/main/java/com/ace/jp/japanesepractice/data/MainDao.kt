package com.ace.jp.japanesepractice.data

import androidx.room.*
import com.ace.jp.japanesepractice.data.model.*

@Dao
interface MainDao {
    @Query("SELECT * FROM Word")
    suspend fun getAllWords(): List<Word>

    @Query("SELECT * FROM Word WHERE wordListId = :listId")
    suspend fun getWordsForList(listId: Int): List<Word>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWord(word: Word)

    @Update
    suspend fun updateWord(word: Word)

    @Delete
    suspend fun deleteWord(word: Word)

    @Query("DELETE FROM Word WHERE wordListId = :listId")
    suspend fun deleteWordsFromList(listId: Int)

    @Query("SELECT * FROM WordList")
    suspend fun getAllWordLists(): List<WordList>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWordList(wordList: WordList)

    @Update
    suspend fun updateWordList(wordList: WordList)

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

    @Query("SELECT * FROM SubRule WHERE masterRuleId = :masterRuleId")
    suspend fun getSubRulesForMasterRule(masterRuleId: Int): List<SubRule>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubRule(subRule: SubRule)

    @Update
    suspend fun updateSubRule(subRule: SubRule)

    @Delete
    suspend fun deleteSubRule(subRule: SubRule)

    @Query("DELETE FROM SubRule WHERE masterRuleId = :masterRuleId")
    suspend fun deleteSubRulesForMasterRule(masterRuleId: Int)

    @Query("DELETE FROM WordList")
    suspend fun deleteAllWordLists()

    @Query("DELETE FROM Word")
    suspend fun deleteAllWords()

    @Query("DELETE FROM MasterRule")
    suspend fun deleteAllMasterRules()

    @Query("DELETE FROM SubRule")
    suspend fun deleteAllSubRules()

    @Query("SELECT * FROM GrammarRule")
    suspend fun getAllGrammarRules(): List<GrammarRule>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGrammarRule(grammarRule: GrammarRule)

    @Update
    suspend fun updateGrammarRule(grammarRule: GrammarRule)

    @Delete
    suspend fun deleteGrammarRule(grammarRule: GrammarRule)

    @Query("DELETE FROM GrammarRule")
    suspend fun deleteAllGrammarRules()
}