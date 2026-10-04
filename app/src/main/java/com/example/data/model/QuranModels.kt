package com.example.data.model

data class Surah(
    val id: Int,
    val nameArabic: String,
    val nameEnglish: String,
    val englishTranslation: String,
    val revelationType: String, // "Meccan" or "Medinan"
    val versesCount: Int,
    val juzNumber: Int
)

data class Ayah(
    val surahNumber: Int,
    val ayahNumber: Int,
    val textArabic: String,
    val tafsir: String? = null,
    val translations: Map<String, String> = emptyMap()
)

data class JuzInfo(
    val juzNumber: Int,
    val nameArabic: String,
    val startSurahName: String,
    val startAyahNumber: Int,
    val endSurahName: String,
    val endAyahNumber: Int,
    val surahIdsIncluded: List<Int>
)

data class Qari(
    val id: String,
    val name: String,
    val country: String,
    val apiIdentifier: String,
    val photoUrl: String = "",
    val serverUrl: String = "",
    val imageResId: Int? = null,
    val recordedSurahs: List<Int>? = null
) {
    val baseAudioUrl: String
        get() = serverUrl

    fun isSurahRecorded(surahId: Int): Boolean {
        return recordedSurahs == null || surahId in recordedSurahs
    }
}

data class LastReadPosition(
    val surahId: Int,
    val surahNameEnglish: String,
    val surahNameArabic: String,
    val ayahNumber: Int,
    val timestampMs: Long
)

enum class AppTab(val label: String) {
    WELCOME("Welcome"),
    HOME("Home"),
    SURAHS("Surahs"),
    JUZ("Juz"),
    MUSHAF("Full Quran"),
    READER("Reader"),
    PLAYER("Player"),
    RECITERS("Reciters"),
    PROFILE("Profile"),
    PREMIUM("Premium"),
    ABOUT("About"),
    AUTH("Auth"),
    AUTH_CALLBACK("Auth Callback"),
    THEME_LAB("Theme Lab"),
    INSPIRATION("Inspiration"),
    PRIVACY_POLICY("Privacy Policy"),
    TERMS_OF_SERVICE("Terms of Service")
}

enum class SubscriptionPlan(val title: String, val price: String, val perMonthText: String) {
    MONTHLY("Monthly", "$4.99", "$4.99/month"),
    ANNUAL("Annual", "$29.99", "$2.49/month")
}

enum class TranslationLanguage(val id: String, val displayName: String) {
    ENGLISH("en", "English"),
    URDU("ur", "Urdu"),
    INDONESIAN("id", "Indonesian"),
    AMHARIC("am", "Amharic"),
    FRENCH("fr", "French"),
    SPANISH("es", "Spanish"),
    TURKISH("tr", "Turkish"),
    BENGALI("bn", "Bengali"),
    HINDI("hi", "Hindi"),
    RUSSIAN("ru", "Russian"),
    GERMAN("de", "German")
}

enum class DarkModeOption(val displayName: String) {
    SYSTEM("System Default"),
    LIGHT("Light Mode"),
    DARK("Dark Mode")
}

enum class BackgroundSound(val displayName: String, val audioUrl: String?) {
    NONE("None", null),
    RAIN("Rain", "https://cdn.pixabay.com/download/audio/2021/08/04/audio_3d1a550978.mp3"),
    NATURE("Nature", "https://cdn.pixabay.com/download/audio/2022/01/18/audio_d0a13f69d2.mp3"),
    BROWN_NOISE("Brown Noise", "https://cdn.pixabay.com/download/audio/2022/10/24/audio_dc7ab2d634.mp3"),
    FIREPLACE("Fireplace", "https://cdn.pixabay.com/download/audio/2022/03/15/audio_2491a62779.mp3"),
    OCEAN("Ocean", "https://cdn.pixabay.com/download/audio/2022/02/10/audio_fc862140bb.mp3")
}
