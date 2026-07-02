package pas.tmdb.data.tmdb.impl.cache

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import pas.tmdb.data.tmdb.api.FavoriteMoviesCache

internal class FavoriteMoviesCacheImpl : FavoriteMoviesCache {

    private val _favoriteIds = MutableStateFlow<Set<Int>>(emptySet())

    override val favoriteIds: StateFlow<Set<Int>> = _favoriteIds.asStateFlow()

    override fun setFavorite(movieId: Int, isFavorite: Boolean) {
        _favoriteIds.update { ids ->
            if (isFavorite) ids + movieId else ids - movieId
        }
    }

    override fun replaceAll(movieIds: Set<Int>) {
        _favoriteIds.value = movieIds
    }

    override fun clear() {
        _favoriteIds.value = emptySet()
    }
}
