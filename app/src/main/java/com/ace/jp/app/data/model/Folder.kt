package com.ace.jp.app.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    foreignKeys = [ForeignKey(
        entity = Folder::class,
        parentColumns = ["id"],
        childColumns = ["parentFolderId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("parentFolderId")]
)
data class Folder(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val parentFolderId: Int? = null,
    val sortOrder: Int = 0
)