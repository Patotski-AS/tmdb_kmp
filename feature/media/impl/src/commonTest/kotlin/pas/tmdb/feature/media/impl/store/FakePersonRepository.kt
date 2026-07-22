package pas.tmdb.feature.media.impl.store

import pas.tmdb.core.common.AppResult
import pas.tmdb.data.tmdb.api.PersonRepository
import pas.tmdb.data.tmdb.api.model.PagedPeople
import pas.tmdb.data.tmdb.api.model.Person
import pas.tmdb.data.tmdb.api.model.PersonDetails

internal class FakePersonRepository(
    private val popularPeople: AppResult<PagedPeople> = AppResult.Success(
        PagedPeople(
            people = listOf(
                Person(
                    id = 287,
                    name = "Brad Pitt",
                    profileUrl = null,
                    knownForDepartment = "Acting",
                ),
            ),
            page = 1,
            totalPages = 1,
            totalResults = 1,
        ),
    ),
    private val searchPeople: AppResult<PagedPeople> = AppResult.Success(
        PagedPeople(emptyList(), page = 1, totalPages = 0, totalResults = 0),
    ),
) : PersonRepository {
    var popularCalls: Int = 0
    var searchCalls: Int = 0
    var lastSearchQuery: String? = null

    override suspend fun getPopularPeople(page: Int): AppResult<PagedPeople> {
        popularCalls++
        return popularPeople
    }

    override suspend fun searchPeople(query: String, page: Int): AppResult<PagedPeople> {
        searchCalls++
        lastSearchQuery = query
        return searchPeople
    }

    override suspend fun getPersonDetails(personId: Int): AppResult<PersonDetails> =
        AppResult.Error("Not implemented")
}
