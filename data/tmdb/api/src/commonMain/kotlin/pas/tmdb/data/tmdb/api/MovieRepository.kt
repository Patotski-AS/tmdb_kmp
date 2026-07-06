package pas.tmdb.data.tmdb.api

import pas.tmdb.core.common.AppResult
import pas.tmdb.data.tmdb.api.model.Genre
import pas.tmdb.data.tmdb.api.model.ImageConfiguration
import pas.tmdb.data.tmdb.api.model.MovieDetails
import pas.tmdb.data.tmdb.api.model.PagedMovies

interface MovieRepository {
    suspend fun getMovieGenres(): AppResult<List<Genre>>
    suspend fun getPopularMovies(page: Int = 1): AppResult<PagedMovies>
    suspend fun searchMovies(query: String, page: Int = 1): AppResult<PagedMovies>
    suspend fun discoverMovies(
        genreId: Int? = null,
        sortBy: String? = null,
        page: Int = 1,
    ): AppResult<PagedMovies>
    suspend fun getMovieDetails(movieId: Int): AppResult<MovieDetails>
    suspend fun getImageConfiguration(): AppResult<ImageConfiguration>
}
