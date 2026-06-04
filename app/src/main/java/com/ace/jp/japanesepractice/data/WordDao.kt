package com.ace.jp.japanesepractice.data

import androidx.room.*
import com.ace.jp.japanesepractice.data.model.Word

@Dao
interface WordDao {
    @Query("SELECT * FROM Word WHERE wordListId = :listId")
    suspend fun getWordsForList(listId: Int): List<Word>

    @Query("SELECT * FROM Word")
    suspend fun getAllWords(): List<Word>

    @Insert
    suspend fun insert(word: Word)

    @Update
    suspend fun update(word: Word)

    @Delete
    suspend fun delete(word: Word)

    @Query("DELETE FROM Word WHERE wordListId = :listId")
    suspend fun deleteWordsFromList(listId: Int)

}
