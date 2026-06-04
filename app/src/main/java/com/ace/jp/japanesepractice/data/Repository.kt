package com.ace.jp.japanesepractice.data

import com.ace.jp.japanesepractice.data.model.*

class Repository(private val dao: MainDao) {
    suspend fun getAllWords() = dao.getAllWords()
    suspend fun insertWord(word: Word) = dao.insertWord(word)
    suspend fun deleteWordsFromList(listId: Int) = dao.deleteWordsFromList(listId)
    
    suspend fun getAllWordLists() = dao.getAllWordLists()
    suspend fun insertWordList(wordList: WordList) = dao.insertWordList(wordList)
    suspend fun deleteAllWordLists() = dao.deleteAllWordLists()
    suspend fun deleteAllMasterRules() = dao.deleteAllMasterRules()
    suspend fun deleteAllGrammarRules() = dao.deleteAllGrammarRules()


    
    suspend fun getAllMasterRules() = dao.getAllMasterRules()
    suspend fun insertMasterRule(masterRule: MasterRule) = dao.insertMasterRule(masterRule)
    
    suspend fun getSubRulesForMasterRule(masterRuleId: Int) = dao.getSubRulesForMasterRule(masterRuleId)
    suspend fun insertSubRule(subRule: SubRule) = dao.insertSubRule(subRule)
    
    suspend fun getAllGrammarRules() = dao.getAllGrammarRules()
    suspend fun insertGrammarRule(grammarRule: GrammarRule) = dao.insertGrammarRule(grammarRule)
}
