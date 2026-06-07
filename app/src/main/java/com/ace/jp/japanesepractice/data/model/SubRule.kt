package com.ace.jp.japanesepractice.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    foreignKeys = [ForeignKey(
        entity = MasterRule::class,
        parentColumns = ["id"],
        childColumns = ["masterRuleId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("masterRuleId")]
)
data class SubRule(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val masterRuleId: Int,
    val description: String,
    val type: Type,
    val originalEnding: String,
    val newEnding: String,
    val isUnique: Boolean,
    val isEnabled: Boolean = true
)