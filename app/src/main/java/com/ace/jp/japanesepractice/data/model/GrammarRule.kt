package com.ace.jp.japanesepractice.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters

@Entity
data class GrammarRule(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val description: String,
    @param:TypeConverters(Converters::class) val rule: List<GrammarObject>,
    val example: String?,
    val isEnabled: Boolean = true
)
