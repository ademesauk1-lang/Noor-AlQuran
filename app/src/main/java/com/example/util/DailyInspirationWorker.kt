package com.example.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.MainActivity
import com.example.R
import java.util.Calendar
import java.util.concurrent.TimeUnit

class DailyInspirationWorker(
    private val appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        try {
            showNotification()
            return Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            return Result.retry()
        }
    }

    private fun showNotification() {
        val channelId = "daily_inspiration_channel"
        val notificationManager =
            appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Daily Quran & Zikr Inspiration",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Daily inspirational Ayahs, Hadiths, and Zikr reminders"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(appContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("OPEN_TAB", "INSPIRATION")
        }

        val pendingIntent = PendingIntent.getActivity(
            appContext,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val inspirations = listOf(
            Pair("✨ Daily Quran Verse", "فَإِنَّ مَعَ الْعُسْرِ يُسْرًا — Indeed, with hardship comes ease. [Surah Ash-Sharh 94:5]"),
            Pair("🤲 Daily Du'a & Zikr", "سُبْحَانَ اللَّهِ وَبِحَمْدِهِ ، سُبْحَانَ اللَّهِ الْعَظِيمِ — Glory be to Allah and His praise, Glory be to Allah the Greatest."),
            Pair("📖 Reflection of the Day", "وَقُل رَّبِّ زِدْنِي عِلْمًا — And say: 'My Lord, increase me in knowledge.' [Surah Taha 20:114]"),
            Pair("🌿 Peace & Faith", "أَلَا بِذِكْرِ اللَّهِ تَطْمَئِنُّ الْقُلُوبُ — Verily, in the remembrance of Allah do hearts find rest. [Surah Ar-Ra'd 13:28]"),
            Pair("💡 Daily Wisdom", "وَاسْتَعِينُوا بِالصَّبْرِ وَالصَّلَاةِ — And seek help through patience and prayer. [Surah Al-Baqarah 2:45]")
        )

        val inspiration = inspirations.random()

        val notification = NotificationCompat.Builder(appContext, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(inspiration.first)
            .setContentText(inspiration.second)
            .setStyle(NotificationCompat.BigTextStyle().bigText(inspiration.second))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    companion object {
        const val WORK_NAME = "DailyInspirationWork"
        const val NOTIFICATION_ID = 1001

        fun scheduleDailyNotification(context: Context) {
            val currentDate = Calendar.getInstance()
            val dueDate = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 8)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
            }

            if (dueDate.before(currentDate)) {
                dueDate.add(Calendar.HOUR_OF_DAY, 24)
            }

            val initialDelay = dueDate.timeInMillis - currentDate.timeInMillis

            val dailyWorkRequest = PeriodicWorkRequestBuilder<DailyInspirationWorker>(24, TimeUnit.HOURS)
                .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                dailyWorkRequest
            )
        }

        fun cancelDailyNotification(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }
    }
}
