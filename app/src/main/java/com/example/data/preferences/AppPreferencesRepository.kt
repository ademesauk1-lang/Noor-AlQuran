package com.example.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers

/**
 * Single, process-wide DataStore instance for lightweight app preferences.
 *
 * Kept in a top-level property so that only one DataStore<Preferences> can ever
 * be created for the same file name (creating two instances for the same file
 * throws at runtime).
 */
private val Context.noorPreferencesDataStore: DataStore<Preferences> by preferencesDataStore(
    name = AppPreferencesRepository.STORE_NAME
)

/**
 * Persistent, coroutine-friendly preferences for first-run / onboarding state.
 *
 * Values are read off the main thread (DataStore performs all disk I/O on
 * [Dispatchers.IO] internally) and exposed as a [Flow] so the UI can simply
 * observe them.
 *
 * The `has_seen_welcome` flag that older versions of the app stored in the
 * legacy `quran_prefs` SharedPreferences file is migrated transparently, so
 * existing users are not shown the onboarding flow again after the upgrade.
 */
class AppPreferencesRepository(context: Context) {

    private val appContext = context.applicationContext
    private val store: DataStore<Preferences> = appContext.noorPreferencesDataStore

    /** Emits `true` once the user finished (or skipped) the onboarding flow. */
    val hasCompletedOnboarding: Flow<Boolean> = store.data.map { prefs ->
        prefs[KEY_ONBOARDING_COMPLETED] ?: false
    }

    /**
     * Reads the onboarding state once, migrating the legacy SharedPreferences
     * flag on first run so returning users skip the intro screens.
     */
    suspend fun isOnboardingCompleted(): Boolean = withContext(Dispatchers.IO) {
        val stored = store.data.first()[KEY_ONBOARDING_COMPLETED]
        if (stored != null) return@withContext stored

        val legacySeen = readLegacyFlag()
        if (legacySeen) {
            store.edit { prefs -> prefs[KEY_ONBOARDING_COMPLETED] = true }
        }
        legacySeen
    }

    /** Persists the onboarding state. */
    suspend fun setOnboardingCompleted(completed: Boolean = true) {
        withContext(Dispatchers.IO) {
            store.edit { prefs -> prefs[KEY_ONBOARDING_COMPLETED] = completed }
        }
    }

    private fun readLegacyFlag(): Boolean = runCatching {
        appContext.getSharedPreferences(LEGACY_PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(LEGACY_ONBOARDING_KEY, false)
    }.getOrDefault(false)

    companion object {
        const val STORE_NAME = "noor_al_quran_prefs"

        /** Legacy SharedPreferences file used before the DataStore migration. */
        const val LEGACY_PREFS_NAME = "quran_prefs"
        const val LEGACY_ONBOARDING_KEY = "has_seen_welcome"

        private val KEY_ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
    }
}
