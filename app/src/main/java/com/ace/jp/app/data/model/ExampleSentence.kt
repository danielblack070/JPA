package com.ace.jp.app.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = GrammarRule::class,
            parentColumns = ["id"],
            childColumns = ["grammarRuleId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["grammarRuleId"])]
)
data class ExampleSentence(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val grammarRuleId: Int,
    val english: String,
    val japanese: String,
    val reading: String? = null
)
