package pas.tmdb.data.storage.impl

import pas.tmdb.data.storage.api.SessionStorage

class InMemorySessionStorage : SessionStorage {
    private var sessionId: String? = null
    private var accountId: Int? = null
    private var username: String? = null

    override suspend fun saveSession(sessionId: String, accountId: Int, username: String?) {
        this.sessionId = sessionId
        this.accountId = accountId
        this.username = username
    }

    override suspend fun loadSessionId(): String? = sessionId

    override suspend fun loadAccountId(): Int? = accountId

    override suspend fun loadUsername(): String? = username

    override suspend fun clear() {
        sessionId = null
        accountId = null
        username = null
    }
}
