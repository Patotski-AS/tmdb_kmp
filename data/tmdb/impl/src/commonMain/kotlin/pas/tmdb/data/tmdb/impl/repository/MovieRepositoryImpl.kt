package pas.tmdb.data.tmdb.impl.repository

import pas.tmdb.core.common.AppResult
import pas.tmdb.data.tmdb.api.MovieRepository
import pas.tmdb.data.tmdb.api.model.Genre
import pas.tmdb.data.tmdb.api.model.ImageConfiguration
import pas.tmdb.data.tmdb.api.model.MovieDetails
import pas.tmdb.data.tmdb.api.model.PagedMovies
import pas.tmdb.data.tmdb.impl.mapper.toDomain
import pas.tmdb.data.tmdb.impl.remote.TmdbApiClient
import pas.tmdb.data.tmdb.impl.remote.TmdbApiException

internal class MovieRepositoryImpl(
    private val apiClient: TmdbApiClient,
) : MovieRepository {

    private var cachedImageConfiguration: ImageConfiguration? = null

    override suspend fun getMovieGenres(): AppResult<List<Genre>> =
        runCatchingAppResult {
            apiClient.getMovieGenres().toDomain()
        }

    override suspend fun getPopularMovies(page: Int): AppResult<PagedMovies> =
        runCatchingAppResult {
            val imageConfiguration = requireImageConfiguration()
            apiClient.getPopularMovies(page).toDomain(imageConfiguration)
        }

    override suspend fun searchMovies(query: String, page: Int): AppResult<PagedMovies> =
        runCatchingAppResult {
            val imageConfiguration = requireImageConfiguration()
            apiClient.searchMovies(query, page).toDomain(imageConfiguration)
        }

    override suspend fun discoverMovies(
        genreId: Int?,
        sortBy: String?,
        page: Int,
    ): AppResult<PagedMovies> =
        runCatchingAppResult {
            val imageConfiguration = requireImageConfiguration()
            apiClient.discoverMovies(page = page, genreId = genreId, sortBy = sortBy)
                .toDomain(imageConfiguration)
        }

    override suspend fun getMovieDetails(movieId: Int): AppResult<MovieDetails> =
        runCatchingAppResult {
            val imageConfiguration = requireImageConfiguration()
            apiClient.getMovieDetails(movieId).toDomain(imageConfiguration)
        }

    override suspend fun getImageConfiguration(): AppResult<ImageConfiguration> =
        runCatchingAppResult {
            requireImageConfiguration()
        }

    private suspend fun requireImageConfiguration(): ImageConfiguration {
        cachedImageConfiguration?.let { return it }

        val configuration = apiClient.getConfiguration().toDomain()
        cachedImageConfiguration = configuration
        return configuration
    }

    private inline fun <T> runCatchingAppResult(block: () -> T): AppResult<T> =
        try {
            AppResult.Success(block())
        } catch (e: Exception) {
            AppResult.Error(
                message = e.toUserMessage(),
                cause = e,
            )
        }

    private fun Exception.toUserMessage(): String =
        when (this) {
            is TmdbApiException -> message.orEmpty()
            else -> message ?: "Network request failed"
        }
}
