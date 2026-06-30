package com.ace.jp.japanesepractice.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class GrammarRule(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val isEnabled: Boolean = true,
    val confidence: Int = 0,
    val lastPracticed: Long? = null,
    val englishRule: String,
    val japaneseRule: String,
    val readingRule: String? = null,
    val notes: String? = null
)
