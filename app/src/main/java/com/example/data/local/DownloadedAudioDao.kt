package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DownloadedAudioDao {
    @Query("SELECT * FROM downloaded_audio ORDER BY timestamp DESC")
    fun getAllDownloadedAudio(): Flow<List<DownloadedAudio>>

    @Query("SELECT * FROM downloaded_audio WHERE id = :id LIMIT 1")
    suspend fun getDownloadedAudioById(id: String): DownloadedAudio?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDownloadedAudio(audio: DownloadedAudio)

    @Query("DELETE FROM downloaded_audio WHERE id = :id")
    suspend fun deleteDownloadedAudio(id: String)

    @Query("SELECT COUNT(*) FROM downloaded_audio")
    fun getDownloadedCount(): Flow<Int>
}
