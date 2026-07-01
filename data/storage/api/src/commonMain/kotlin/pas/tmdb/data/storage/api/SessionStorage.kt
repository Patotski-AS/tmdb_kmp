package pas.tmdb.data.storage.api

interface SessionStorage {
    suspend fun saveSessionId(sessionId: String)
    suspend fun loadSessionId(): String?
    suspend fun clear()
}
