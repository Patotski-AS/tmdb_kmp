package pas.tmdb.data.storage.impl

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.UByteVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.value
import platform.CoreFoundation.CFDictionaryCreateMutable
import platform.CoreFoundation.CFDictionarySetValue
import platform.CoreFoundation.CFTypeRefVar
import platform.CoreFoundation.kCFAllocatorDefault
import platform.CoreFoundation.kCFBooleanTrue
import platform.Foundation.NSData
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create
import platform.Security.SecItemAdd
import platform.Security.SecItemCopyMatching
import platform.Security.SecItemDelete
import platform.Security.kSecAttrAccount
import platform.Security.kSecAttrService
import platform.Security.kSecClass
import platform.Security.kSecClassGenericPassword
import platform.Security.kSecMatchLimit
import platform.Security.kSecMatchLimitOne
import platform.Security.kSecReturnData
import platform.Security.kSecValueData
import pas.tmdb.data.storage.api.SessionStorage

private const val SERVICE: String = "pas.tmdb.session"

@OptIn(ExperimentalForeignApi::class)
internal class KeychainSessionStorage : SessionStorage {

    override suspend fun saveSession(sessionId: String, accountId: Int, username: String?) {
        saveValue(KEY_SESSION_ID, sessionId)
        saveValue(KEY_ACCOUNT_ID, accountId.toString())
        saveValue(KEY_USERNAME, username.orEmpty())
    }

    override suspend fun loadSessionId(): String? = loadValue(KEY_SESSION_ID)

    override suspend fun loadAccountId(): Int? =
        loadValue(KEY_ACCOUNT_ID)?.toIntOrNull()?.takeIf { it > 0 }

    override suspend fun loadUsername(): String? =
        loadValue(KEY_USERNAME)?.takeIf { it.isNotBlank() }

    override suspend fun clear() {
        deleteValue(KEY_SESSION_ID)
        deleteValue(KEY_ACCOUNT_ID)
        deleteValue(KEY_USERNAME)
    }

    private fun saveValue(account: String, value: String) {
        deleteValue(account)
        val data = (value as NSString).dataUsingEncoding(NSUTF8StringEncoding) ?: return
        val query = CFDictionaryCreateMutable(kCFAllocatorDefault, 4, null, null) ?: return
        CFDictionarySetValue(query, kSecClass.reinterpret(), kSecClassGenericPassword.reinterpret())
        CFDictionarySetValue(query, kSecAttrService.reinterpret(), (SERVICE as NSString).reinterpret())
        CFDictionarySetValue(query, kSecAttrAccount.reinterpret(), (account as NSString).reinterpret())
        CFDictionarySetValue(query, kSecValueData.reinterpret(), data.reinterpret())
        SecItemAdd(query, null)
    }

    private fun loadValue(account: String): String? = memScoped {
        val query = CFDictionaryCreateMutable(kCFAllocatorDefault, 5, null, null) ?: return null
        CFDictionarySetValue(query, kSecClass.reinterpret(), kSecClassGenericPassword.reinterpret())
        CFDictionarySetValue(query, kSecAttrService.reinterpret(), (SERVICE as NSString).reinterpret())
        CFDictionarySetValue(query, kSecAttrAccount.reinterpret(), (account as NSString).reinterpret())
        CFDictionarySetValue(query, kSecReturnData.reinterpret(), kCFBooleanTrue.reinterpret())
        CFDictionarySetValue(query, kSecMatchLimit.reinterpret(), kSecMatchLimitOne.reinterpret())

        val result = alloc<CFTypeRefVar>()
        val status = SecItemCopyMatching(query, result.ptr)
        if (status != 0) return null

        val data = result.value as? NSData ?: return null
        NSString.create(data, NSUTF8StringEncoding) as String
    }

    private fun deleteValue(account: String) {
        val query = CFDictionaryCreateMutable(kCFAllocatorDefault, 3, null, null) ?: return
        CFDictionarySetValue(query, kSecClass.reinterpret(), kSecClassGenericPassword.reinterpret())
        CFDictionarySetValue(query, kSecAttrService.reinterpret(), (SERVICE as NSString).reinterpret())
        CFDictionarySetValue(query, kSecAttrAccount.reinterpret(), (account as NSString).reinterpret())
        SecItemDelete(query)
    }

    private companion object {
        const val KEY_SESSION_ID: String = "session_id"
        const val KEY_ACCOUNT_ID: String = "account_id"
        const val KEY_USERNAME: String = "username"
    }
}

internal actual fun createPlatformSessionStorage(): SessionStorage = KeychainSessionStorage()
