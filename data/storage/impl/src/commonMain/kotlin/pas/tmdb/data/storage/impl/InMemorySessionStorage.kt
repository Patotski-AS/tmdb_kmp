package pas.tmdb.data.storage.impl

import pas.tmdb.data.storage.api.SessionStorage

class InMemorySessionStorage : SessionStorage {
    private var sessionId: String? = null

    override suspend fun saveSessionId(sessionId: String) {
        this.sessionId = sessionId
    }

    override suspend fun loadSessionId(): String? = sessionId

    override suspend fun clear() {
        sessionId = null
    }
}
