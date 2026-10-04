package com.example.util

import com.example.data.model.Ayah
import java.util.Locale

object QuranUtils {

    /**
     * Compiled once for the process: this formatter runs inside `LazyColumn`
     * item composition, and allocating a fresh [Regex] per ayah was measurable
     * jank while scrolling long surahs.
     */
    private val WHITESPACE_REGEX = Regex("\\s+")

    /**
     * Cleans Ayah text for display by removing the first 4 words (Bismillah) from Ayah 1
     * using word-dropping for any Surah EXCEPT Surah Al-Fatiha (1) and Surah At-Tawbah (9).
     */
    fun formatAyahText(surahNumber: Int, ayahNumber: Int, text: String): String {
        if (surahNumber != 1 && surahNumber != 9 && ayahNumber == 1) {
            val cleanText = text.trimStart('\uFEFF', ' ', '\n', '\r', '\t').trim()
            val words = cleanText.split(WHITESPACE_REGEX)
            if (words.size > 4) {
                return words.drop(4).joinToString(" ").trim()
            }
        }
        return text.trim()
    }

    /**
     * Dynamically formats a base audio URL and Surah number (1 to 114) into a full MP3 stream URL.
     * Formats surahNumber as 3-digit zero-padded string (e.g. 001.mp3, 114.mp3).
     */
    fun buildSurahAudioUrl(baseAudioUrl: String, surahNumber: Int): String {
        val base = baseAudioUrl.trimEnd('/')
        return when {
            base.contains("islamic.network") -> "$base/$surahNumber.mp3"
            base.isNotBlank() -> String.format(Locale.US, "%s/%03d.mp3", base, surahNumber)
            else -> String.format(Locale.US, "https://server8.mp3quran.net/afs/%03d.mp3", surahNumber)
        }
    }

    /**
     * Constructs a clean, verified audio URL for a given Qari and Surah.
     * Handles base URL trimming, double-slash elimination, and 3-digit zero padding.
     */
    fun buildAudioUrl(qari: com.example.data.model.Qari, surahId: Int): String {
        return buildSurahAudioUrl(qari.baseAudioUrl, surahId)
    }

    /**
     * Checks if a specific Surah is recorded by the selected Qari.
     * Most Qaris have recorded the complete Quran (114 Surahs), while some reciters
     * have partial studio recordings.
     */
    fun isSurahRecorded(qari: com.example.data.model.Qari, surahId: Int): Boolean {
        return qari.isSurahRecorded(surahId)
    }

    /**
     * Secondary fallback audio stream URL if primary stream fails to buffer.
     * Retains the currently selected Qari's base URL to prevent switching to another reciter.
     */
    fun buildFallbackAudioUrl(qari: com.example.data.model.Qari, surahId: Int): String {
        return buildAudioUrl(qari, surahId)
    }

    /**
     * Robust time formatting converting seconds or milliseconds into HH:mm:ss or mm:ss.
     */
    fun formatAudioDuration(totalInput: Int): String {
        val totalSeconds = if (totalInput > 86400 * 2) totalInput / 1000 else totalInput
        if (totalSeconds <= 0) return "00:00"

        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60

        return if (hours > 0) {
            String.format(Locale.US, "%d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format(Locale.US, "%02d:%02d", minutes, seconds)
        }
    }
}
