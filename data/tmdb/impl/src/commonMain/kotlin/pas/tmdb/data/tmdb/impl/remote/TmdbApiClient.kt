package pas.tmdb.data.tmdb.impl.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import pas.tmdb.data.config.api.TmdbConfig
import pas.tmdb.data.tmdb.impl.dto.ConfigurationResponseDto
import pas.tmdb.data.tmdb.impl.dto.PopularMoviesResponseDto

internal class TmdbApiClient(
    private val httpClient: HttpClient,
    private val config: TmdbConfig,
) {
    suspend fun getPopularMovies(page: Int): PopularMoviesResponseDto =
        httpClient.get("${config.baseUrl}movie/popular") {
            parameter("api_key", config.apiKey)
            parameter("language", LANGUAGE)
            parameter("page", page)
        }.body()

    suspend fun getConfiguration(): ConfigurationResponseDto =
        httpClient.get("${config.baseUrl}configuration") {
            parameter("api_key", config.apiKey)
        }.body()

    private companion object {
        const val LANGUAGE: String = "ru-RU"
    }
}
