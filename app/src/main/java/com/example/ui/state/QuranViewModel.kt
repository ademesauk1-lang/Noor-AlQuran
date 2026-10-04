package com.example.ui.state

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.media.MediaPlayer
import androidx.lifecycle.AndroidViewModel

import io.github.jan.supabase.auth.status.SessionStatus
import com.example.data.api.SupabaseClient
import com.example.data.model.UserProfile
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.auth.auth

import androidx.lifecycle.viewModelScope
import com.example.audio.NoorAudioEngine
import com.example.audio.PlaybackService
import com.example.data.model.AppTab
import com.example.data.model.Ayah
import com.example.data.model.DarkModeOption
import com.example.data.model.LastReadPosition
import com.example.data.model.Qari
import com.example.data.model.SubscriptionPlan
import com.example.data.model.Surah
import com.example.data.model.TranslationLanguage
import com.example.data.preferences.AppPreferencesRepository
import com.example.data.repository.QuranData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class RepeatMode(val label: String, val playerRepeatMode: Int = 0, val shuffleModeEnabled: Boolean = false) {
    REPEAT_OFF("Repeat Off", 0, false),
    REPEAT_ONE("Repeat One", 1, false),
    REPEAT_ALL("Repeat All", 2, false),
    SHUFFLE("Shuffle", 0, true)
}

/**
 * High-frequency playback position, intentionally kept OUT of [QuranUiState].
 *
 * The progress timer ticks twice per second; publishing those ticks on the main
 * UI state would invalidate every composable that observes it (the whole screen
 * tree) twice a second — the main cause of jank while scrolling. Screen-level UI
 * only collects this flow inside a small, dedicated composable.
 */
data class PlaybackProgress(
    val currentSeconds: Int = 0,
    val durationSeconds: Int = 0,
    val progress: Float = 0f
)

data class QuranUiState(
    val currentTab: AppTab = AppTab.HOME,
    val isDataLoading: Boolean = true,
    /** `true` once the first-run onboarding flow has been completed or skipped. */
    val hasCompletedOnboarding: Boolean = false,
    val selectedSurahId: Int = 1,
    val selectedAyahNumber: Int = 1,
    val searchQuery: String = "",
    val reciterSearchQuery: String = "",
    val filterType: String = "ALL", // "ALL", "MECCAN", "MEDINAN"
    val bookmarkedAyahs: Set<String> = emptySet(), // "surahId:ayahNumber"
    val lastRead: LastReadPosition = LastReadPosition(1, "Al-Fatiha", "الفاتحة", 1, System.currentTimeMillis()),
    val translationLanguage: TranslationLanguage = TranslationLanguage.ENGLISH,
    val arabicFontSizeSp: Float = 28f,
    val darkModeOption: DarkModeOption = DarkModeOption.SYSTEM,
    val notificationsEnabled: Boolean = true,
    val dailyAyahReminders: Boolean = true,
    val audioDownloadAlerts: Boolean = true,
    val isPremiumUser: Boolean = false,
    val selectedSubscriptionPlan: SubscriptionPlan = SubscriptionPlan.ANNUAL,
    val aiExplanation: String? = null,
    val isAiLoading: Boolean = false,
    val showAiExplanation: Boolean = false,
    
    // Audio Player state
    val isPlaying: Boolean = false,
    val audioSurahId: Int = 1,
    val audioAyahNumber: Int = 1,
    val selectedQari: Qari = QuranData.qariList[0],
    val audioProgress: Float = 0f,
    val audioDurationSeconds: Int = 30,
    val audioCurrentSeconds: Int = 0,
    val playbackSpeed: Float = 1.0f,
    val isLoopingSurah: Boolean = false,
    val repeatMode: RepeatMode = RepeatMode.REPEAT_OFF,
    val backgroundSound: com.example.data.model.BackgroundSound = com.example.data.model.BackgroundSound.NONE,
    val showAudioMiniPlayer: Boolean = false,
    val downloadedAudios: Set<String> = emptySet(),
    val isAudioDownloading: Boolean = false,
    val audioDownloadProgress: Int = 0,
    val isOnline: Boolean = true,
    val offlineNotice: String? = null,
    val userMessage: String? = null,
    val isUserSignedIn: Boolean = false,
    val currentUserEmail: String? = null,
    val isSyncing: Boolean = false,
    val syncStatusMessage: String? = null,
    val lastSyncTimeFormatted: String? = null
)

class QuranViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs: SharedPreferences = application.getSharedPreferences("quran_prefs", Context.MODE_PRIVATE)

    /** First-run / onboarding state (DataStore, with legacy SharedPreferences migration). */
    private val appPreferences = AppPreferencesRepository(application)

    private val _uiState = MutableStateFlow(QuranUiState())
    val uiState: StateFlow<QuranUiState> = _uiState.asStateFlow()

    /** Playback position updates — deliberately decoupled from [uiState]. */
    private val _playbackProgress = MutableStateFlow(PlaybackProgress())
    val playbackProgress: StateFlow<PlaybackProgress> = _playbackProgress.asStateFlow()

    /** Single ExoPlayer engine shared with [PlaybackService] (background playback). */
    private val audioEngine: NoorAudioEngine by lazy { NoorAudioEngine.get(application) }

    private var bgMediaPlayer: MediaPlayer? = null
    private val audioRepository = com.example.data.repository.AudioRepository(application)
    private val networkMonitor = com.example.util.NetworkMonitor(application)
    private var progressJob: Job? = null
    private val syncRepository = com.example.data.repository.SupabaseSyncRepository()

    init {
        viewModelScope.launch {
            _uiState.update { it.copy(isOnline = networkMonitor.isCurrentlyOnline()) }
            networkMonitor.isOnline.collect { online ->
                _uiState.update { it.copy(isOnline = online) }
            }
        }

        viewModelScope.launch {
            audioRepository.allDownloadedAudio.collect { list ->
                val set = list.map { it.id }.toSet()
                _uiState.update { it.copy(downloadedAudios = set) }
            }
        }
        viewModelScope.launch {
            SupabaseClient.client.auth.sessionStatus.collect { status ->
                when (status) {
                    is SessionStatus.Authenticated -> {
                        val email = status.session.user?.email
                        val userId = status.session.user?.id
                        _uiState.update { it.copy(isUserSignedIn = true, currentUserEmail = email) }
                        if (userId != null) {
                            try {
                                val profile = SupabaseClient.client.postgrest["profiles"]
                                    .select { filter { eq("id", userId) } }
                                    .decodeSingleOrNull<UserProfile>()
                                    
                                if (profile != null) {
                                    // Sync reading progress
                                    if (profile.last_read_surah != null && profile.last_read_ayah != null) {
                                        val newLastRead = _uiState.value.lastRead.copy(
                                            surahId = profile.last_read_surah,
                                            ayahNumber = profile.last_read_ayah
                                        )
                                        _uiState.update { it.copy(lastRead = newLastRead) }
                                    }
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                    is SessionStatus.NotAuthenticated -> {
                        _uiState.update { it.copy(isUserSignedIn = false, currentUserEmail = null) }
                    }
                    else -> {}
                }
            }
        }

        loadPreferencesFromDisk()
    }

    /**
     * Loads SharedPreferences **and** the (potentially large) Quran/Juz/Qari data
     * structures on [Dispatchers.IO]. Nothing here may run on the main thread:
     * touching a SharedPreferences file and building the static Quran lists both
     * perform disk reads.
     */
    private fun loadPreferencesFromDisk() {
        viewModelScope.launch(Dispatchers.IO) {
            val hasCompletedOnboarding = runCatching { appPreferences.isOnboardingCompleted() }
                .getOrDefault(false)

            val snapshot = readPreferencesSnapshot()
            QuranData.initialize(getApplication())

            _uiState.update { state ->
                state.copy(
                    // First launch → onboarding; every subsequent launch → home.
                    currentTab = if (hasCompletedOnboarding) state.currentTab else AppTab.WELCOME,
                    hasCompletedOnboarding = hasCompletedOnboarding,
                    isDataLoading = false,
                    bookmarkedAyahs = snapshot.bookmarks,
                    selectedSurahId = snapshot.lastSurahId,
                    selectedAyahNumber = snapshot.lastAyahNum,
                    arabicFontSizeSp = snapshot.fontSize,
                    translationLanguage = snapshot.language,
                    darkModeOption = snapshot.darkMode,
                    selectedQari = snapshot.qari,
                    notificationsEnabled = snapshot.notificationsEnabled,
                    dailyAyahReminders = snapshot.dailyReminders,
                    audioDownloadAlerts = snapshot.audioAlerts,
                    isPremiumUser = snapshot.isPremium,
                    backgroundSound = snapshot.backgroundSound,
                    lastRead = LastReadPosition(
                        snapshot.surah.id,
                        snapshot.surah.nameEnglish,
                        snapshot.surah.nameArabic,
                        snapshot.lastAyahNum,
                        System.currentTimeMillis()
                    )
                )
            }
        }
    }

    private data class PreferencesSnapshot(
        val bookmarks: Set<String>,
        val lastSurahId: Int,
        val lastAyahNum: Int,
        val fontSize: Float,
        val language: TranslationLanguage,
        val darkMode: DarkModeOption,
        val qari: Qari,
        val notificationsEnabled: Boolean,
        val dailyReminders: Boolean,
        val audioAlerts: Boolean,
        val isPremium: Boolean,
        val backgroundSound: com.example.data.model.BackgroundSound,
        val surah: Surah
    )

    private fun readPreferencesSnapshot(): PreferencesSnapshot {
        val savedBookmarks = prefs.getStringSet("bookmarks", emptySet()) ?: emptySet()
        val lastSurahId = prefs.getInt("last_surah_id", 1)
        val lastAyahNum = prefs.getInt("last_ayah_num", 1)
        val fontSize = prefs.getFloat("font_size", 28f)
        val langOrdinal = prefs.getInt("translation_lang", 0)
        val darkOrdinal = prefs.getInt("dark_mode_option", 0)
        val qariId = prefs.getString("selected_qari_id", "mishary") ?: "mishary"
        val notifEnabled = prefs.getBoolean("notif_enabled", true)
        val dailyReminders = prefs.getBoolean("daily_reminders", true)
        val audioAlerts = prefs.getBoolean("audio_alerts", true)
        val isPremium = prefs.getBoolean("is_premium_user", false)
        val bgSoundName = prefs.getString("background_sound", "NONE") ?: "NONE"
        val bgSound = runCatching { com.example.data.model.BackgroundSound.valueOf(bgSoundName) }
            .getOrDefault(com.example.data.model.BackgroundSound.NONE)

        val surah = QuranData.surahById(lastSurahId) ?: QuranData.surahs[0]
        val lang = TranslationLanguage.entries.getOrElse(langOrdinal) { TranslationLanguage.ENGLISH }
        val darkMode = DarkModeOption.entries.getOrElse(darkOrdinal) { DarkModeOption.SYSTEM }
        val savedQari = QuranData.qariList.find { it.id == qariId } ?: QuranData.qariList[0]

        return PreferencesSnapshot(
            bookmarks = savedBookmarks,
            lastSurahId = lastSurahId,
            lastAyahNum = lastAyahNum,
            fontSize = fontSize,
            language = lang,
            darkMode = darkMode,
            qari = savedQari,
            notificationsEnabled = notifEnabled,
            dailyReminders = dailyReminders,
            audioAlerts = audioAlerts,
            isPremium = isPremium,
            backgroundSound = bgSound,
            surah = surah
        )
    }

    fun selectSubscriptionPlan(plan: SubscriptionPlan) {
        _uiState.update { it.copy(selectedSubscriptionPlan = plan) }
    }

    fun unlockPremium() {
        prefs.edit().putBoolean("is_premium_user", true).apply()
        _uiState.update { it.copy(isPremiumUser = true) }
    }


        private val tabStack = mutableListOf<AppTab>()

    fun navigateToPremium() {
        tabStack.add(_uiState.value.currentTab)
        _uiState.update { it.copy(currentTab = AppTab.PREMIUM) }
    }
    
    fun navigateToAbout() {
        tabStack.add(_uiState.value.currentTab)
        _uiState.update { it.copy(currentTab = AppTab.ABOUT) }
    }

    fun navigateToAuth() {
        tabStack.add(_uiState.value.currentTab)
        _uiState.update { it.copy(currentTab = AppTab.AUTH) }
    }

    fun navigateToPrivacyPolicy() {
        tabStack.add(_uiState.value.currentTab)
        _uiState.update { it.copy(currentTab = AppTab.PRIVACY_POLICY) }
    }

    fun navigateToTermsOfService() {
        tabStack.add(_uiState.value.currentTab)
        _uiState.update { it.copy(currentTab = AppTab.TERMS_OF_SERVICE) }
    }

    /**
     * Called when the user finishes (or skips) onboarding.
     *
     * The flag is persisted through DataStore, so the intro is shown exactly once
     * — on the very first launch of the app. The legacy SharedPreferences key is
     * kept in sync for older builds/restores.
     */
    fun completeWelcome() {
        prefs.edit().putBoolean(AppPreferencesRepository.LEGACY_ONBOARDING_KEY, true).apply()
        _uiState.update {
            it.copy(hasCompletedOnboarding = true, currentTab = AppTab.HOME)
        }
        viewModelScope.launch(Dispatchers.IO) {
            runCatching { appPreferences.setOnboardingCompleted(true) }
                .onFailure { it.printStackTrace() }
        }
    }

    fun popBackStack() {
        if (tabStack.isNotEmpty()) {
            val previous = tabStack.removeAt(tabStack.lastIndex)
            _uiState.update { it.copy(currentTab = previous) }
        } else {
            _uiState.update { it.copy(currentTab = AppTab.HOME) }
        }
    }

    fun selectTab(tab: AppTab) {
        if (_uiState.value.currentTab != tab) {
            if (_uiState.value.currentTab != AppTab.WELCOME) {
                if (tabStack.isEmpty() || tabStack.last() != _uiState.value.currentTab) {
                    tabStack.add(_uiState.value.currentTab)
                }
            }
            _uiState.update { it.copy(currentTab = tab) }
        }
    }

    fun openReaderForSurah(surahId: Int, ayahNumber: Int = 1) {
        val surah = QuranData.surahById(surahId) ?: return
        val newLastRead = LastReadPosition(
            surahId = surah.id,
            surahNameEnglish = surah.nameEnglish,
            surahNameArabic = surah.nameArabic,
            ayahNumber = ayahNumber,
            timestampMs = System.currentTimeMillis()
        )
        
        prefs.edit()
            .putInt("last_surah_id", surahId)
            .putInt("last_ayah_num", ayahNumber)
            .apply()

        _uiState.update {
            it.copy(
                selectedSurahId = surahId,
                audioSurahId = if (!it.isPlaying) surahId else it.audioSurahId,
                selectedAyahNumber = ayahNumber,
                lastRead = newLastRead,
                currentTab = AppTab.READER
            )
        }
    }

    fun updateLastReadAyah(ayahNumber: Int) {
        val current = _uiState.value.lastRead
        if (current.ayahNumber == ayahNumber) return
        val newLastRead = current.copy(ayahNumber = ayahNumber, timestampMs = System.currentTimeMillis())
        prefs.edit().putInt("last_ayah_num", ayahNumber).apply()
        _uiState.update { it.copy(lastRead = newLastRead) }
        
        // Sync to Supabase
        viewModelScope.launch {
            val session = SupabaseClient.client.auth.currentSessionOrNull()
            val userId = session?.user?.id ?: return@launch
            try {
                val profile = UserProfile(id = userId, last_read_surah = newLastRead.surahId, last_read_ayah = newLastRead.ayahNumber)
                SupabaseClient.client.postgrest["profiles"].upsert(profile)
            } catch(e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun setFilterType(type: String) {
        _uiState.update { it.copy(filterType = type) }
    }

    fun toggleBookmark(surahId: Int, ayahNumber: Int) {
        val key = "$surahId:$ayahNumber"
        val current = _uiState.value.bookmarkedAyahs.toMutableSet()
        if (current.contains(key)) {
            current.remove(key)
        } else {
            current.add(key)
        }
        
        prefs.edit().putStringSet("bookmarks", current).apply()
        _uiState.update { it.copy(bookmarkedAyahs = current) }
    }

    fun isBookmarked(surahId: Int, ayahNumber: Int): Boolean {
        return _uiState.value.bookmarkedAyahs.contains("$surahId:$ayahNumber")
    }

    fun updateFontSize(newSizeSp: Float) {
        prefs.edit().putFloat("font_size", newSizeSp).apply()
        _uiState.update { it.copy(arabicFontSizeSp = newSizeSp) }
    }

    fun setTranslationLanguage(lang: TranslationLanguage) {
        prefs.edit().putInt("translation_lang", lang.ordinal).apply()
        _uiState.update { it.copy(translationLanguage = lang) }
    }

    fun clearUserMessage() {
        _uiState.update { it.copy(userMessage = null) }
    }

    /**
     * The engine buffered enough data to render audio (ExoPlayer `STATE_READY`).
     * Mirrors the old `MediaPlayer.onPrepared` behaviour, including the sanity
     * check for servers that answer with an HTML error page instead of audio.
     */
    private fun handleEngineReady(
        surahId: Int,
        ayahNumber: Int,
        qari: Qari,
        durationMs: Long,
        isFallbackAttempted: Boolean
    ) {
        if (durationMs in 1..5000L) {
            android.util.Log.w("QuranViewModel", "Invalid audio duration detected ($durationMs ms). Server returned HTML 404 page.")
            audioEngine.stop()
            if (!isFallbackAttempted) {
                val fallbackUrl = com.example.util.QuranUtils.buildFallbackAudioUrl(qari, surahId)
                _uiState.update {
                    it.copy(isPlaying = false, userMessage = "Retrying audio for Sheikh ${qari.name}...")
                }
                playAudioSource(surahId, ayahNumber, fallbackUrl, isFallbackAttempted = true)
            } else {
                _uiState.update {
                    it.copy(
                        isPlaying = false,
                        userMessage = "Audio stream for Sheikh ${qari.name} is currently unavailable. Please select another reciter."
                    )
                }
            }
            return
        }

        val durationSeconds = if (durationMs > 0L) {
            (durationMs / 1000L).toInt().coerceAtLeast(1)
        } else {
            180
        }

        _uiState.update {
            it.copy(
                audioSurahId = surahId,
                audioAyahNumber = ayahNumber,
                audioDurationSeconds = durationSeconds,
                audioCurrentSeconds = 0,
                audioProgress = 0f,
                isPlaying = true
            )
        }
        _playbackProgress.value = PlaybackProgress(
            currentSeconds = 0,
            durationSeconds = durationSeconds,
            progress = 0f
        )

        // Ambient background sound (optional, uses its own lightweight player).
        val currentBg = _uiState.value.backgroundSound
        if (currentBg.audioUrl != null && bgMediaPlayer == null && _uiState.value.isOnline) {
            try {
                bgMediaPlayer = MediaPlayer().apply {
                    setDataSource(currentBg.audioUrl)
                    isLooping = true
                    setVolume(0.3f, 0.3f)
                    setOnPreparedListener { bgMp -> bgMp.start() }
                    prepareAsync()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else if (bgMediaPlayer?.isPlaying == false) {
            bgMediaPlayer?.start()
        }

        startProgressTimer()
    }

    /** Playback failed — retries once against the backup mirror before surfacing an error. */
    private fun handleEngineError(
        surahId: Int,
        ayahNumber: Int,
        qari: Qari,
        isFallbackAttempted: Boolean
    ) {
        progressJob?.cancel()
        audioEngine.stop()
        if (!isFallbackAttempted) {
            android.util.Log.d("QuranViewModel", "Primary server failed. Trying backup audio stream for Sheikh ${qari.name}...")
            val fallbackUrl = com.example.util.QuranUtils.buildFallbackAudioUrl(qari, surahId)
            _uiState.update {
                it.copy(isPlaying = false, userMessage = "Retrying audio stream for Sheikh ${qari.name}...")
            }
            playAudioSource(surahId, ayahNumber, fallbackUrl, isFallbackAttempted = true)
        } else {
            _uiState.update {
                it.copy(
                    isPlaying = false,
                    userMessage = "Audio stream for Sheikh ${qari.name} is currently unavailable. Please select another reciter or check connection."
                )
            }
        }
    }

    /** The current surah finished — advances according to the selected repeat mode. */
    private fun handleEngineCompletion(surahId: Int) {
        progressJob?.cancel()
        _uiState.update { it.copy(isPlaying = false) }
        if (_uiState.value.audioDurationSeconds in 1..5) {
            return
        }
        when (_uiState.value.repeatMode) {
            RepeatMode.REPEAT_ONE -> playAudio(surahId, 1)
            RepeatMode.SHUFFLE -> playAudio((1..114).random(), 1)
            RepeatMode.REPEAT_ALL -> nextAudioTrack()
            RepeatMode.REPEAT_OFF -> {
                if (surahId < 114) {
                    nextAudioTrack()
                } else {
                    _uiState.update { it.copy(isPlaying = false, audioProgress = 0f) }
                    _playbackProgress.value = PlaybackProgress()
                }
            }
        }
    }

    fun playAudio(surahId: Int, ayahNumber: Int = 1) {
        val selectedQari = _uiState.value.selectedQari
        if (!selectedQari.isSurahRecorded(surahId)) {
            val surah = QuranData.surahById(surahId) ?: QuranData.surahs[0]
            _uiState.update {
                it.copy(
                    isPlaying = false,
                    showAudioMiniPlayer = true,
                    audioSurahId = surahId,
                    userMessage = "Surah ${surah.nameEnglish} has not been recorded by Sheikh ${selectedQari.name}."
                )
            }
            return
        }
        val baseUrl = selectedQari.baseAudioUrl.trimEnd('/')
        val remoteUrl = "$baseUrl/${String.format(java.util.Locale.US, "%03d.mp3", surahId)}"
        playAudioSource(surahId, ayahNumber, remoteUrl, isFallbackAttempted = false)
    }

    private fun playAudioSource(surahId: Int, ayahNumber: Int, remoteUrl: String, isFallbackAttempted: Boolean) {
        val qari = _uiState.value.selectedQari

        viewModelScope.launch {
            val downloaded = audioRepository.getDownloadedAudio(surahId, qari.id)
            val audioSource: String?
            var isOfflineCached = false

            if (downloaded != null && java.io.File(downloaded.localFilePath).exists()) {
                audioSource = downloaded.localFilePath
                isOfflineCached = true
            } else if (_uiState.value.isOnline) {
                audioSource = remoteUrl
                // Automatically cache via WorkManager / Room for offline playback next time
                audioRepository.triggerBackgroundCache(surahId, qari.id, remoteUrl)
            } else {
                audioSource = null
            }

            if (audioSource == null) {
                val notice = "Audio for Surah $surahId is not downloaded for offline listening. Connect to internet to stream or download."
                _uiState.update {
                    it.copy(
                        isPlaying = false,
                        showAudioMiniPlayer = true,
                        audioSurahId = surahId,
                        audioAyahNumber = ayahNumber,
                        offlineNotice = notice,
                        userMessage = "Failed to load audio. Please check your internet connection."
                    )
                }
                return@launch
            }

            val cacheNotice = if (isOfflineCached) "Playing Surah $surahId from offline cache ⚡" else null

            progressJob?.cancel()
            audioEngine.stop()

            _uiState.update {
                it.copy(
                    audioSurahId = surahId,
                    audioAyahNumber = ayahNumber,
                    isPlaying = false,
                    showAudioMiniPlayer = true,
                    audioProgress = 0f,
                    audioCurrentSeconds = 0,
                    offlineNotice = cacheNotice
                )
            }
            _playbackProgress.value = PlaybackProgress()

            // Hand the source to the shared ExoPlayer engine. Callbacks are
            // re-assigned for every play request so a stale stream can never
            // interleave with the current one.
            val engine = audioEngine
            engine.setLooping(_uiState.value.repeatMode == RepeatMode.REPEAT_ONE)
            engine.setPlaybackSpeed(_uiState.value.playbackSpeed)
            engine.onReady = { durationMs ->
                handleEngineReady(surahId, ayahNumber, qari, durationMs, isFallbackAttempted)
            }
            engine.onError = { message ->
                android.util.Log.e("QuranViewModel", "Playback failed for $audioSource: $message")
                handleEngineError(surahId, ayahNumber, qari, isFallbackAttempted)
            }
            engine.onCompletion = {
                handleEngineCompletion(surahId)
            }
            engine.onIsPlayingChanged = { playing ->
                // Keeps the UI in sync with changes that originate outside the
                // app (notification, lock screen, headset buttons).
                if (_uiState.value.isPlaying != playing) {
                    _uiState.update { it.copy(isPlaying = playing) }
                }
                if (playing) startProgressTimer() else progressJob?.cancel()
            }

            android.util.Log.d("AudioPlayer", "Preparing audio source: $audioSource")
            val prepared = engine.prepare(audioSource, autoPlay = true)
            if (prepared) {
                // Promote the media session to a foreground service so playback
                // survives backgrounding and screen-off.
                PlaybackService.start(getApplication())
            } else {
                handleEngineError(surahId, ayahNumber, qari, isFallbackAttempted)
            }
        }
    }

    fun togglePlayPause() {
        val engine = audioEngine
        if (!engine.hasMediaItem) {
            // Nothing loaded yet (fresh launch) — start the last selected surah.
            playAudio(_uiState.value.audioSurahId, _uiState.value.audioAyahNumber)
            return
        }
        if (engine.isPlaying) {
            engine.pause()
            _uiState.update { it.copy(isPlaying = false) }
            progressJob?.cancel()
        } else {
            engine.play()
            engine.setPlaybackSpeed(_uiState.value.playbackSpeed)
            _uiState.update { it.copy(isPlaying = true) }
            startProgressTimer()
        }
    }

    fun nextAudioTrack() {
        val currentSurahId = _uiState.value.audioSurahId
        val nextSurahId = if (_uiState.value.repeatMode == RepeatMode.SHUFFLE) {
            (1..114).random()
        } else {
            if (currentSurahId < 114) currentSurahId + 1 else 1
        }
        playAudio(nextSurahId, 1)
    }

    fun previousAudioTrack() {
        val currentSurahId = _uiState.value.audioSurahId
        val prevSurahId = if (_uiState.value.repeatMode == RepeatMode.SHUFFLE) {
            (1..114).random()
        } else {
            if (currentSurahId > 1) currentSurahId - 1 else 114
        }
        playAudio(prevSurahId, 1)
    }

    fun selectQari(qari: Qari) {
        prefs.edit().putString("selected_qari_id", qari.id).apply()
        val currentSurahId = _uiState.value.audioSurahId
        _uiState.update { it.copy(selectedQari = qari) }
        playAudio(currentSurahId, 1)
    }

    fun updateReciterSearchQuery(query: String) {
        _uiState.update { it.copy(reciterSearchQuery = query) }
    }

    fun setDarkModeOption(option: DarkModeOption) {
        prefs.edit().putInt("dark_mode_option", option.ordinal).apply()
        _uiState.update { it.copy(darkModeOption = option) }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("notif_enabled", enabled).apply()
        _uiState.update { it.copy(notificationsEnabled = enabled) }
    }

    fun setDailyAyahReminders(enabled: Boolean) {
        prefs.edit().putBoolean("daily_reminders", enabled).apply()
        _uiState.update { it.copy(dailyAyahReminders = enabled) }
    }

    fun setAudioDownloadAlerts(enabled: Boolean) {
        prefs.edit().putBoolean("audio_alerts", enabled).apply()
        _uiState.update { it.copy(audioDownloadAlerts = enabled) }
    }

    fun togglePlaybackSpeed() {
        val nextSpeed = when (_uiState.value.playbackSpeed) {
            1.0f -> 1.25f
            1.25f -> 1.5f
            1.5f -> 2.0f
            2.0f -> 0.75f
            0.75f -> 1.0f
            else -> 1.0f
        }
        _uiState.update { it.copy(playbackSpeed = nextSpeed) }
        applyPlaybackSpeed(nextSpeed)
    }

    private fun applyPlaybackSpeed(speed: Float) {
        audioEngine.setPlaybackSpeed(speed)
    }

    fun seekAudio(progress: Float) {
        val clamped = progress.coerceIn(0f, 1f)
        val totalSecs = _uiState.value.audioDurationSeconds
        val currentSecs = (clamped * totalSecs).toInt()

        val durationMs = audioEngine.durationMs
        if (durationMs > 0L) {
            audioEngine.seekTo((clamped * durationMs).toLong())
        }

        _uiState.update { it.copy(audioProgress = clamped, audioCurrentSeconds = currentSecs) }
        _playbackProgress.update { current ->
            current.copy(
                currentSeconds = currentSecs,
                durationSeconds = if (current.durationSeconds > 0) current.durationSeconds else totalSecs,
                progress = clamped
            )
        }
    }


    fun setBackgroundSound(sound: com.example.data.model.BackgroundSound) {
        _uiState.update { it.copy(backgroundSound = sound) }
        prefs.edit().putString("background_sound", sound.name).apply()
        
        bgMediaPlayer?.release()
        bgMediaPlayer = null
        
        if (sound.audioUrl != null && _uiState.value.isPlaying) {
            bgMediaPlayer = MediaPlayer().apply {
                setDataSource(sound.audioUrl)
                isLooping = true
                setVolume(0.3f, 0.3f)
                setOnPreparedListener { mp ->
                    mp.start()
                }
                prepareAsync()
            }
        }
    }
    fun toggleLoopSurah() {
        val nextMode = when (_uiState.value.repeatMode) {
            RepeatMode.REPEAT_OFF -> RepeatMode.REPEAT_ONE
            RepeatMode.REPEAT_ONE -> RepeatMode.REPEAT_ALL
            RepeatMode.REPEAT_ALL -> RepeatMode.SHUFFLE
            RepeatMode.SHUFFLE -> RepeatMode.REPEAT_OFF
        }
        setRepeatMode(nextMode)
    }

    fun setRepeatMode(mode: RepeatMode) {
        _uiState.update { it.copy(repeatMode = mode, isLoopingSurah = (mode == RepeatMode.REPEAT_ONE)) }
        audioEngine.setLooping(mode == RepeatMode.REPEAT_ONE)
    }

    fun setPlaybackSpeed(speed: Float) {
        _uiState.update { it.copy(playbackSpeed = speed) }
        applyPlaybackSpeed(speed)
    }

    suspend fun getDailyVerse(langId: String): com.example.data.model.Ayah {
        // Curated inspirational verses (SurahId to AyahNum)
        val inspirationalVerses = listOf(
            Pair(2, 255), // Ayat-ul-Kursi
            Pair(2, 286), // Last verse of Baqarah
            Pair(3, 139), // Do not weaken or grieve
            Pair(24, 35), // Allah is the Light of the heavens
            Pair(39, 53), // Do not despair of the mercy of Allah
            Pair(59, 22), // He is Allah, other than whom there is no deity
            Pair(67, 1),  // Blessed is He in whose hand is dominion
            Pair(94, 5),  // For indeed, with hardship comes ease
            Pair(112, 1)  // Say He is Allah One
        )
        // Pick verse based on current day of year
        val dayOfYear = java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_YEAR)
        val pair = inspirationalVerses[dayOfYear % inspirationalVerses.size]
        
        val ayahs = QuranData.fetchAyahsForSurah(pair.first, langId)
        val ayah = ayahs.find { it.ayahNumber == pair.second } 
            ?: ayahs.firstOrNull { it.textArabic.trim().isNotBlank() && !it.textArabic.trim().equals("بِسْمِ ٱللَّهِ ٱلرَّحْمَٰنِ ٱلرَّحِيمِ") }
            ?: com.example.data.model.Ayah(surahNumber = 1, ayahNumber = 1, textArabic = "بِسْمِ ٱللَّهِ ٱلرَّحْمَٰنِ ٱلرَّحِيمِ", translations = mapOf("en" to "In the name of God, The Most Gracious, The Most Merciful"))

        // Format Arabic text to remove Bismillah if present
        val cleanArabic = com.example.util.QuranUtils.formatAyahText(ayah.surahNumber, ayah.ayahNumber, ayah.textArabic)
        return ayah.copy(textArabic = cleanArabic)
    }

    /**
     * Publishes playback position twice a second.
     *
     * Values are written to the dedicated [playbackProgress] flow (NOT to
     * [uiState]) and only when they actually changed, which keeps the recompose
     * scope limited to the small progress widget instead of the whole screen.
     */
    private fun startProgressTimer() {
        progressJob?.cancel()
        progressJob = viewModelScope.launch {
            val engine = audioEngine
            while (isActive && engine.isPlaying) {
                delay(PROGRESS_TICK_MS)
                val durationMs = engine.durationMs
                if (durationMs > 0L) {
                    val positionMs = engine.currentPositionMs.coerceIn(0L, durationMs)
                    val currentSecs = (positionMs / 1000L).toInt()
                    val durationSecs = (durationMs / 1000L).toInt().coerceAtLeast(1)
                    val progress = (positionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)

                    _playbackProgress.update { current ->
                        if (current.currentSeconds == currentSecs &&
                            current.durationSeconds == durationSecs &&
                            current.progress == progress
                        ) {
                            current
                        } else {
                            PlaybackProgress(
                                currentSeconds = currentSecs,
                                durationSeconds = durationSecs,
                                progress = progress
                            )
                        }
                    }

                    // Duration only changes when a new track is loaded — update the
                    // shared state just once instead of on every tick.
                    if (_uiState.value.audioDurationSeconds != durationSecs) {
                        _uiState.update { it.copy(audioDurationSeconds = durationSecs) }
                    }
                }
            }
        }
    }
    
    fun closeAiExplanation() {
        _uiState.update { it.copy(showAiExplanation = false, aiExplanation = null) }
    }
    
    fun explainAyah(surahId: Int, ayahNum: Int) {
        val surah = com.example.data.repository.QuranData.surahById(surahId) ?: return
        
        if (!_uiState.value.isOnline) {
            _uiState.update {
                it.copy(
                    showAiExplanation = true,
                    isAiLoading = false,
                    aiExplanation = "AI Tafsir requires an active internet connection. Please connect to Wi-Fi or mobile data."
                )
            }
            return
        }

        val prompt = "Please explain the meaning of Surah ${surah.nameEnglish} (Surah ${surahId}), Ayah ${ayahNum} from the Quran. Please provide a clear and insightful tafsir (exegesis) suitable for a general reader."
        
        _uiState.update { it.copy(showAiExplanation = true, isAiLoading = true, aiExplanation = null) }
        
        viewModelScope.launch {
            try {
                val apiKey = com.example.BuildConfig.GEMINI_API_KEY
                val request = com.example.data.GenerateContentRequest(
                    contents = listOf(com.example.data.Content(
                        parts = listOf(com.example.data.Part(text = prompt))
                    )),
                    generationConfig = com.example.data.GenerationConfig(
                        thinkingConfig = com.example.data.ThinkingConfig(thinkingLevel = "HIGH")
                    )
                )
                
                val response = com.example.data.GeminiClient.service.generateContent(apiKey, request)
                val text = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: "No explanation available."
                
                _uiState.update { it.copy(isAiLoading = false, aiExplanation = text) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isAiLoading = false, aiExplanation = "Failed to load explanation: ${e.message}") }
            }
        }
    }

    fun downloadAudio(surahId: Int, qariId: String) {
        if (_uiState.value.isAudioDownloading) return
        if (!_uiState.value.isOnline) {
            _uiState.update { it.copy(offlineNotice = "Internet connection required to download Surah audio.") }
            return
        }
        
        _uiState.update { it.copy(isAudioDownloading = true, audioDownloadProgress = 0) }
        viewModelScope.launch {
            val qari = com.example.data.repository.QuranData.qariList.find { it.id == qariId } ?: _uiState.value.selectedQari
            val audioUrl = com.example.util.QuranUtils.buildAudioUrl(qari, surahId)
            
            val success = audioRepository.downloadAudio(surahId, qariId, audioUrl) { progress ->
                _uiState.update { it.copy(audioDownloadProgress = progress) }
            }
            
            _uiState.update {
                it.copy(
                    isAudioDownloading = false,
                    offlineNotice = if (success) "Surah $surahId audio downloaded successfully for offline listening!" else "Download failed. Please check connection and try again."
                )
            }
        }
    }

    fun removeDownloadedAudio(surahId: Int, qariId: String) {
        viewModelScope.launch {
            audioRepository.removeDownloadedAudio(surahId, qariId)
            _uiState.update { it.copy(offlineNotice = "Surah $surahId downloaded audio removed.") }
        }
    }

    fun dismissOfflineNotice() {
        _uiState.update { it.copy(offlineNotice = null) }
    }

    fun triggerCloudBackup() {
        if (_uiState.value.isSyncing) return
        _uiState.update { it.copy(isSyncing = true, syncStatusMessage = "Syncing with Supabase Cloud...") }
        viewModelScope.launch {
            val lastReadPage = _uiState.value.lastRead.surahId
            val bookmarkPageList = _uiState.value.bookmarkedAyahs.mapNotNull {
                it.split(":").firstOrNull()?.toIntOrNull()
            }.distinct()

            val result = syncRepository.backupData(lastReadPage, bookmarkPageList)
            val timeStr = java.text.SimpleDateFormat("MMM dd, HH:mm", java.util.Locale.getDefault()).format(java.util.Date())
            result.fold(
                onSuccess = { msg ->
                    _uiState.update {
                        it.copy(
                            isSyncing = false,
                            syncStatusMessage = msg,
                            lastSyncTimeFormatted = timeStr
                        )
                    }
                },
                onFailure = { err ->
                    _uiState.update {
                        it.copy(
                            isSyncing = false,
                            syncStatusMessage = "Backup warning: ${err.localizedMessage ?: "Sync completed"}",
                            lastSyncTimeFormatted = timeStr
                        )
                    }
                }
            )
        }
    }

    fun restoreFromCloudBackup() {
        if (_uiState.value.isSyncing) return
        _uiState.update { it.copy(isSyncing = true, syncStatusMessage = "Restoring data from Supabase...") }
        viewModelScope.launch {
            val result = syncRepository.restoreData()
            val timeStr = java.text.SimpleDateFormat("MMM dd, HH:mm", java.util.Locale.getDefault()).format(java.util.Date())
            result.fold(
                onSuccess = { (lastPage, bookmarks) ->
                    if (lastPage in 1..114) {
                        openReaderForSurah(lastPage)
                    }
                    val newBookmarkSet = bookmarks.map { "$it:1" }.toSet()
                    if (newBookmarkSet.isNotEmpty()) {
                        _uiState.update { it.copy(bookmarkedAyahs = newBookmarkSet) }
                        prefs.edit().putStringSet("bookmarks", newBookmarkSet).apply()
                    }
                    _uiState.update {
                        it.copy(
                            isSyncing = false,
                            syncStatusMessage = "Data restored successfully! (Page $lastPage, ${bookmarks.size} Bookmarks)",
                            lastSyncTimeFormatted = timeStr
                        )
                    }
                },
                onFailure = { err ->
                    _uiState.update {
                        it.copy(
                            isSyncing = false,
                            syncStatusMessage = "No active cloud backup found or restore issue: ${err.localizedMessage}",
                            lastSyncTimeFormatted = timeStr
                        )
                    }
                }
            )
        }
    }

    fun setupDailyNotifications(context: Context, enabled: Boolean) {
        setDailyAyahReminders(enabled)
        if (enabled) {
            com.example.util.DailyInspirationWorker.scheduleDailyNotification(context)
        } else {
            com.example.util.DailyInspirationWorker.cancelDailyNotification(context)
        }
    }

    fun signOut() {
        viewModelScope.launch {
            try {
                SupabaseClient.client.auth.signOut()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            _uiState.update { it.copy(isUserSignedIn = false, currentUserEmail = null) }
        }
    }

    override fun onCleared() {
        super.onCleared()
        progressJob?.cancel()
        progressJob = null

        // Tear down playback when the app UI is finished: stop the foreground
        // service (which removes the media notification and releases the wake
        // lock) and release the ambient-sound player. The shared engine itself is
        // intentionally not released — it is a process-wide singleton that a live
        // MediaSession may still reference.
        runCatching { audioEngine.stop() }
        bgMediaPlayer?.release()
        bgMediaPlayer = null
        PlaybackService.stop(getApplication())
    }

    companion object {
        /** Playback progress polling interval (ms). */
        private const val PROGRESS_TICK_MS = 500L
    }
}