package com.ace.jp.japanesepractice.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class MasterRule(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val isEnabled: Boolean = true,
    val confidence: Int = 0,
    val lastPracticed: Long? = null
)
