package pas.tmdb.data.tmdb.impl.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.JsonConvertException
import pas.tmdb.data.config.api.TmdbConfig
import pas.tmdb.data.tmdb.impl.dto.ConfigurationResponseDto
import pas.tmdb.data.tmdb.impl.dto.GenreListResponseDto
import pas.tmdb.data.tmdb.impl.dto.FavoriteRequestDto
import pas.tmdb.data.tmdb.impl.dto.MovieAccountStatesDto
import pas.tmdb.data.tmdb.impl.dto.MovieDetailsDto
import pas.tmdb.data.tmdb.impl.dto.PopularMoviesResponseDto
import pas.tmdb.data.tmdb.impl.dto.PopularTvResponseDto
import pas.tmdb.data.tmdb.impl.dto.TmdbErrorResponseDto
import pas.tmdb.data.tmdb.impl.dto.TvShowDetailsDto

internal class TmdbApiClient(
    private val httpClient: HttpClient,
    private val config: TmdbConfig,
) {
    suspend fun getMovieGenres(): GenreListResponseDto =
        tmdbGet("${config.baseUrl}genre/movie/list") {
            parameter("language", LANGUAGE)
        }

    suspend fun getPopularMovies(page: Int): PopularMoviesResponseDto =
        tmdbGet("${config.baseUrl}movie/popular") {
            parameter("language", LANGUAGE)
            parameter("page", page)
        }

    suspend fun getMovieDetails(movieId: Int): MovieDetailsDto =
        tmdbGet("${config.baseUrl}movie/$movieId") {
            parameter("language", LANGUAGE)
            parameter("append_to_response", "credits,videos,images,recommendations")
        }

    suspend fun getPopularTvShows(page: Int): PopularTvResponseDto =
        tmdbGet("${config.baseUrl}tv/popular") {
            parameter("language", LANGUAGE)
            parameter("page", page)
        }

    suspend fun getTvShowDetails(tvId: Int): TvShowDetailsDto =
        tmdbGet("${config.baseUrl}tv/$tvId") {
            parameter("language", LANGUAGE)
            parameter("append_to_response", "credits,videos,images,recommendations")
        }

    suspend fun searchTvShows(query: String, page: Int): PopularTvResponseDto =
        tmdbGet("${config.baseUrl}search/tv") {
            parameter("language", LANGUAGE)
            parameter("query", query)
            parameter("page", page)
            parameter("include_adult", false)
        }

    suspend fun searchMovies(query: String, page: Int): PopularMoviesResponseDto =
        tmdbGet("${config.baseUrl}search/movie") {
            parameter("language", LANGUAGE)
            parameter("query", query)
            parameter("page", page)
            parameter("include_adult", false)
        }

    suspend fun discoverMovies(
        page: Int,
        genreId: Int? = null,
        sortBy: String? = null,
    ): PopularMoviesResponseDto =
        tmdbGet("${config.baseUrl}discover/movie") {
            parameter("language", LANGUAGE)
            parameter("page", page)
            genreId?.let { parameter("with_genres", it) }
            sortBy?.let { parameter("sort_by", it) }
        }

    suspend fun getConfiguration(): ConfigurationResponseDto =
        tmdbGet("${config.baseUrl}configuration")

    suspend fun getMovieAccountStates(movieId: Int, sessionId: String): MovieAccountStatesDto =
        tmdbGet("${config.baseUrl}movie/$movieId/account_states") {
            parameter("session_id", sessionId)
        }

    suspend fun setFavorite(
        accountId: Int,
        movieId: Int,
        favorite: Boolean,
        sessionId: String,
    ) {
        ensureAccessTokenConfigured()
        val response = httpClient.post("${config.baseUrl}account/$accountId/favorite") {
            parameter("session_id", sessionId)
            contentType(ContentType.Application.Json)
            setBody(
                FavoriteRequestDto(
                    mediaType = "movie",
                    mediaId = movieId,
                    favorite = favorite,
                ),
            )
        }
        if (!response.status.isSuccess()) {
            throw response.parseTmdbError()
        }
    }

    suspend fun getFavoriteMovies(
        accountId: Int,
        sessionId: String,
        page: Int,
    ): PopularMoviesResponseDto =
        tmdbGet("${config.baseUrl}account/$accountId/favorite/movies") {
            parameter("session_id", sessionId)
            parameter("language", LANGUAGE)
            parameter("page", page)
        }

    private suspend inline fun <reified T> tmdbGet(
        url: String,
        block: HttpRequestBuilder.() -> Unit = {},
    ): T {
        ensureAccessTokenConfigured()
        val response = httpClient.get(url) {
            block()
        }
        return response.parseTmdbBody()
    }

    private fun ensureAccessTokenConfigured() {
        val accessToken = config.accessToken
        if (accessToken.isBlank() || accessToken == PLACEHOLDER_ACCESS_TOKEN) {
            throw TmdbApiException(
                "TMDB_ACCESS_TOKEN не настроен. Укажите Ключ доступа к API (v4) в local.properties и пересоберите проект.",
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
                message = "Неверный ответ TMDB. Проверьте TMDB_ACCESS_TOKEN в local.properties.",
                cause = e,
            )
        }
    }

    private suspend fun HttpResponse.parseTmdbError(): TmdbApiException {
        val error = runCatching { body<TmdbErrorResponseDto>() }.getOrNull()
        val message = error?.statusMessage?.takeIf { it.isNotBlank() }
            ?: "TMDB request failed (HTTP ${status.value})"
        return TmdbApiException(message, httpStatusCode = status.value)
    }

    private companion object {
        const val LANGUAGE: String = "ru-RU"
        const val PLACEHOLDER_ACCESS_TOKEN: String = "your_access_token_here"
    }
}
