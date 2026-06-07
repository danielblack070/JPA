package com.ace.jp.japanesepractice.data.model

sealed class GrammarObject(val name: String)

data class DynamicObject(
    val broadType: BroadType,
    val form: MasterRule? = null
) : GrammarObject(broadType.name + (form?.let { " - ${it.name}" } ?: ""))

data class FixedObject(
    val word: String
) : GrammarObject(word)