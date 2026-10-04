package com.example.util

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.data.local.AppDatabase
import com.example.data.local.DownloadedAudio
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

class AudioCacheWorker(
    private val appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val surahId = inputData.getInt(KEY_SURAH_ID, -1)
        val qariId = inputData.getString(KEY_QARI_ID) ?: return@withContext Result.failure()
        val audioUrl = inputData.getString(KEY_AUDIO_URL) ?: return@withContext Result.failure()

        if (surahId == -1) return@withContext Result.failure()

        val id = "${surahId}_${qariId}"
        val fileName = "audio_${id}.mp3"
        val file = File(appContext.filesDir, fileName)

        // Check if already cached in Room & exists on disk
        val dao = AppDatabase.getDatabase(appContext).downloadedAudioDao()
        val existing = dao.getDownloadedAudioById(id)
        if (existing != null && File(existing.localFilePath).exists() && File(existing.localFilePath).length() > 0) {
            return@withContext Result.success()
        }

        try {
            val url = URL(audioUrl)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 15000
            connection.readTimeout = 15000
            connection.connect()

            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                return@withContext Result.retry()
            }

            val input = connection.inputStream
            val tempFile = File(appContext.filesDir, "temp_${fileName}")
            val output = FileOutputStream(tempFile)

            val data = ByteArray(8192)
            var count: Int
            while (input.read(data).also { count = it } != -1) {
                output.write(data, 0, count)
            }
            output.flush()
            output.close()
            input.close()

            if (tempFile.exists() && tempFile.length() > 0) {
                if (file.exists()) file.delete()
                tempFile.renameTo(file)

                val downloadedAudio = DownloadedAudio(
                    id = id,
                    surahId = surahId,
                    qariId = qariId,
                    localFilePath = file.absolutePath,
                    timestamp = System.currentTimeMillis(),
                    fileSizeBytes = file.length()
                )
                dao.insertDownloadedAudio(downloadedAudio)
                return@withContext Result.success()
            } else {
                return@withContext Result.failure()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext Result.retry()
        }
    }

    companion object {
        const val KEY_SURAH_ID = "KEY_SURAH_ID"
        const val KEY_QARI_ID = "KEY_QARI_ID"
        const val KEY_AUDIO_URL = "KEY_AUDIO_URL"

        fun enqueueAudioCache(context: Context, surahId: Int, qariId: String, audioUrl: String) {
            val inputData = Data.Builder()
                .putInt(KEY_SURAH_ID, surahId)
                .putString(KEY_QARI_ID, qariId)
                .putString(KEY_AUDIO_URL, audioUrl)
                .build()

            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val workRequest = OneTimeWorkRequestBuilder<AudioCacheWorker>()
                .setInputData(inputData)
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueue(workRequest)
        }
    }
}
