package pas.tmdb.data.tmdb.api

import kotlinx.coroutines.flow.StateFlow

interface FavoriteMoviesCache {
    val favoriteIds: StateFlow<Set<Int>>
    fun setFavorite(movieId: Int, isFavorite: Boolean)
    fun replaceAll(movieIds: Set<Int>)
    fun clear()
}
