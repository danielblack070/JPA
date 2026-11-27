package com.ace.jp.app.data.model

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {
    private val gson = Gson()
    private val meaningListType = object : TypeToken<List<Meaning>>() {}.type
    @TypeConverter
    fun fromType(type: Type): String = type.name

    @TypeConverter
    fun toType(value: String): Type = Type.valueOf(value)

    @TypeConverter
    fun fromMeaningList(meanings: List<Meaning>): String = gson.toJson(meanings)

    @TypeConverter
    fun toMeaningList(value: String): List<Meaning> {
        if (value.isBlank()) return emptyList()
        return gson.fromJson(value, meaningListType) ?: emptyList()
    }
}