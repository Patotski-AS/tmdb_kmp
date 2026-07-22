package pas.tmdb.feature.person.api

import com.arkivanov.decompose.value.Value
import pas.tmdb.feature.details.api.DetailsMediaType

interface PersonComponent {
    val model: Value<PersonModel>
    fun onIntent(intent: PersonIntent)
}

data class PersonCreditModel(
    val id: Int,
    val title: String,
    val posterUrl: String?,
    val mediaType: DetailsMediaType,
    val subtitle: String?,
    val voteAverage: Double?,
)

data class PersonModel(
    val name: String? = null,
    val biography: String? = null,
    val profileUrl: String? = null,
    val knownForDepartment: String? = null,
    val birthday: String? = null,
    val deathday: String? = null,
    val placeOfBirth: String? = null,
    val alsoKnownAs: List<String> = emptyList(),
    val knownFor: List<PersonCreditModel> = emptyList(),
    val castCredits: List<PersonCreditModel> = emptyList(),
    val crewCredits: List<PersonCreditModel> = emptyList(),
    val showFullCastCredits: Boolean = false,
    val showFullCrewCredits: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
)

sealed interface PersonIntent {
    data object Load : PersonIntent
    data object Retry : PersonIntent
    data object Back : PersonIntent
    data object ShowFullCastCredits : PersonIntent
    data object ShowFullCrewCredits : PersonIntent
    data class OnCreditClick(val id: Int, val mediaType: DetailsMediaType) : PersonIntent
}
