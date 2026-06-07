package com.ace.jp.japanesepractice.data.model

enum class Type {
    Noun, UVerb, RuVerb, IrrVerb, IAdjective, NaAdjective, YoAdjective, Adverb, Other;

    fun toDisplayString(): String = when (this) {
        Noun -> "Noun"
        UVerb -> "U-Verb"
        RuVerb -> "Ru-Verb"
        IrrVerb -> "Irr-Verb"
        IAdjective -> "I-Adjective"
        NaAdjective -> "Na-Adjective"
        YoAdjective -> "Yo-Adjective"
        Adverb -> "Adverb"
        Other -> "Other"
    }

    companion object {
        fun fromDisplayString(display: String): Type {
            return entries.firstOrNull { it.toDisplayString().equals(display, ignoreCase = true) } ?: Noun
        }
    }
}