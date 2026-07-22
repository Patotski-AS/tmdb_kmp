package pas.tmdb.feature.person.impl.store

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
import pas.tmdb.data.tmdb.api.PersonRepository
import pas.tmdb.data.tmdb.api.model.PagedPeople
import pas.tmdb.data.tmdb.api.model.PersonCredit
import pas.tmdb.data.tmdb.api.model.PersonCreditMediaType
import pas.tmdb.data.tmdb.api.model.PersonDetails
import pas.tmdb.feature.person.api.PersonIntent
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class PersonStoreFactoryTest {

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun loadInitial_populatesPersonDetailsOnSuccess() = runTest {
        val repository = FakePersonRepository()
        val store = PersonStoreFactory(
            storeFactory = DefaultStoreFactory(),
            personRepository = repository,
        ).create(personId = 287)

        val state = store.states
            .filter { !it.isLoading && it.name != null }
            .first()

        assertEquals("Brad Pitt", state.name)
        assertEquals("Актёрское искусство", state.knownForDepartment)
        assertEquals(1, state.knownFor.size)
        assertEquals(1, state.castCredits.size)
        assertEquals(1, state.crewCredits.size)
        assertEquals(1, repository.detailsCalls)
        assertEquals(287, repository.lastPersonId)
    }

    @Test
    fun loadInitial_setsErrorOnFailure() = runTest {
        val repository = FakePersonRepository(
            details = AppResult.Error("Network error"),
        )
        val store = PersonStoreFactory(
            storeFactory = DefaultStoreFactory(),
            personRepository = repository,
        ).create(personId = 1)

        val state = store.states
            .filter { !it.isLoading && it.error != null }
            .first()

        assertEquals("Network error", state.error)
        assertEquals(null, state.name)
    }

    @Test
    fun showFullCastCredits_expandsFlag() = runTest {
        val store = PersonStoreFactory(
            storeFactory = DefaultStoreFactory(),
            personRepository = FakePersonRepository(),
        ).create(personId = 287)

        store.states.filter { !it.isLoading && it.name != null }.first()
        store.accept(PersonIntent.ShowFullCastCredits)

        val state = store.states.filter { it.showFullCastCredits }.first()
        assertTrue(state.showFullCastCredits)
    }
}

private class FakePersonRepository(
    private val details: AppResult<PersonDetails> = AppResult.Success(samplePersonDetails()),
) : PersonRepository {
    var detailsCalls: Int = 0
    var lastPersonId: Int? = null

    override suspend fun getPopularPeople(page: Int): AppResult<PagedPeople> =
        AppResult.Success(PagedPeople(emptyList(), page = 1, totalPages = 0, totalResults = 0))

    override suspend fun searchPeople(query: String, page: Int): AppResult<PagedPeople> =
        AppResult.Success(PagedPeople(emptyList(), page = 1, totalPages = 0, totalResults = 0))

    override suspend fun getPersonDetails(personId: Int): AppResult<PersonDetails> {
        detailsCalls++
        lastPersonId = personId
        return details
    }

    companion object {
        fun samplePersonDetails(): PersonDetails =
            PersonDetails(
                id = 287,
                name = "Brad Pitt",
                biography = "American actor.",
                profileUrl = null,
                knownForDepartment = "Acting",
                birthday = "1963-12-18",
                deathday = null,
                placeOfBirth = "USA",
                alsoKnownAs = listOf("William Bradley Pitt"),
                knownFor = listOf(
                    PersonCredit(
                        id = 550,
                        title = "Fight Club",
                        posterUrl = null,
                        mediaType = PersonCreditMediaType.Movie,
                        characterOrJob = "Tyler Durden",
                        releaseDate = "1999-10-15",
                        voteAverage = 8.4,
                        isCast = true,
                    ),
                ),
                castCredits = listOf(
                    PersonCredit(
                        id = 550,
                        title = "Fight Club",
                        posterUrl = null,
                        mediaType = PersonCreditMediaType.Movie,
                        characterOrJob = "Tyler Durden",
                        releaseDate = "1999-10-15",
                        voteAverage = 8.4,
                        isCast = true,
                    ),
                ),
                crewCredits = listOf(
                    PersonCredit(
                        id = 16869,
                        title = "Inglourious Basterds",
                        posterUrl = null,
                        mediaType = PersonCreditMediaType.Movie,
                        characterOrJob = "Producer",
                        releaseDate = "2009-08-21",
                        voteAverage = 8.2,
                        isCast = false,
                    ),
                ),
            )
    }
}
