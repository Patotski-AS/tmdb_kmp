package pas.tmdb.auth.api

import pas.tmdb.core.common.AppResult

data class TmdbAccountInfo(
    val id: Int,
    val username: String?,
)

data class TmdbLoginResult(
    val sessionId: String,
    val account: TmdbAccountInfo,
)

interface TmdbSessionClient {
    suspend fun createRequestToken(): AppResult<String>
    suspend fun validateWithLogin(
        requestToken: String,
        username: String,
        password: String,
    ): AppResult<String>
    suspend fun createSession(requestToken: String): AppResult<String>
    suspend fun getAccount(sessionId: String): AppResult<TmdbAccountInfo>
    suspend fun loginWithCredentials(
        username: String,
        password: String,
    ): AppResult<TmdbLoginResult>
}
