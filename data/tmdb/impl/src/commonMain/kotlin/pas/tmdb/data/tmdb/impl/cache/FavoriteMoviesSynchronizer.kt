package pas.tmdb.data.tmdb.impl.cache

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import pas.tmdb.auth.api.SessionRepository
import pas.tmdb.auth.api.SessionState
import pas.tmdb.core.common.AppResult
import pas.tmdb.data.tmdb.api.AccountRepository
import pas.tmdb.data.tmdb.api.FavoriteMoviesCache

internal class FavoriteMoviesSynchronizer(
    sessionRepository: SessionRepository,
    private val accountRepository: AccountRepository,
    private val favoriteMoviesCache: FavoriteMoviesCache,
) {
    private val scope = CoroutineScope(SupervisorJob())

    init {
        scope.launch {
            sessionRepository.sessionState.collect { sessionState ->
                when (sessionState) {
                    is SessionState.Guest -> favoriteMoviesCache.clear()
                    is SessionState.Authenticated -> refreshFavorites()
                }
            }
        }
    }

    private suspend fun refreshFavorites() {
        when (val result = accountRepository.getFavoriteMovieIds()) {
            is AppResult.Success -> favoriteMoviesCache.replaceAll(result.value)
            is AppResult.Error -> Unit
        }
    }
}
