package com.ace.jp.japanesepractice.data.model

sealed class GrammarObject(val name: String)

data class DynamicObject(
    val type: Type,
    val formId: Int? = null
) : GrammarObject(type.name + (formId?.toString() ?: ""))

data class FixedObject(
    val word: String
) : GrammarObject(word)
