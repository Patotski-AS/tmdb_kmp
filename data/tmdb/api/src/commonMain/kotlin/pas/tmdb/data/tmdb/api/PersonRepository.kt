package pas.tmdb.data.tmdb.api

import pas.tmdb.core.common.AppResult
import pas.tmdb.data.tmdb.api.model.PagedPeople
import pas.tmdb.data.tmdb.api.model.PersonDetails

interface PersonRepository {
    suspend fun getPopularPeople(page: Int = 1): AppResult<PagedPeople>
    suspend fun searchPeople(query: String, page: Int = 1): AppResult<PagedPeople>
    suspend fun getPersonDetails(personId: Int): AppResult<PersonDetails>
}
