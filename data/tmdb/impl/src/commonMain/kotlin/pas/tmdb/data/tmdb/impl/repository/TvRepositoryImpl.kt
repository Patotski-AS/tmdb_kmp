package pas.tmdb.data.tmdb.impl.repository

import pas.tmdb.core.common.AppResult
import pas.tmdb.data.tmdb.api.TvRepository
import pas.tmdb.data.tmdb.api.model.ImageConfiguration
import pas.tmdb.data.tmdb.api.model.PagedTvShows
import pas.tmdb.data.tmdb.api.model.TvShowDetails
import pas.tmdb.data.tmdb.impl.mapper.toDomain
import pas.tmdb.data.tmdb.impl.remote.TmdbApiClient
import pas.tmdb.data.tmdb.impl.remote.TmdbApiException

internal class TvRepositoryImpl(
    private val apiClient: TmdbApiClient,
) : TvRepository {

    private var cachedImageConfiguration: ImageConfiguration? = null

    override suspend fun getPopularTvShows(page: Int): AppResult<PagedTvShows> =
        runCatchingAppResult {
            val imageConfiguration = requireImageConfiguration()
            apiClient.getPopularTvShows(page).toDomain(imageConfiguration)
        }

    override suspend fun searchTvShows(query: String, page: Int): AppResult<PagedTvShows> =
        runCatchingAppResult {
            val imageConfiguration = requireImageConfiguration()
            apiClient.searchTvShows(query, page).toDomain(imageConfiguration)
        }

    override suspend fun getTvShowDetails(tvId: Int): AppResult<TvShowDetails> =
        runCatchingAppResult {
            val imageConfiguration = requireImageConfiguration()
            apiClient.getTvShowDetails(tvId).toDomain(imageConfiguration)
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
