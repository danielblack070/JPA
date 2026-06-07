package com.ace.jp.japanesepractice.data.model

import androidx.room.TypeConverter
import com.google.gson.*
import com.google.gson.reflect.TypeToken

class Converters {
    private val gson: Gson = GsonBuilder()
        // FIX: Using registerTypeHierarchyAdapter instead of registerTypeAdapter
        .registerTypeHierarchyAdapter(GrammarObject::class.java, GrammarObjectAdapter())
        .create()

    @TypeConverter
    fun fromType(type: Type): String = type.name

    @TypeConverter
    fun toType(value: String): Type = Type.valueOf(value)

    @TypeConverter
    fun fromGrammarObjectList(value: List<GrammarObject>?): String? {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toGrammarObjectList(value: String?): List<GrammarObject>? {
        if (value == null) return null
        val listType = object : TypeToken<List<GrammarObject>>() {}.type
        return gson.fromJson(value, listType)
    }
}

class GrammarObjectAdapter : JsonSerializer<GrammarObject>, JsonDeserializer<GrammarObject> {
    override fun serialize(src: GrammarObject, typeOfSrc: java.lang.reflect.Type, context: JsonSerializationContext): JsonElement {
        val obj = JsonObject()
        when (src) {
            is FixedObject -> {
                obj.addProperty("type", "FixedObject")
                obj.addProperty("word", src.word)
            }
            is DynamicObject -> {
                obj.addProperty("type", "DynamicObject")
                obj.addProperty("broadType", src.broadType.name)
                if (src.form != null) {
                    obj.add("form", context.serialize(src.form))
                }
            }
        }
        return obj
    }

    override fun deserialize(json: JsonElement, typeOfT: java.lang.reflect.Type, context: JsonDeserializationContext): GrammarObject {
        val obj = json.asJsonObject
        val type = obj.get("type")?.asString ?: throw JsonParseException("Missing 'type' property")
        return when (type) {
            "FixedObject" -> {
                val word = obj.get("word")?.asString ?: throw JsonParseException("Missing 'word' property for FixedObject")
                FixedObject(word)
            }
            "DynamicObject" -> {
                val broadType = BroadType.valueOf(obj.get("broadType")?.asString ?: throw JsonParseException("Missing 'broadType' property for DynamicObject"))
                val form = if (obj.has("form")) context.deserialize<MasterRule>(obj.get("form"), MasterRule::class.java) else null
                DynamicObject(broadType, form)
            }
            else -> throw JsonParseException("Unknown type: $type")
        }
    }
}