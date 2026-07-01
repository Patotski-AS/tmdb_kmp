package pas.tmdb.data.tmdb.api

import pas.tmdb.core.common.AppResult
import pas.tmdb.data.tmdb.api.model.ImageConfiguration
import pas.tmdb.data.tmdb.api.model.PagedMovies

interface MovieRepository {
    suspend fun getPopularMovies(page: Int = 1): AppResult<PagedMovies>
    suspend fun getImageConfiguration(): AppResult<ImageConfiguration>
}
