package pas.tmdb.data.tmdb.impl.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.HttpResponse
import io.ktor.http.isSuccess
import io.ktor.serialization.JsonConvertException
import pas.tmdb.data.config.api.TmdbConfig
import pas.tmdb.data.tmdb.impl.dto.ConfigurationResponseDto
import pas.tmdb.data.tmdb.impl.dto.PopularMoviesResponseDto
import pas.tmdb.data.tmdb.impl.dto.TmdbErrorResponseDto

internal class TmdbApiClient(
    private val httpClient: HttpClient,
    private val config: TmdbConfig,
) {
    suspend fun getPopularMovies(page: Int): PopularMoviesResponseDto =
        tmdbGet("${config.baseUrl}movie/popular") {
            parameter("language", LANGUAGE)
            parameter("page", page)
        }

    suspend fun getConfiguration(): ConfigurationResponseDto =
        tmdbGet("${config.baseUrl}configuration")

    private suspend inline fun <reified T> tmdbGet(
        url: String,
        block: HttpRequestBuilder.() -> Unit = {},
    ): T {
        ensureApiKeyConfigured()
        val response = httpClient.get(url) {
            parameter("api_key", config.apiKey)
            block()
        }
        return response.parseTmdbBody()
    }

    private fun ensureApiKeyConfigured() {
        val apiKey = config.apiKey
        if (apiKey.isBlank() || apiKey == PLACEHOLDER_API_KEY) {
            throw TmdbApiException(
                "TMDB_API_KEY не настроен. Укажите ключ API (v3) в local.properties и пересоберите проект.",
            )
        }
    }

    private suspend inline fun <reified T> HttpResponse.parseTmdbBody(): T {
        if (!status.isSuccess()) {
            throw parseTmdbError()
        }

        return try {
            body()
        } catch (e: JsonConvertException) {
            throw TmdbApiException(
                message = "Неверный ответ TMDB. Проверьте TMDB_API_KEY в local.properties.",
                cause = e,
            )
        }
    }

    private suspend fun HttpResponse.parseTmdbError(): TmdbApiException {
        val error = runCatching { body<TmdbErrorResponseDto>() }.getOrNull()
        val message = error?.statusMessage?.takeIf { it.isNotBlank() }
            ?: "TMDB request failed (HTTP ${status.value})"
        return TmdbApiException(message)
    }

    private companion object {
        const val LANGUAGE: String = "ru-RU"
        const val PLACEHOLDER_API_KEY: String = "your_api_key_here"
    }
}
