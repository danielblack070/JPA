package com.ace.jp.app.data.model

enum class Type {
    Noun, UVerb, RuVerb, IrrVerb, IAdjective, NaAdjective, IrrAdjective, Other;

    fun toDisplayString(): String = when (this) {
        Noun -> "Noun"
        UVerb -> "U-Verb"
        RuVerb -> "Ru-Verb"
        IrrVerb -> "Irregular Verb"
        IAdjective -> "I-Adjective"
        NaAdjective -> "Na-Adjective"
        IrrAdjective -> "Irregular I-Adjective"
        Other -> "Other"
    }

    companion object {
        fun fromDisplayString(display: String): Type {
            return entries.firstOrNull { it.toDisplayString().equals(display, ignoreCase = true) } ?: Noun
        }
    }
}