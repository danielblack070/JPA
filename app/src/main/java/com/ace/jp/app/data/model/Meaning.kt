package com.ace.jp.app.data.model

import androidx.room.PrimaryKey

data class Meaning(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val text: String,
    val pronunciation: String? = null,
    val sense: String? = null
)

fun Meaning.formatDisplay(showPronunciation: Boolean = true): String {
    val parts = mutableListOf(text)
    if (showPronunciation && !pronunciation.isNullOrBlank()) {
        parts.add("[$pronunciation]")
    }
    if (!sense.isNullOrBlank()) {
        parts.add("($sense)")
    }
    return parts.joinToString(" ")
}