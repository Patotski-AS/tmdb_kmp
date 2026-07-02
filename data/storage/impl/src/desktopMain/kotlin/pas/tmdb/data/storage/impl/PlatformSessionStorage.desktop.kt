package pas.tmdb.data.storage.impl

import pas.tmdb.data.storage.api.SessionStorage
import java.io.File
import java.security.SecureRandom
import java.util.Base64
import java.util.prefs.Preferences
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

private const val PREFS_NODE: String = "pas.tmdb.session"
private const val PREFS_KEY: String = "encryption_key"
private const val SESSION_FILE: String = ".tmdb_kmp/session.dat"
private const val GCM_TAG_LENGTH: Int = 128
private const val GCM_IV_LENGTH: Int = 12

internal class EncryptedFileSessionStorage : SessionStorage {

    private val sessionFile: File = File(System.getProperty("user.home"), SESSION_FILE)
    private val secretKey = SecretKeySpec(loadOrCreateKey(), "AES")

    override suspend fun saveSession(sessionId: String, accountId: Int, username: String?) {
        val payload = listOf(sessionId, accountId.toString(), username.orEmpty()).joinToString("\n")
        val encrypted = encrypt(payload.toByteArray(Charsets.UTF_8))
        sessionFile.parentFile?.mkdirs()
        sessionFile.writeBytes(encrypted)
    }

    override suspend fun loadSessionId(): String? = loadFields()?.getOrNull(0)

    override suspend fun loadAccountId(): Int? =
        loadFields()?.getOrNull(1)?.toIntOrNull()?.takeIf { it > 0 }

    override suspend fun loadUsername(): String? =
        loadFields()?.getOrNull(2)?.takeIf { it.isNotBlank() }

    override suspend fun clear() {
        if (sessionFile.exists()) {
            sessionFile.delete()
        }
    }

    private fun loadFields(): List<String>? {
        if (!sessionFile.exists()) return null
        val decrypted = decrypt(sessionFile.readBytes()) ?: return null
        val text = decrypted.toString(Charsets.UTF_8)
        if (text.isBlank()) return null
        return text.split("\n")
    }

    private fun loadOrCreateKey(): ByteArray {
        val prefs = Preferences.userRoot().node(PREFS_NODE)
        val encoded = prefs.get(PREFS_KEY, null)
        if (encoded != null) {
            return Base64.getDecoder().decode(encoded)
        }
        val key = ByteArray(32)
        SecureRandom().nextBytes(key)
        prefs.put(PREFS_KEY, Base64.getEncoder().encodeToString(key))
        return key
    }

    private fun encrypt(plain: ByteArray): ByteArray {
        val iv = ByteArray(GCM_IV_LENGTH)
        SecureRandom().nextBytes(iv)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, GCMParameterSpec(GCM_TAG_LENGTH, iv))
        val encrypted = cipher.doFinal(plain)
        return iv + encrypted
    }

    private fun decrypt(data: ByteArray): ByteArray? {
        if (data.size <= GCM_IV_LENGTH) return null
        val iv = data.copyOfRange(0, GCM_IV_LENGTH)
        val encrypted = data.copyOfRange(GCM_IV_LENGTH, data.size)
        return runCatching {
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.DECRYPT_MODE, secretKey, GCMParameterSpec(GCM_TAG_LENGTH, iv))
            cipher.doFinal(encrypted)
        }.getOrNull()
    }
}

internal actual fun createPlatformSessionStorage(): SessionStorage = EncryptedFileSessionStorage()
