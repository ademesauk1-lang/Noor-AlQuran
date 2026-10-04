package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "downloaded_audio")
data class DownloadedAudio(
    @PrimaryKey val id: String, // format: "surahId_qariId"
    val surahId: Int,
    val qariId: String,
    val localFilePath: String,
    val timestamp: Long = System.currentTimeMillis(),
    val fileSizeBytes: Long = 0L
)
