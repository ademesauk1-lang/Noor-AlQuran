package com.example.audio

import android.content.Context
import android.os.Looper
import android.os.PowerManager
import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import java.io.File

/**
 * Process-wide ExoPlayer engine used for all Qur'an recitation playback.
 *
 * Production hardening applied here:
 *  - [DefaultLoadControl] tuning (higher minimum buffer + longer back buffer) so
 *    playback keeps running smoothly on flaky mobile networks instead of
 *    re-buffering between every verse.
 *  - A [SimpleCache] backed by an LRU evictor, so streamed surahs are stored on
 *    disk and re-played instantly (and without network) the next time.
 *  - HTTP connect/read timeouts + cross-protocol redirect support, which the
 *    mp3quran.net mirrors require.
 *  - [ExoPlayer.Builder.setWakeMode] **plus** an explicit partial [PowerManager.WakeLock]
 *    held only while audio is actually playing, so playback survives screen-off
 *    and Doze without draining the battery.
 *  - Audio focus handling and "become noisy" (headphone unplug) handling.
 *
 * The player instance is shared with [PlaybackService] so the media session and
 * the in-app UI always control the exact same player.
 */
@OptIn(UnstableApi::class)
class NoorAudioEngine private constructor(private val context: Context) {

    /** Invoked once per [prepare] as soon as playback can start; receives duration in ms (-1 if unknown). */
    var onReady: ((durationMs: Long) -> Unit)? = null

    /** Invoked whenever the current media item finishes playing. */
    var onCompletion: (() -> Unit)? = null

    /** Invoked when the player fails with a recoverable-by-fallback error. */
    var onError: ((message: String) -> Unit)? = null

    /** Invoked when play/pause state changes (including session/notification initiated changes). */
    var onIsPlayingChanged: ((isPlaying: Boolean) -> Unit)? = null

    private val playerLock = Any()
    private var exoPlayer: ExoPlayer? = null
    private var wakeLock: PowerManager.WakeLock? = null
    private var readySignalled = false

    val isPlaying: Boolean
        get() = exoPlayer?.isPlaying == true

    /** `true` while a media item is loaded (i.e. playback can be resumed). */
    val hasMediaItem: Boolean
        get() = (exoPlayer?.mediaItemCount ?: 0) > 0

    val currentPositionMs: Long
        get() = exoPlayer?.currentPosition?.coerceAtLeast(0L) ?: 0L

    /** Total duration in milliseconds, or `-1` while it is still unknown. */
    val durationMs: Long
        get() {
            val duration = exoPlayer?.duration ?: C.TIME_UNSET
            return if (duration == C.TIME_UNSET || duration <= 0L) -1L else duration
        }

    /**
     * Returns the underlying media3 [Player] (creating it if needed) so that a
     * [androidx.media3.session.MediaSession] can expose it to the system UI.
     */
    fun sessionPlayer(): Player? = ensurePlayer()

    /**
     * Prepares [uri] for playback. Returns `false` when the source could not be
     * handed to the player, in which case [onError] is notified.
     */
    fun prepare(uri: String, autoPlay: Boolean = true): Boolean {
        if (uri.isBlank()) return false
        val player = ensurePlayer() ?: return false
        readySignalled = false
        return try {
            player.setMediaItem(MediaItem.fromUri(uri))
            player.playWhenReady = autoPlay
            player.prepare()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Unable to prepare audio source: ${e.message}")
            onError?.invoke(e.message ?: "Unable to prepare audio source")
            false
        }
    }

    fun play() {
        exoPlayer?.let { player ->
            if (player.playbackState == Player.STATE_IDLE) {
                player.prepare()
            }
            player.play()
        }
    }

    fun pause() {
        exoPlayer?.pause()
    }

    fun seekTo(positionMs: Long) {
        exoPlayer?.seekTo(positionMs.coerceAtLeast(0L))
    }

    fun setPlaybackSpeed(speed: Float) {
        exoPlayer?.setPlaybackSpeed(speed.coerceIn(0.5f, 2.0f))
    }

    fun setLooping(looping: Boolean) {
        exoPlayer?.repeatMode = if (looping) Player.REPEAT_MODE_ONE else Player.REPEAT_MODE_OFF
    }

    /** Stops playback and releases the current media item (the player is kept warm for reuse). */
    fun stop() {
        releaseWakeLock()
        exoPlayer?.let { player ->
            runCatching {
                player.pause()
                player.stop()
                player.clearMediaItems()
            }
        }
        readySignalled = false
    }

    /** Fully releases the player. Only use once no media session references it. */
    fun release() {
        releaseWakeLock()
        val player = synchronized(playerLock) {
            val current = exoPlayer
            exoPlayer = null
            current
        }
        runCatching { player?.release() }
    }

    private fun ensurePlayer(): ExoPlayer? = synchronized(playerLock) {
        exoPlayer?.let { return it }
        return try {
            buildPlayer().also { exoPlayer = it }
        } catch (e: Exception) {
            Log.e(TAG, "Unable to create ExoPlayer: ${e.message}")
            null
        }
    }

    private fun buildPlayer(): ExoPlayer {
        val httpDataSourceFactory = DefaultHttpDataSource.Factory()
            .setUserAgent(USER_AGENT)
            .setConnectTimeoutMs(CONNECT_TIMEOUT_MS)
            .setReadTimeoutMs(READ_TIMEOUT_MS)
            .setAllowCrossProtocolRedirects(true)

        // Reads from the on-disk cache first, then falls back to the network and
        // transparently stores whatever it streamed for the next time.
        val cacheDataSourceFactory = CacheDataSource.Factory()
            .setCache(sharedCache(context))
            .setUpstreamDataSourceFactory(httpDataSourceFactory)
            .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)

        val mediaSourceFactory = DefaultMediaSourceFactory(cacheDataSourceFactory)

        val audioAttributes = AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.AUDIO_CONTENT_TYPE_SPEECH)
            .build()

        return ExoPlayer.Builder(context)
            // All engine calls are made from the UI thread; pinning the player to
            // the main looper guarantees our callbacks run there too, which keeps
            // the Compose state updates single-threaded and predictable.
            .setLooper(Looper.getMainLooper())
            .setMediaSourceFactory(mediaSourceFactory)
            .setLoadControl(buildLoadControl())
            .setAudioAttributes(audioAttributes, /* handleAudioFocus = */ true)
            .setHandleAudioBecomingNoisy(true)
            .setWakeMode(C.WAKE_MODE_NETWORK)
            .build()
            .apply { addListener(playerListener) }
    }

    private fun buildLoadControl(): DefaultLoadControl = DefaultLoadControl.Builder()
        .setBufferDurationsMs(
            /* minBufferMs = */ MIN_BUFFER_MS,
            /* maxBufferMs = */ MAX_BUFFER_MS,
            /* bufferForPlaybackMs = */ BUFFER_FOR_PLAYBACK_MS,
            /* bufferForPlaybackAfterRebufferMs = */ BUFFER_FOR_PLAYBACK_AFTER_REBUFFER_MS
        )
        .setBackBuffer(BACK_BUFFER_MS, /* retainBackBufferFromKeyframe = */ true)
        .setPrioritizeTimeOverSizeThresholds(true)
        .build()

    private val playerListener = object : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            when (playbackState) {
                Player.STATE_READY -> {
                    if (!readySignalled) {
                        readySignalled = true
                        onReady?.invoke(durationMs)
                    }
                }
                Player.STATE_ENDED -> onCompletion?.invoke()
                else -> Unit
            }
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            if (isPlaying) acquireWakeLock() else releaseWakeLock()
            onIsPlayingChanged?.invoke(isPlaying)
        }

        override fun onPlayerError(error: PlaybackException) {
            Log.e(TAG, "Playback error ${error.errorCodeName}: ${error.message}")
            releaseWakeLock()
            onError?.invoke("${error.errorCodeName}: ${error.message ?: "Playback failed"}")
        }
    }

    private fun acquireWakeLock() {
        val held = wakeLock?.isHeld == true
        if (held) return
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager ?: return
        try {
            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WAKE_LOCK_TAG).apply {
                setReferenceCounted(false)
                // Safety net: auto-expires after 6h so a stuck lock can never drain the battery.
                acquire(WAKE_LOCK_TIMEOUT_MS)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Unable to acquire playback wake lock: ${e.message}")
        }
    }

    private fun releaseWakeLock() {
        try {
            wakeLock?.takeIf { it.isHeld }?.release()
        } catch (e: Exception) {
            Log.w(TAG, "Unable to release playback wake lock: ${e.message}")
        } finally {
            wakeLock = null
        }
    }

    companion object {
        private const val TAG = "NoorAudioEngine"
        private const val USER_AGENT = "NoorAlQuran/1.0 (Android)"
        private const val CONNECT_TIMEOUT_MS = 15_000
        private const val READ_TIMEOUT_MS = 20_000

        // Buffering: start quickly, but keep a generous safety margin in RAM so
        // marginal connections do not interrupt recitation.
        private const val MIN_BUFFER_MS = 20_000
        private const val MAX_BUFFER_MS = 90_000
        private const val BUFFER_FOR_PLAYBACK_MS = 1_500
        private const val BUFFER_FOR_PLAYBACK_AFTER_REBUFFER_MS = 3_000
        private const val BACK_BUFFER_MS = 30_000

        // On-disk stream cache (LRU). Keeps recently played surahs instant + offline.
        private const val CACHE_DIR_NAME = "noor_audio_stream_cache"
        private const val MAX_CACHE_BYTES = 256L * 1024L * 1024L // 256 MB

        private const val WAKE_LOCK_TAG = "NoorAlQuran::PlaybackWakeLock"
        private const val WAKE_LOCK_TIMEOUT_MS = 6L * 60L * 60L * 1000L

        private val cacheLock = Any()
        private var sharedCacheInstance: SimpleCache? = null

        @Volatile
        private var instance: NoorAudioEngine? = null

        /** Returns the process-wide engine, creating it on first use. */
        fun get(context: Context): NoorAudioEngine {
            instance?.let { return it }
            return synchronized(cacheLock) {
                instance ?: NoorAudioEngine(context.applicationContext).also { instance = it }
            }
        }

        /**
         * A [SimpleCache] must be a singleton per cache directory, shared by every
         * player/data-source in the process.
         */
        private fun sharedCache(context: Context): SimpleCache = synchronized(cacheLock) {
            sharedCacheInstance?.let { return it }
            val cache = SimpleCache(
                File(context.applicationContext.cacheDir, CACHE_DIR_NAME),
                LeastRecentlyUsedCacheEvictor(MAX_CACHE_BYTES),
                StandaloneDatabaseProvider(context.applicationContext)
            )
            sharedCacheInstance = cache
            cache
        }
    }
}
