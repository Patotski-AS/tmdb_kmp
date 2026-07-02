package pas.tmdb.feature.media.impl.store

import com.arkivanov.mvikotlin.extensions.coroutines.states
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import pas.tmdb.core.common.AppResult
import pas.tmdb.data.tmdb.api.model.Movie
import pas.tmdb.data.tmdb.api.model.PagedMovies
import pas.tmdb.feature.media.api.ContentType
import pas.tmdb.feature.media.api.MediaFilters
import pas.tmdb.feature.media.api.MediaIntent
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class MediaStoreFactoryTest {

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
        val store = MediaStoreFactory(
            storeFactory = DefaultStoreFactory(),
            movieRepository = movieRepository,
            favoriteMoviesCache = FakeFavoriteMoviesCache(),
        ).create()

        val state = store.states
            .filter { !it.isLoading && it.items.isNotEmpty() }
            .first()

        assertEquals(1, state.items.size)
        assertEquals("Inception", state.items.first().title)
        assertEquals(ContentType.Movies, state.contentType)
        assertTrue(state.endReached)
        assertEquals(1, movieRepository.popularCalls)
    }

    @Test
    fun loadInitial_setsErrorOnFailure() = runTest {
        val movieRepository = FakeMovieRepository(
            popularMovies = AppResult.Error("Network error"),
        )
        val store = MediaStoreFactory(
            storeFactory = DefaultStoreFactory(),
            movieRepository = movieRepository,
            favoriteMoviesCache = FakeFavoriteMoviesCache(),
        ).create()

        val state = store.states
            .filter { !it.isLoading && it.error != null }
            .first()

        assertEquals("Network error", state.error)
        assertTrue(state.items.isEmpty())
    }

    @Test
    fun contentTypeSelected_switchesToTvWithoutLoading() = runTest {
        val movieRepository = FakeMovieRepository()
        val store = MediaStoreFactory(
            storeFactory = DefaultStoreFactory(),
            movieRepository = movieRepository,
            favoriteMoviesCache = FakeFavoriteMoviesCache(),
        ).create()

        store.states.filter { !it.isLoading && it.items.isNotEmpty() }.first()

        store.accept(MediaIntent.OnContentTypeSelected(ContentType.Tv))

        val tvState = store.states
            .filter { it.contentType == ContentType.Tv }
            .first()

        assertTrue(tvState.items.isEmpty())
        assertTrue(tvState.endReached)
        assertEquals(1, movieRepository.popularCalls)
    }

    @Test
    fun queryChanged_triggersSearchAfterDebounce() = runTest {
        val searchResult = PagedMovies(
            movies = listOf(
                Movie(
                    id = 2,
                    title = "Interstellar",
                    overview = "",
                    posterUrl = null,
                    voteAverage = 8.6,
                    releaseDate = "2014",
                ),
            ),
            page = 1,
            totalPages = 1,
            totalResults = 1,
        )
        val movieRepository = FakeMovieRepository(
            searchMovies = AppResult.Success(searchResult),
        )
        val store = MediaStoreFactory(
            storeFactory = DefaultStoreFactory(),
            movieRepository = movieRepository,
            favoriteMoviesCache = FakeFavoriteMoviesCache(),
        ).create()

        store.states.filter { !it.isLoading && it.items.isNotEmpty() }.first()

        store.accept(MediaIntent.QueryChanged("Inter"))

        advanceTimeBy(500)

        val searchState = store.states
            .filter { !it.isLoading && it.searchQuery == "Inter" && it.items.isNotEmpty() }
            .first()

        assertEquals("Interstellar", searchState.items.first().title)
        assertEquals(1, movieRepository.searchCalls)
        assertEquals("Inter", movieRepository.lastSearchQuery)
    }

    @Test
    fun applyFilters_usesDiscoverEndpoint() = runTest {
        val discoverResult = PagedMovies(
            movies = listOf(
                Movie(
                    id = 3,
                    title = "Drama Movie",
                    overview = "",
                    posterUrl = null,
                    voteAverage = 7.5,
                    releaseDate = "2020",
                ),
            ),
            page = 1,
            totalPages = 1,
            totalResults = 1,
        )
        val movieRepository = FakeMovieRepository(
            discoverMovies = AppResult.Success(discoverResult),
        )
        val store = MediaStoreFactory(
            storeFactory = DefaultStoreFactory(),
            movieRepository = movieRepository,
            favoriteMoviesCache = FakeFavoriteMoviesCache(),
        ).create()

        store.states.filter { !it.isLoading && it.items.isNotEmpty() }.first()

        store.accept(MediaIntent.ApplyFilters(MediaFilters(genreId = 18)))

        val filteredState = store.states
            .filter { !it.isLoading && it.filters.genreId == 18 && it.items.isNotEmpty() }
            .first()

        assertEquals("Drama Movie", filteredState.items.first().title)
        assertEquals(1, movieRepository.discoverCalls)
        assertEquals(18, movieRepository.lastDiscoverGenreId)
    }
}
