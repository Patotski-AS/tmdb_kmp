package pas.tmdb.data.tmdb.impl.repository

import pas.tmdb.core.common.AppResult
import pas.tmdb.data.tmdb.api.PersonRepository
import pas.tmdb.data.tmdb.api.model.ImageConfiguration
import pas.tmdb.data.tmdb.api.model.PagedPeople
import pas.tmdb.data.tmdb.api.model.PersonDetails
import pas.tmdb.data.tmdb.impl.mapper.toDomain
import pas.tmdb.data.tmdb.impl.remote.TmdbApiClient
import pas.tmdb.data.tmdb.impl.remote.TmdbApiException

internal class PersonRepositoryImpl(
    private val apiClient: TmdbApiClient,
) : PersonRepository {

    private var cachedImageConfiguration: ImageConfiguration? = null

    override suspend fun getPopularPeople(page: Int): AppResult<PagedPeople> =
        runCatchingAppResult {
            val imageConfiguration = requireImageConfiguration()
            apiClient.getPopularPeople(page).toDomain(imageConfiguration)
        }

    override suspend fun searchPeople(query: String, page: Int): AppResult<PagedPeople> =
        runCatchingAppResult {
            val imageConfiguration = requireImageConfiguration()
            apiClient.searchPeople(query, page).toDomain(imageConfiguration)
        }

    override suspend fun getPersonDetails(personId: Int): AppResult<PersonDetails> =
        runCatchingAppResult {
            val imageConfiguration = requireImageConfiguration()
            apiClient.getPersonDetails(personId).toDomain(imageConfiguration)
        }

    private suspend fun requireImageConfiguration(): ImageConfiguration {
        cachedImageConfiguration?.let { return it }

        val configuration = apiClient.getConfiguration().toDomain()
        cachedImageConfiguration = configuration
        return configuration
    }

    private inline fun <T> runCatchingAppResult(block: () -> T): AppResult<T> =
        try {
            AppResult.Success(block())
        } catch (e: Exception) {
            AppResult.Error(
                message = e.toUserMessage(),
                cause = e,
            )
        }

    private fun Exception.toUserMessage(): String =
        when (this) {
            is TmdbApiException -> message.orEmpty()
            else -> message ?: "Network request failed"
        }
}
