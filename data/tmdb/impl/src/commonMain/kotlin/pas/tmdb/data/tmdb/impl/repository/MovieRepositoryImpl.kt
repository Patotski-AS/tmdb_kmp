package pas.tmdb.data.tmdb.impl.repository

import pas.tmdb.core.common.AppResult
import pas.tmdb.data.tmdb.api.MovieRepository
import pas.tmdb.data.tmdb.api.model.ImageConfiguration
import pas.tmdb.data.tmdb.api.model.PagedMovies
import pas.tmdb.data.tmdb.impl.mapper.toDomain
import pas.tmdb.data.tmdb.impl.remote.TmdbApiClient

internal class MovieRepositoryImpl(
    private val apiClient: TmdbApiClient,
) : MovieRepository {

    private var cachedImageConfiguration: ImageConfiguration? = null

    override suspend fun getPopularMovies(page: Int): AppResult<PagedMovies> =
        runCatchingAppResult {
            val imageConfiguration = requireImageConfiguration()
            apiClient.getPopularMovies(page).toDomain(imageConfiguration)
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
                message = e.message ?: "Network request failed",
                cause = e,
            )
        }
}
