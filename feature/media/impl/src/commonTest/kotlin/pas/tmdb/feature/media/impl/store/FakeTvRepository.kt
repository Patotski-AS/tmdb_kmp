package pas.tmdb.feature.media.impl.store

import pas.tmdb.core.common.AppResult
import pas.tmdb.data.tmdb.api.TvRepository
import pas.tmdb.data.tmdb.api.model.Genre
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
    private val discoverTvShows: AppResult<PagedTvShows> = AppResult.Success(
        PagedTvShows(emptyList(), page = 1, totalPages = 0, totalResults = 0),
    ),
    private val tvGenres: AppResult<List<Genre>> = AppResult.Success(
        listOf(
            Genre(id = 18, name = "Драма"),
            Genre(id = 10759, name = "Боевик и Приключения"),
        ),
    ),
) : TvRepository {
    var popularCalls: Int = 0
    var searchCalls: Int = 0
    var discoverCalls: Int = 0
    var genreCalls: Int = 0
    var lastSearchQuery: String? = null
    var lastDiscoverGenreId: Int? = null
    var lastDiscoverSortBy: String? = null

    override suspend fun getTvGenres(): AppResult<List<Genre>> {
        genreCalls++
        return tvGenres
    }

    override suspend fun getPopularTvShows(page: Int): AppResult<PagedTvShows> {
        popularCalls++
        return popularTvShows
    }

    override suspend fun searchTvShows(query: String, page: Int): AppResult<PagedTvShows> {
        searchCalls++
        lastSearchQuery = query
        return searchTvShows
    }

    override suspend fun discoverTvShows(
        genreId: Int?,
        sortBy: String?,
        page: Int,
    ): AppResult<PagedTvShows> {
        discoverCalls++
        lastDiscoverGenreId = genreId
        lastDiscoverSortBy = sortBy
        return discoverTvShows
    }

    override suspend fun getTvShowDetails(tvId: Int): AppResult<TvShowDetails> =
        AppResult.Error("Not implemented")
}
