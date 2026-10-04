package com.example.data.local

import androidx.room.TypeConverter
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString

class MapTypeConverter {
    @TypeConverter
    fun fromMap(value: Map<String, String>): String {
        return Json.encodeToString(value)
    }

    @TypeConverter
    fun toMap(value: String): Map<String, String> {
        if (value.isEmpty()) return emptyMap()
        return try {
            Json.decodeFromString(value)
        } catch (e: Exception) {
            emptyMap()
        }
    }
}
