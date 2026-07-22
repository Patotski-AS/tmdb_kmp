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
import pas.tmdb.data.tmdb.api.model.PagedPeople
import pas.tmdb.data.tmdb.api.model.PagedTvShows
import pas.tmdb.data.tmdb.api.model.Person
import pas.tmdb.data.tmdb.api.model.TvShow
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
            tvRepository = FakeTvRepository(),
            personRepository = FakePersonRepository(),
            favoriteMoviesCache = FakeFavoriteMoviesCache(),
            sessionRepository = FakeSessionRepository(),
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
            tvRepository = FakeTvRepository(),
            personRepository = FakePersonRepository(),
            favoriteMoviesCache = FakeFavoriteMoviesCache(),
            sessionRepository = FakeSessionRepository(),
        ).create()

        val state = store.states
            .filter { !it.isLoading && it.error != null }
            .first()

        assertEquals("Network error", state.error)
        assertTrue(state.items.isEmpty())
    }

    @Test
    fun contentTypeSelected_loadsPopularTvShows() = runTest {
        val tvShows = PagedTvShows(
            shows = listOf(
                TvShow(
                    id = 1396,
                    name = "Breaking Bad",
                    overview = "",
                    posterUrl = null,
                    voteAverage = 8.9,
                    firstAirDate = "2008",
                ),
            ),
            page = 1,
            totalPages = 1,
            totalResults = 1,
        )
        val tvRepository = FakeTvRepository(popularTvShows = AppResult.Success(tvShows))
        val movieRepository = FakeMovieRepository()
        val store = MediaStoreFactory(
            storeFactory = DefaultStoreFactory(),
            movieRepository = movieRepository,
            tvRepository = tvRepository,
            personRepository = FakePersonRepository(),
            favoriteMoviesCache = FakeFavoriteMoviesCache(),
            sessionRepository = FakeSessionRepository(),
        ).create()

        store.states.filter { !it.isLoading && it.items.isNotEmpty() }.first()

        store.accept(MediaIntent.OnContentTypeSelected(ContentType.Tv))

        val tvState = store.states
            .filter { it.contentType == ContentType.Tv && !it.isLoading && it.items.isNotEmpty() }
            .first()

        assertEquals("Breaking Bad", tvState.items.first().title)
        assertEquals(1, tvRepository.popularCalls)
        assertEquals(1, movieRepository.popularCalls)
    }

    @Test
    fun queryChanged_onTvTab_triggersTvSearch() = runTest {
        val searchResult = PagedTvShows(
            shows = listOf(
                TvShow(
                    id = 1399,
                    name = "Game of Thrones",
                    overview = "",
                    posterUrl = null,
                    voteAverage = 8.4,
                    firstAirDate = "2011",
                ),
            ),
            page = 1,
            totalPages = 1,
            totalResults = 1,
        )
        val tvRepository = FakeTvRepository(searchTvShows = AppResult.Success(searchResult))
        val store = MediaStoreFactory(
            storeFactory = DefaultStoreFactory(),
            movieRepository = FakeMovieRepository(),
            tvRepository = tvRepository,
            personRepository = FakePersonRepository(),
            favoriteMoviesCache = FakeFavoriteMoviesCache(),
            sessionRepository = FakeSessionRepository(),
        ).create()

        store.states.filter { !it.isLoading && it.items.isNotEmpty() }.first()
        store.accept(MediaIntent.OnContentTypeSelected(ContentType.Tv))
        store.states.filter { it.contentType == ContentType.Tv && !it.isLoading }.first()

        store.accept(MediaIntent.QueryChanged("Game"))
        advanceTimeBy(500)

        val searchState = store.states
            .filter { !it.isLoading && it.searchQuery == "Game" && it.items.isNotEmpty() }
            .first()

        assertEquals("Game of Thrones", searchState.items.first().title)
        assertEquals(1, tvRepository.searchCalls)
        assertEquals("Game", tvRepository.lastSearchQuery)
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
            tvRepository = FakeTvRepository(),
            personRepository = FakePersonRepository(),
            favoriteMoviesCache = FakeFavoriteMoviesCache(),
            sessionRepository = FakeSessionRepository(),
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
            tvRepository = FakeTvRepository(),
            personRepository = FakePersonRepository(),
            favoriteMoviesCache = FakeFavoriteMoviesCache(),
            sessionRepository = FakeSessionRepository(),
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

    @Test
    fun resetFilters_reloadsPopularMovies() = runTest {
        val movieRepository = FakeMovieRepository()
        val store = MediaStoreFactory(
            storeFactory = DefaultStoreFactory(),
            movieRepository = movieRepository,
            tvRepository = FakeTvRepository(),
            personRepository = FakePersonRepository(),
            favoriteMoviesCache = FakeFavoriteMoviesCache(),
            sessionRepository = FakeSessionRepository(),
        ).create()

        store.states.filter { !it.isLoading && it.items.isNotEmpty() }.first()

        store.accept(MediaIntent.ApplyFilters(MediaFilters(genreId = 18)))
        store.states
            .filter { !it.isLoading && it.filters.genreId == 18 }
            .first()

        store.accept(MediaIntent.ResetFilters)

        val resetState = store.states
            .filter { !it.isLoading && !it.filters.isActive && it.items.isNotEmpty() }
            .first()

        assertTrue(resetState.filters.genreId == null)
        assertTrue(resetState.filters.sortBy == null)
        assertEquals(2, movieRepository.popularCalls)
    }

    @Test
    fun loadGenres_populatesGenreList() = runTest {
        val movieRepository = FakeMovieRepository()
        val store = MediaStoreFactory(
            storeFactory = DefaultStoreFactory(),
            movieRepository = movieRepository,
            tvRepository = FakeTvRepository(),
            personRepository = FakePersonRepository(),
            favoriteMoviesCache = FakeFavoriteMoviesCache(),
            sessionRepository = FakeSessionRepository(),
        ).create()

        val state = store.states
            .filter { it.genres.isNotEmpty() }
            .first()

        assertEquals(2, state.genres.size)
        assertEquals("Драма", state.genres.first().name)
        assertEquals(1, movieRepository.genreCalls)
    }

    @Test
    fun contentTypeSelected_loadsPopularPeople() = runTest {
        val people = PagedPeople(
            people = listOf(
                Person(
                    id = 525,
                    name = "Christopher Nolan",
                    profileUrl = null,
                    knownForDepartment = "Directing",
                ),
            ),
            page = 1,
            totalPages = 1,
            totalResults = 1,
        )
        val personRepository = FakePersonRepository(popularPeople = AppResult.Success(people))
        val store = MediaStoreFactory(
            storeFactory = DefaultStoreFactory(),
            movieRepository = FakeMovieRepository(),
            tvRepository = FakeTvRepository(),
            personRepository = personRepository,
            favoriteMoviesCache = FakeFavoriteMoviesCache(),
            sessionRepository = FakeSessionRepository(),
        ).create()

        store.states.filter { !it.isLoading && it.items.isNotEmpty() }.first()

        store.accept(MediaIntent.OnContentTypeSelected(ContentType.People))

        val peopleState = store.states
            .filter { it.contentType == ContentType.People && !it.isLoading && it.items.isNotEmpty() }
            .first()

        assertEquals("Christopher Nolan", peopleState.items.first().title)
        assertEquals("Режиссура", peopleState.items.first().subtitle)
        assertEquals(1, personRepository.popularCalls)
    }

    @Test
    fun applyFilters_withSortBy_usesDiscoverEndpoint() = runTest {
        val discoverResult = PagedMovies(
            movies = listOf(
                Movie(
                    id = 4,
                    title = "Sorted Movie",
                    overview = "",
                    posterUrl = null,
                    voteAverage = 9.0,
                    releaseDate = "2021",
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
            tvRepository = FakeTvRepository(),
            personRepository = FakePersonRepository(),
            favoriteMoviesCache = FakeFavoriteMoviesCache(),
            sessionRepository = FakeSessionRepository(),
        ).create()

        store.states.filter { !it.isLoading && it.items.isNotEmpty() }.first()

        store.accept(
            MediaIntent.ApplyFilters(
                MediaFilters(sortBy = "vote_average.desc"),
            ),
        )

        val filteredState = store.states
            .filter { !it.isLoading && it.filters.sortBy == "vote_average.desc" && it.items.isNotEmpty() }
            .first()

        assertEquals("Sorted Movie", filteredState.items.first().title)
        assertEquals(1, movieRepository.discoverCalls)
        assertEquals("vote_average.desc", movieRepository.lastDiscoverSortBy)
    }
}

private class FakeSessionRepository : pas.tmdb.auth.api.SessionRepository {
    override val sessionState = kotlinx.coroutines.flow.MutableStateFlow(pas.tmdb.auth.api.SessionState.Guest)

    override suspend fun getSessionId(): String? = null

    override suspend fun logout() = Unit
}
