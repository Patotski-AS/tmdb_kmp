package pas.tmdb.auth.api

import pas.tmdb.core.common.AppResult

data class TmdbAccountInfo(
    val id: Int,
    val username: String?,
)

interface TmdbSessionClient {
    suspend fun createRequestToken(): AppResult<String>
    suspend fun createSession(requestToken: String): AppResult<String>
    suspend fun getAccount(sessionId: String): AppResult<TmdbAccountInfo>
}
