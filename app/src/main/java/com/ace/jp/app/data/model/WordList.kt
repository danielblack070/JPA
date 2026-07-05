package com.ace.jp.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class WordList(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val isEnabled: Boolean = true
)
