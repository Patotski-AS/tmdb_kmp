package pas.tmdb.feature.details.impl.store

import com.arkivanov.mvikotlin.extensions.coroutines.labels
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
import pas.tmdb.core.common.AppResult
import pas.tmdb.data.tmdb.api.model.CastMember
import pas.tmdb.feature.details.api.DetailsImageGallerySection
import pas.tmdb.feature.details.api.DetailsIntent
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class DetailsStoreFactoryTest {

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun loadInitial_populatesCastAndVideosOnSuccess() = runTest {
        val movieRepository = FakeMovieRepository()
        val store = createStore(movieRepository = movieRepository)

        val state = store.states
            .filter { !it.isLoading && it.details.title != null }
            .first()

        assertEquals("Fight Club", state.details.title)
        assertEquals(1, state.details.cast.size)
        assertEquals("Edward Norton", state.details.cast.first().name)
        assertEquals(1, state.details.videos.size)
        assertEquals("SUXWAEX2jlg", state.details.videos.first().key)
        assertTrue(state.details.crew.isNotEmpty())
        assertEquals(1, movieRepository.detailsCalls)
        assertEquals(550, movieRepository.lastMovieId)
    }

    @Test
    fun onVideoClick_setsSelectedVideoKey() = runTest {
        val store = createStore()
        store.states.filter { it.details.title != null }.first()

        store.accept(DetailsIntent.OnVideoClick("SUXWAEX2jlg"))

        val state = store.states.filter { it.selectedVideoKey == "SUXWAEX2jlg" }.first()
        assertEquals("SUXWAEX2jlg", state.selectedVideoKey)
    }

    @Test
    fun dismissVideoPlayer_clearsSelectedVideoKey() = runTest {
        val store = createStore()
        store.states.filter { it.details.title != null }.first()
        store.accept(DetailsIntent.OnVideoClick("SUXWAEX2jlg"))
        store.states.filter { it.selectedVideoKey != null }.first()

        store.accept(DetailsIntent.DismissVideoPlayer)

        val state = store.states.filter { it.selectedVideoKey == null }.first()
        assertNull(state.selectedVideoKey)
    }

    @Test
    fun showFullCast_expandsVisibleCast() = runTest {
        val extendedCast = (1..20).map { index ->
            CastMember(
                id = index,
                name = "Actor $index",
                character = "Role $index",
                profileUrl = null,
                order = index,
            )
        }
        val store = createStore(
            movieRepository = FakeMovieRepository(
                movieDetails = AppResult.Success(
                    FakeMovieRepository.sampleMovieDetails().copy(cast = extendedCast),
                ),
            ),
        )
        store.states.filter { it.details.cast.size == 20 }.first()

        store.accept(DetailsIntent.ShowFullCast)

        val state = store.states.filter { it.details.showFullCast }.first()
        assertTrue(state.details.showFullCast)
        assertEquals(20, state.details.cast.size)
    }

    @Test
    fun onImageClick_opensGalleryState() = runTest {
        val store = createStore()
        store.states.filter { it.details.title != null }.first()

        store.accept(DetailsIntent.OnImageClick(DetailsImageGallerySection.Backdrops, index = 0))

        val state = store.states.filter { it.imageGallery != null }.first()
        assertEquals(DetailsImageGallerySection.Backdrops, state.imageGallery?.section)
        assertEquals(0, state.imageGallery?.index)
    }

    @Test
    fun loadInitial_setsErrorOnFailure() = runTest {
        val store = createStore(
            movieRepository = FakeMovieRepository(
                movieDetails = AppResult.Error("Network error"),
            ),
        )

        val state = store.states
            .filter { !it.isLoading && it.error != null }
            .first()

        assertEquals("Network error", state.error)
        assertNull(state.details.title)
    }

    private fun createStore(
        movieRepository: FakeMovieRepository = FakeMovieRepository(),
    ): DetailsStore =
        DetailsStoreFactory(
            storeFactory = DefaultStoreFactory(),
            movieRepository = movieRepository,
            accountRepository = FakeAccountRepository(),
            sessionRepository = FakeSessionRepository(),
        ).create(movieId = 550)
}
