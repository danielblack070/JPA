package com.ace.jp.app.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    foreignKeys = [ForeignKey(
        entity = Folder::class,
        parentColumns = ["id"],
        childColumns = ["folderId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("folderId")]
)
data class WordList(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val isEnabled: Boolean = true,
    val folderId: Int? = null,
    val sortOrder: Int = 0
)
