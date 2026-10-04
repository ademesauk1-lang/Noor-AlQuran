package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ayahs")
data class AyahEntity(
    @PrimaryKey val id: String, // format: "surahNumber_ayahNumber"
    val surahNumber: Int,
    val ayahNumber: Int,
    val textArabic: String,
    val tafsir: String?,
    val translations: Map<String, String>
)
