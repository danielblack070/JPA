package com.ace.jp.app.data.model

import androidx.room.PrimaryKey

data class Meaning(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val text: String,
    val pronunciation: String? = null,
    val sense: String? = null,
    val kanjiMode: Boolean = false
)

fun Char.isConvertibleKatakana(): Boolean =
    this in '\u30A1'..'\u30F6' || this in '\u30FD'..'\u30FE'

fun Char.katakanaToHiraganaChar(): Char =
    if (isConvertibleKatakana()) (this.code - 0x0060).toChar() else this

fun String.containsKatakana(): Boolean =
    any { it.isConvertibleKatakana() }

fun String.katakanaToHiragana(): String =
    map { it.katakanaToHiraganaChar() }.joinToString("")

fun String.formatWithKatakanaFreeMode(katakanaFreeMode: Boolean): String {
    return if (katakanaFreeMode && containsKatakana()) {
        "$this *${katakanaToHiragana()}*"
    } else {
        this
    }
}

fun Meaning.formatDisplay(
    showPronunciation: Boolean = true,
    katakanaFreeMode: Boolean = false
): String {
    val parts = mutableListOf(text.formatWithKatakanaFreeMode(katakanaFreeMode))
    if (showPronunciation && !pronunciation.isNullOrBlank()) {
        parts.add("[$pronunciation]")
    }
    if (!sense.isNullOrBlank()) {
        parts.add("($sense)")
    }
    return parts.joinToString(" ")
}