package pas.tmdb.data.storage.impl

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import pas.tmdb.data.storage.api.SessionStorage

private const val PREFS_NAME: String = "tmdb_session_secure"
private const val KEY_SESSION_ID: String = "session_id"
private const val KEY_ACCOUNT_ID: String = "account_id"
private const val KEY_USERNAME: String = "username"

internal class EncryptedSessionStorage(
    context: Context,
) : SessionStorage {

    private val preferences: SharedPreferences = createEncryptedPreferences(context)

    override suspend fun saveSession(sessionId: String, accountId: Int, username: String?) {
        preferences.edit()
            .putString(KEY_SESSION_ID, sessionId)
            .putInt(KEY_ACCOUNT_ID, accountId)
            .putString(KEY_USERNAME, username)
            .apply()
    }

    override suspend fun loadSessionId(): String? =
        preferences.getString(KEY_SESSION_ID, null)

    override suspend fun loadAccountId(): Int? {
        if (!preferences.contains(KEY_ACCOUNT_ID)) return null
        return preferences.getInt(KEY_ACCOUNT_ID, -1).takeIf { it > 0 }
    }

    override suspend fun loadUsername(): String? =
        preferences.getString(KEY_USERNAME, null)

    override suspend fun clear() {
        preferences.edit().clear().apply()
    }

    private fun createEncryptedPreferences(context: Context): SharedPreferences {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        return EncryptedSharedPreferences.create(
            context,
            PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )
    }
}

internal actual fun createPlatformSessionStorage(): SessionStorage {
    val context = StorageAndroidContext.requireContext()
    return EncryptedSessionStorage(context)
}
