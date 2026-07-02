package pas.tmdb.feature.discover.impl.store

import com.arkivanov.mvikotlin.extensions.coroutines.states
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import pas.tmdb.auth.api.SessionState
import pas.tmdb.core.common.AppResult
import pas.tmdb.data.tmdb.api.model.Movie
import pas.tmdb.data.tmdb.api.model.PagedMovies
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class DiscoverStoreFactoryTest {

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun loadInitial_populatesMoviesOnSuccess() = runTest {
        val movies = PagedMovies(
            movies = listOf(
                Movie(
                    id = 1,
                    title = "Inception",
                    overview = "",
                    posterUrl = null,
                    voteAverage = 8.8,
                    releaseDate = "2010",
                ),
            ),
            page = 1,
            totalPages = 1,
            totalResults = 1,
        )
        val movieRepository = FakeMovieRepository(popularMovies = AppResult.Success(movies))
        val sessionRepository = FakeSessionRepository()
        val store = DiscoverStoreFactory(
            storeFactory = DefaultStoreFactory(),
            movieRepository = movieRepository,
            sessionRepository = sessionRepository,
        ).create()

        val state = store.states
            .filter { !it.isLoading && it.movies.isNotEmpty() }
            .first()

        assertEquals(1, state.movies.size)
        assertEquals("Inception", state.movies.first().title)
        assertTrue(state.endReached)
        assertEquals(1, movieRepository.popularCalls)
    }

    @Test
    fun loadInitial_setsErrorOnFailure() = runTest {
        val movieRepository = FakeMovieRepository(
            popularMovies = AppResult.Error("Network error"),
        )
        val sessionRepository = FakeSessionRepository()
        val store = DiscoverStoreFactory(
            storeFactory = DefaultStoreFactory(),
            movieRepository = movieRepository,
            sessionRepository = sessionRepository,
        ).create()

        val state = store.states
            .filter { !it.isLoading && it.error != null }
            .first()

        assertEquals("Network error", state.error)
        assertTrue(state.movies.isEmpty())
    }

    @Test
    fun sessionChanged_updatesSessionState() = runTest {
        val movieRepository = FakeMovieRepository()
        val sessionRepository = FakeSessionRepository()
        val store = DiscoverStoreFactory(
            storeFactory = DefaultStoreFactory(),
            movieRepository = movieRepository,
            sessionRepository = sessionRepository,
        ).create()

        store.states.filter { !it.isLoading }.first()

        sessionRepository.setSessionState(
            SessionState.Authenticated(accountId = 99, username = "user"),
        )

        val authenticatedState = store.states
            .filter { it.sessionState is SessionState.Authenticated }
            .first()

        val session = authenticatedState.sessionState as SessionState.Authenticated
        assertEquals(99, session.accountId)
        assertEquals("user", session.username)
        assertFalse(authenticatedState.isLoading)
    }
}
