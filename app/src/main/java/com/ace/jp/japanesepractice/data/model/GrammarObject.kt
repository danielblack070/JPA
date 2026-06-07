package com.ace.jp.japanesepractice.data.model

sealed class GrammarObject(val name: String)

data class DynamicObject(
    val broadType: BroadType,
    val formId: Int? = null
) : GrammarObject(broadType.name + (formId?.toString() ?: ""))

data class FixedObject(
    val word: String
) : GrammarObject(word)
