package com.ace.jp.japanesepractice.data

import androidx.room.*
import com.ace.jp.japanesepractice.data.model.*

@Dao
interface MainDao {
    @Query("SELECT * FROM Word")
    suspend fun getAllWords(): List<Word>

    @Query("SELECT * FROM Word WHERE wordListId = :listId")
    suspend fun getWordsForList(listId: Int): List<Word>

    @Insert
    suspend fun insertWord(word: Word)

    @Update
    suspend fun updateWord(word: Word)

    @Delete
    suspend fun deleteWord(word: Word)

    @Query("DELETE FROM Word WHERE wordListId = :listId")
    suspend fun deleteWordsFromList(listId: Int)


    @Query("SELECT * FROM WordList")
    suspend fun getAllWordLists(): List<WordList>

    @Insert
    suspend fun insertWordList(wordList: WordList)

    @Query("SELECT * FROM MasterRule")
    suspend fun getAllMasterRules(): List<MasterRule>

    @Insert
    suspend fun insertMasterRule(masterRule: MasterRule)

    @Query("SELECT * FROM SubRule WHERE masterRuleId = :masterRuleId")
    suspend fun getSubRulesForMasterRule(masterRuleId: Int): List<SubRule>

    @Insert
    suspend fun insertSubRule(subRule: SubRule)
    
    @Query("DELETE FROM WordList")
    suspend fun deleteAllWordLists()

    @Query("DELETE FROM Word")
    suspend fun deleteAllWords()

    @Query("DELETE FROM MasterRule")
    suspend fun deleteAllMasterRules()

    @Query("DELETE FROM SubRule WHERE masterRuleId = :masterRuleId")
    suspend fun deleteSubRulesForMasterRule(masterRuleId: Int)

    @Query("DELETE FROM SubRule")
    suspend fun deleteAllSubRules()

    @Query("SELECT * FROM GrammarRule")
    suspend fun getAllGrammarRules(): List<GrammarRule>

    @Insert
    suspend fun insertGrammarRule(grammarRule: GrammarRule)

    @Query("DELETE FROM GrammarRule")
    suspend fun deleteAllGrammarRules()


}
