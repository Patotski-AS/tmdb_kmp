package pas.tmdb.feature.discover.impl.store

import pas.tmdb.core.common.AppResult
import pas.tmdb.data.tmdb.api.MovieRepository
import pas.tmdb.data.tmdb.api.model.ImageConfiguration
import pas.tmdb.data.tmdb.api.model.Movie
import pas.tmdb.data.tmdb.api.model.MovieDetails
import pas.tmdb.data.tmdb.api.model.PagedMovies

internal class FakeMovieRepository(
    private val popularMovies: AppResult<PagedMovies> = AppResult.Success(
        PagedMovies(
            movies = listOf(
                Movie(
                    id = 1,
                    title = "Test Movie",
                    overview = "Overview",
                    posterUrl = null,
                    voteAverage = 8.5,
                    releaseDate = "2024-01-01",
                ),
            ),
            page = 1,
            totalPages = 1,
            totalResults = 1,
        ),
    ),
) : MovieRepository {
    var popularCalls: Int = 0

    override suspend fun getPopularMovies(page: Int): AppResult<PagedMovies> {
        popularCalls++
        return popularMovies
    }

    override suspend fun searchMovies(query: String, page: Int): AppResult<PagedMovies> =
        AppResult.Success(PagedMovies(emptyList(), page, 0, 0))

    override suspend fun getMovieDetails(movieId: Int): AppResult<MovieDetails> =
        AppResult.Error("Not implemented")

    override suspend fun getImageConfiguration(): AppResult<ImageConfiguration> =
        AppResult.Error("Not implemented")
}
