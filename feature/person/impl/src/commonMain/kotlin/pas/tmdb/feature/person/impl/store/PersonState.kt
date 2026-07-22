package pas.tmdb.feature.person.impl.store

import pas.tmdb.feature.person.api.PersonCreditModel

data class PersonState(
    val personId: Int,
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
