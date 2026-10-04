package com.example.audio

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService

/**
 * Foreground playback service for Qur'an recitations.
 *
 * The service owns the [MediaSession] that wraps the shared [NoorAudioEngine]
 * player, which gives us:
 *  - audio that keeps playing while the app is backgrounded or the screen is off
 *    (the engine holds a partial wake lock only while actually playing),
 *  - a system media notification with play/pause/seek + lock-screen controls,
 *  - correct integration with Bluetooth/headset media buttons.
 *
 * It never creates a second player: the in-app screens and the media session
 * always control the same ExoPlayer instance owned by [NoorAudioEngine].
 */
@OptIn(UnstableApi::class)
class PlaybackService : MediaSessionService() {

    private var mediaSession: MediaSession? = null

    override fun onCreate() {
        super.onCreate()
        val player = NoorAudioEngine.get(this).sessionPlayer()
        mediaSession = if (player != null) {
            MediaSession.Builder(this, player)
                .setSessionActivity(openAppIntent())
                .build()
        } else {
            Log.w(TAG, "PlaybackService started without a player instance")
            null
        }
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = mediaSession

    override fun onDestroy() {
        // Only the session is released here — the shared player survives so the
        // in-app UI can keep controlling it after the service is stopped.
        mediaSession?.release()
        mediaSession = null
        super.onDestroy()
    }

    private fun openAppIntent(): PendingIntent {
        val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
            ?: Intent(Intent.ACTION_MAIN).setPackage(packageName)
        return PendingIntent.getActivity(
            this,
            /* requestCode = */ 0,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    companion object {
        private const val TAG = "PlaybackService"

        /**
         * Starts (or reuses) the foreground service. Safe to call repeatedly and
         * from any thread; failures are swallowed so playback is never blocked by
         * a service-start restriction.
         */
        fun start(context: Context) {
            try {
                val intent = Intent(context.applicationContext, PlaybackService::class.java)
                androidx.core.content.ContextCompat.startForegroundService(
                    context.applicationContext,
                    intent
                )
            } catch (e: Exception) {
                Log.w(TAG, "Unable to start playback service: ${e.message}")
            }
        }

        /** Stops the service and removes its media notification. */
        fun stop(context: Context) {
            try {
                context.applicationContext.stopService(
                    Intent(context.applicationContext, PlaybackService::class.java)
                )
            } catch (e: Exception) {
                Log.w(TAG, "Unable to stop playback service: ${e.message}")
            }
        }
    }
}
