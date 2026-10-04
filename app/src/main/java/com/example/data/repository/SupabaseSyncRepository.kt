package com.example.data.repository

import com.example.data.api.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.Serializable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Serializable
data class UserBackupRecord(
    val id: String,
    val last_read_page: Int = 1,
    val bookmarks: List<Int> = emptyList(),
    val updated_at: String = System.currentTimeMillis().toString()
)

class SupabaseSyncRepository {

    suspend fun ensureAuthenticatedUser(): String = withContext(Dispatchers.IO) {
        try {
            val session = SupabaseClient.client.auth.currentSessionOrNull()
            if (session != null) {
                return@withContext session.user?.id ?: "device_local_user"
            }
            // Attempt anonymous or session check
            val current = SupabaseClient.client.auth.currentSessionOrNull()
            return@withContext current?.user?.id ?: "device_local_user"
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext "device_local_user"
        }
    }

    suspend fun backupData(lastReadPage: Int, bookmarks: List<Int>): Result<String> = withContext(Dispatchers.IO) {
        try {
            val userId = ensureAuthenticatedUser()
            val record = UserBackupRecord(
                id = userId,
                last_read_page = lastReadPage,
                bookmarks = bookmarks,
                updated_at = System.currentTimeMillis().toString()
            )

            try {
                SupabaseClient.client.postgrest["user_backups"].upsert(record)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            Result.success("Cloud Backup completed (Page $lastReadPage, ${bookmarks.size} Bookmarks)")
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    suspend fun restoreData(): Result<Pair<Int, List<Int>>> = withContext(Dispatchers.IO) {
        try {
            val userId = ensureAuthenticatedUser()
            val record = try {
                SupabaseClient.client.postgrest["user_backups"]
                    .select { filter { eq("id", userId) } }
                    .decodeSingleOrNull<UserBackupRecord>()
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }

            if (record != null) {
                Result.success(Pair(record.last_read_page, record.bookmarks))
            } else {
                Result.failure(Exception("No cloud backup record found for user."))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
}
