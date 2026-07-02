package pas.tmdb.auth.impl

import pas.tmdb.data.storage.api.SessionStorage

internal class FakeSessionStorage : SessionStorage {
    var sessionId: String? = null
    var accountId: Int? = null
    var username: String? = null

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
