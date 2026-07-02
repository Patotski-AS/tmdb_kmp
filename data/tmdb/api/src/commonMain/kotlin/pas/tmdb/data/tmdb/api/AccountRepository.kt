package pas.tmdb.data.tmdb.api

import pas.tmdb.core.common.AppResult

interface AccountRepository {
    suspend fun isFavorite(movieId: Int): AppResult<Boolean>
    suspend fun setFavorite(movieId: Int, favorite: Boolean): AppResult<Unit>
    suspend fun getFavoriteMovieIds(): AppResult<Set<Int>>
}
