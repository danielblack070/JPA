package com.ace.jp.japanesepractice.data

import com.ace.jp.japanesepractice.data.model.*

class Repository(private val dao: MainDao) {
    suspend fun getAllWords() = dao.getAllWords()
    suspend fun insertWord(word: Word) = dao.insertWord(word)
    suspend fun updateWord(word: Word) = dao.updateWord(word)
    suspend fun deleteWord(word: Word) = dao.deleteWord(word)
    suspend fun deleteWordsFromList(listId: Int) = dao.deleteWordsFromList(listId)

    suspend fun getAllWordLists() = dao.getAllWordLists()
    suspend fun insertWordList(wordList: WordList) = dao.insertWordList(wordList)
    suspend fun updateWordList(wordList: WordList) = dao.updateWordList(wordList)
    suspend fun deleteWordList(wordList: WordList) = dao.deleteWordList(wordList)

    suspend fun deleteAllWordLists() = dao.deleteAllWordLists()
    suspend fun deleteAllMasterRules() = dao.deleteAllMasterRules()
    suspend fun deleteAllGrammarRules() = dao.deleteAllGrammarRules()

    suspend fun getAllMasterRules() = dao.getAllMasterRules()
    suspend fun insertMasterRule(masterRule: MasterRule) = dao.insertMasterRule(masterRule)
    suspend fun updateMasterRule(masterRule: MasterRule) = dao.updateMasterRule(masterRule)
    suspend fun deleteMasterRule(masterRule: MasterRule) = dao.deleteMasterRule(masterRule)

    suspend fun getAllSubRules() = dao.getAllSubRules()
    suspend fun getSubRulesForMasterRule(masterRuleId: Int) = dao.getSubRulesForMasterRule(masterRuleId)
    suspend fun insertSubRule(subRule: SubRule) = dao.insertSubRule(subRule)
    suspend fun updateSubRule(subRule: SubRule) = dao.updateSubRule(subRule)
    suspend fun deleteSubRule(subRule: SubRule) = dao.deleteSubRule(subRule)
    suspend fun deleteSubRulesForMasterRule(masterRuleId: Int) = dao.deleteSubRulesForMasterRule(masterRuleId)

    suspend fun getAllGrammarRules() = dao.getAllGrammarRules()
    suspend fun insertGrammarRule(grammarRule: GrammarRule) = dao.insertGrammarRule(grammarRule)
    suspend fun updateGrammarRule(grammarRule: GrammarRule) = dao.updateGrammarRule(grammarRule)
    suspend fun deleteGrammarRule(grammarRule: GrammarRule) = dao.deleteGrammarRule(grammarRule)
}