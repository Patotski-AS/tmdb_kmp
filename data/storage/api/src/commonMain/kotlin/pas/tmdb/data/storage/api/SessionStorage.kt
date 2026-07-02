package pas.tmdb.data.storage.api

interface SessionStorage {
    suspend fun saveSession(sessionId: String, accountId: Int, username: String?)
    suspend fun loadSessionId(): String?
    suspend fun loadAccountId(): Int?
    suspend fun loadUsername(): String?
    suspend fun clear()
}
