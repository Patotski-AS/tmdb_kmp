package pas.tmdb.data.tmdb.api.model

data class PersonDetails(
    val id: Int,
    val name: String,
    val biography: String?,
    val profileUrl: String?,
    val knownForDepartment: String?,
    val birthday: String?,
    val deathday: String?,
    val placeOfBirth: String?,
    val alsoKnownAs: List<String>,
    val knownFor: List<PersonCredit>,
    val castCredits: List<PersonCredit>,
    val crewCredits: List<PersonCredit>,
)
