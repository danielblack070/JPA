package com.ace.jp.app.data.model

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromType(type: Type): String = type.name

    @TypeConverter
    fun toType(value: String): Type = Type.valueOf(value)
}