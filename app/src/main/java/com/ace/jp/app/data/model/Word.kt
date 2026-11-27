package com.ace.jp.app.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    foreignKeys = [ForeignKey(
        entity = WordList::class,
        parentColumns = ["id"],
        childColumns = ["wordListId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("wordListId")]
)
data class Word(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val wordListId: Int,
    val japanese: List<Meaning>,
    val english: List<Meaning>,
    val type: Type,
    val isEnabled: Boolean = true,
    val notes: String?,
    val confidence: Int = 0,
    val lastPracticed: Long? = null,
    val sortOrder: Int = 0
)
