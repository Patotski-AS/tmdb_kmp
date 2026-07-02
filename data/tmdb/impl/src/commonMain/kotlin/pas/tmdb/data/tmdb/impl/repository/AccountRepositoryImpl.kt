package pas.tmdb.data.tmdb.impl.repository

import pas.tmdb.auth.api.SessionRepository
import pas.tmdb.auth.api.SessionState
import pas.tmdb.core.common.AppResult
import pas.tmdb.data.tmdb.api.AccountRepository
import pas.tmdb.data.tmdb.api.FavoriteMoviesCache
import pas.tmdb.data.tmdb.impl.remote.TmdbApiClient
import pas.tmdb.data.tmdb.impl.remote.TmdbApiException

internal class AccountRepositoryImpl(
    private val apiClient: TmdbApiClient,
    private val sessionRepository: SessionRepository,
    private val favoriteMoviesCache: FavoriteMoviesCache,
) : AccountRepository {

    override suspend fun isFavorite(movieId: Int): AppResult<Boolean> =
        withAuthenticatedSession { sessionId, _ ->
            apiClient.getMovieAccountStates(movieId, sessionId).favorite
        }

    override suspend fun setFavorite(movieId: Int, favorite: Boolean): AppResult<Unit> {
        val result = withAuthenticatedSession { sessionId, accountId ->
            apiClient.setFavorite(
                accountId = accountId,
                movieId = movieId,
                favorite = favorite,
                sessionId = sessionId,
            )
        }
        if (result is AppResult.Success) {
            favoriteMoviesCache.setFavorite(movieId, favorite)
        }
        return result
    }

    override suspend fun getFavoriteMovieIds(): AppResult<Set<Int>> =
        withAuthenticatedSession { sessionId, accountId ->
            val ids = mutableSetOf<Int>()
            var page = 1
            var totalPages = 1
            do {
                val response = apiClient.getFavoriteMovies(
                    accountId = accountId,
                    sessionId = sessionId,
                    page = page,
                )
                ids.addAll(response.results.map { it.id })
                totalPages = response.totalPages
                page++
            } while (page <= totalPages)
            ids
        }

    private suspend inline fun <T> withAuthenticatedSession(
        block: (sessionId: String, accountId: Int) -> T,
    ): AppResult<T> {
        val sessionId = sessionRepository.getSessionId()
            ?: return AppResult.Error(NOT_AUTHENTICATED_MESSAGE)

        val accountId = (sessionRepository.sessionState.value as? SessionState.Authenticated)?.accountId
            ?: return AppResult.Error(NOT_AUTHENTICATED_MESSAGE)

        return try {
            AppResult.Success(block(sessionId, accountId))
        } catch (e: Exception) {
            if (e is TmdbApiException && e.isUnauthorized) {
                sessionRepository.logout()
            }
            AppResult.Error(
                message = when (e) {
                    is TmdbApiException -> e.message.orEmpty()
                    else -> e.message ?: "Account request failed"
                },
                cause = e,
            )
        }
    }

    private companion object {
        const val NOT_AUTHENTICATED_MESSAGE: String = "Необходима авторизация"
    }
}
