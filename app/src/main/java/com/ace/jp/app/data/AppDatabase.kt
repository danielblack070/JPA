package com.ace.jp.app.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.ace.jp.app.data.model.*

@Database(entities = [Folder::class, Word::class, WordList::class, MasterRule::class, SubRule::class, GrammarRule::class, ExampleSentence::class], version = 5, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun mainDao(): MainDao
}
