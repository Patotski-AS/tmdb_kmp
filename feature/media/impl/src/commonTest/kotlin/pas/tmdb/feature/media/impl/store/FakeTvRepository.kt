package pas.tmdb.feature.media.impl.store

import pas.tmdb.core.common.AppResult
import pas.tmdb.data.tmdb.api.TvRepository
import pas.tmdb.data.tmdb.api.model.PagedTvShows
import pas.tmdb.data.tmdb.api.model.TvShow
import pas.tmdb.data.tmdb.api.model.TvShowDetails

internal class FakeTvRepository(
    private val popularTvShows: AppResult<PagedTvShows> = AppResult.Success(
        PagedTvShows(
            shows = listOf(
                TvShow(
                    id = 100,
                    name = "Test Show",
                    overview = "Overview",
                    posterUrl = null,
                    voteAverage = 8.0,
                    firstAirDate = "2024-01-01",
                ),
            ),
            page = 1,
            totalPages = 1,
            totalResults = 1,
        ),
    ),
    private val searchTvShows: AppResult<PagedTvShows> = AppResult.Success(
        PagedTvShows(emptyList(), page = 1, totalPages = 0, totalResults = 0),
    ),
) : TvRepository {
    var popularCalls: Int = 0
    var searchCalls: Int = 0
    var lastSearchQuery: String? = null

    override suspend fun getPopularTvShows(page: Int): AppResult<PagedTvShows> {
        popularCalls++
        return popularTvShows
    }

    override suspend fun searchTvShows(query: String, page: Int): AppResult<PagedTvShows> {
        searchCalls++
        lastSearchQuery = query
        return searchTvShows
    }

    override suspend fun getTvShowDetails(tvId: Int): AppResult<TvShowDetails> =
        AppResult.Error("Not implemented")
}
