package com.ace.jp.japanesepractice.data.model

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {
    @TypeConverter
    fun fromType(type: Type): String = type.name
    
    @TypeConverter
    fun toType(value: String): Type = Type.valueOf(value)

    @TypeConverter
    fun fromGrammarObjectList(value: List<GrammarObject>?): String? {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun toGrammarObjectList(value: String?): List<GrammarObject>? {
        if (value == null) return null
        val listType = object : TypeToken<List<GrammarObject>>() {}.type
        return Gson().fromJson(value, listType)
    }
}
