package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface AyahDao {
    @Query("SELECT * FROM ayahs WHERE surahNumber = :surahNumber ORDER BY ayahNumber ASC")
    suspend fun getAyahsForSurah(surahNumber: Int): List<AyahEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAyahs(ayahs: List<AyahEntity>)
    
    @Query("SELECT COUNT(*) FROM ayahs WHERE surahNumber = :surahNumber")
    suspend fun getAyahCountForSurah(surahNumber: Int): Int
}
