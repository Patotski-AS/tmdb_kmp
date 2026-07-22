package pas.tmdb.data.tmdb.api

import pas.tmdb.core.common.AppResult
import pas.tmdb.data.tmdb.api.model.Genre
import pas.tmdb.data.tmdb.api.model.PagedTvShows
import pas.tmdb.data.tmdb.api.model.TvShowDetails

interface TvRepository {
    suspend fun getTvGenres(): AppResult<List<Genre>>
    suspend fun getPopularTvShows(page: Int = 1): AppResult<PagedTvShows>
    suspend fun searchTvShows(query: String, page: Int = 1): AppResult<PagedTvShows>
    suspend fun discoverTvShows(
        genreId: Int? = null,
        sortBy: String? = null,
        page: Int = 1,
    ): AppResult<PagedTvShows>
    suspend fun getTvShowDetails(tvId: Int): AppResult<TvShowDetails>
}
