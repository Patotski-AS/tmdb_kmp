package pas.tmdb.auth.impl.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import pas.tmdb.auth.api.TmdbAccountInfo
import pas.tmdb.auth.api.TmdbSessionClient
import pas.tmdb.auth.impl.dto.AccountResponseDto
import pas.tmdb.auth.impl.dto.RequestTokenResponseDto
import pas.tmdb.auth.impl.dto.SessionRequestDto
import pas.tmdb.auth.impl.dto.SessionResponseDto
import pas.tmdb.core.common.AppResult
import pas.tmdb.data.config.api.TmdbConfig

internal class TmdbSessionClientImpl(
    private val httpClient: HttpClient,
    private val config: TmdbConfig,
) : TmdbSessionClient {

    override suspend fun createRequestToken(): AppResult<String> =
        runCatchingAppResult {
            val response = httpClient.get("${config.baseUrl}authentication/token/new") {
                parameter("api_key", config.apiKey)
            }.parseBody<RequestTokenResponseDto>()

            if (!response.success || response.requestToken.isNullOrBlank()) {
                throw AuthApiException("Не удалось получить request token")
            }
            response.requestToken
        }

    override suspend fun createSession(requestToken: String): AppResult<String> =
        runCatchingAppResult {
            val response = httpClient.post("${config.baseUrl}authentication/session/new") {
                parameter("api_key", config.apiKey)
                contentType(ContentType.Application.Json)
                setBody(SessionRequestDto(requestToken = requestToken))
            }.parseBody<SessionResponseDto>()

            if (!response.success || response.sessionId.isNullOrBlank()) {
                throw AuthApiException("Не удалось создать сессию. Подтвердите вход на themoviedb.org")
            }
            response.sessionId
        }

    override suspend fun getAccount(sessionId: String): AppResult<TmdbAccountInfo> =
        runCatchingAppResult {
            val response = httpClient.get("${config.baseUrl}account") {
                parameter("api_key", config.apiKey)
                parameter("session_id", sessionId)
            }.parseBody<AccountResponseDto>()

            if (response.id <= 0) {
                throw AuthApiException("Не удалось получить данные аккаунта")
            }
            TmdbAccountInfo(
                id = response.id,
                username = response.username,
            )
        }

    private suspend inline fun <reified T> HttpResponse.parseBody(): T {
        if (!status.isSuccess()) {
            throw AuthApiException("TMDB auth request failed (HTTP ${status.value})")
        }
        return body()
    }

    private inline fun <T> runCatchingAppResult(block: () -> T): AppResult<T> =
        try {
            AppResult.Success(block())
        } catch (e: Exception) {
            AppResult.Error(
                message = when (e) {
                    is AuthApiException -> e.message.orEmpty()
                    else -> e.message ?: "Auth request failed"
                },
                cause = e,
            )
        }
}

internal class AuthApiException(
    message: String,
    cause: Throwable? = null,
) : Exception(message, cause)
