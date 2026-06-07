package com.ace.jp.japanesepractice.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.ace.jp.japanesepractice.data.model.*

@Database(entities = [Word::class, WordList::class, MasterRule::class, SubRule::class, GrammarRule::class], version = 2)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun mainDao(): MainDao
}
