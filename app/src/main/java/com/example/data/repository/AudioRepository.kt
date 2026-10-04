package com.example.data.repository

import android.content.Context
import com.example.data.local.AppDatabase
import com.example.data.local.DownloadedAudio
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

class AudioRepository(private val context: Context) {
    private val dao = AppDatabase.getDatabase(context).downloadedAudioDao()

    val allDownloadedAudio: Flow<List<DownloadedAudio>> = dao.getAllDownloadedAudio()

    suspend fun getDownloadedAudio(surahId: Int, qariId: String): DownloadedAudio? {
        val id = "${surahId}_${qariId}"
        val record = dao.getDownloadedAudioById(id) ?: return null
        val file = File(record.localFilePath)
        if (file.exists() && file.length() > 0) {
            return record
        } else {
            dao.deleteDownloadedAudio(id)
            return null
        }
    }

    fun triggerBackgroundCache(surahId: Int, qariId: String, audioUrl: String) {
        com.example.util.AudioCacheWorker.enqueueAudioCache(context, surahId, qariId, audioUrl)
    }

    suspend fun downloadAudio(surahId: Int, qariId: String, audioUrl: String, onProgress: (Int) -> Unit = {}): Boolean = withContext(Dispatchers.IO) {
        try {
            val id = "${surahId}_${qariId}"
            val fileName = "audio_${id}.mp3"
            val file = File(context.filesDir, fileName)

            val url = URL(audioUrl)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 15000
            connection.readTimeout = 15000
            connection.connect()

            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                return@withContext false
            }

            val fileLength = connection.contentLength
            val input = connection.inputStream
            val output = FileOutputStream(file)

            val data = ByteArray(8192)
            var total: Long = 0
            var count: Int
            // Progress is reported at most once per percentage point: emitting one
            // update per 8 KB chunk would push thousands of state updates into the
            // UI and cause visible stutter while downloading.
            var lastReportedProgress = -1
            while (input.read(data).also { count = it } != -1) {
                total += count.toLong()
                if (fileLength > 0) {
                    val percent = (total * 100 / fileLength).toInt()
                    if (percent != lastReportedProgress) {
                        lastReportedProgress = percent
                        onProgress(percent)
                    }
                }
                output.write(data, 0, count)
            }
            output.flush()
            output.close()
            input.close()

            val downloadedAudio = DownloadedAudio(
                id = id,
                surahId = surahId,
                qariId = qariId,
                localFilePath = file.absolutePath,
                timestamp = System.currentTimeMillis(),
                fileSizeBytes = file.length()
            )
            dao.insertDownloadedAudio(downloadedAudio)
            return@withContext true
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext false
        }
    }
    
    suspend fun removeDownloadedAudio(surahId: Int, qariId: String) = withContext(Dispatchers.IO) {
        val id = "${surahId}_${qariId}"
        val audio = dao.getDownloadedAudioById(id)
        if (audio != null) {
            val file = File(audio.localFilePath)
            if (file.exists()) {
                file.delete()
            }
            dao.deleteDownloadedAudio(id)
        }
    }
}
