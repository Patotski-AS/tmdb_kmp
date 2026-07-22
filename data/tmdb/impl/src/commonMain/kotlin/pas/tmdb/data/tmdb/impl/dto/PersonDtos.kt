package pas.tmdb.data.tmdb.impl.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class PopularPeopleResponseDto(
    val page: Int,
    val results: List<PersonDto>,
    @SerialName("total_pages") val totalPages: Int,
    @SerialName("total_results") val totalResults: Int,
)

@Serializable
internal data class PersonDto(
    val id: Int,
    val name: String,
    @SerialName("profile_path") val profilePath: String? = null,
    @SerialName("known_for_department") val knownForDepartment: String? = null,
    @SerialName("known_for") val knownFor: List<PersonKnownForDto> = emptyList(),
)

@Serializable
internal data class PersonKnownForDto(
    val id: Int,
    @SerialName("media_type") val mediaType: String = "movie",
    val title: String? = null,
    val name: String? = null,
    @SerialName("poster_path") val posterPath: String? = null,
    @SerialName("release_date") val releaseDate: String? = null,
    @SerialName("first_air_date") val firstAirDate: String? = null,
    @SerialName("vote_average") val voteAverage: Double = 0.0,
)

@Serializable
internal data class PersonDetailsDto(
    val id: Int,
    val name: String,
    val biography: String = "",
    @SerialName("profile_path") val profilePath: String? = null,
    @SerialName("known_for_department") val knownForDepartment: String? = null,
    val birthday: String? = null,
    val deathday: String? = null,
    @SerialName("place_of_birth") val placeOfBirth: String? = null,
    @SerialName("also_known_as") val alsoKnownAs: List<String> = emptyList(),
    @SerialName("combined_credits") val combinedCredits: CombinedCreditsDto? = null,
)

@Serializable
internal data class CombinedCreditsDto(
    val cast: List<PersonCreditDto> = emptyList(),
    val crew: List<PersonCreditDto> = emptyList(),
)

@Serializable
internal data class PersonCreditDto(
    val id: Int,
    @SerialName("media_type") val mediaType: String = "movie",
    val title: String? = null,
    val name: String? = null,
    @SerialName("poster_path") val posterPath: String? = null,
    @SerialName("release_date") val releaseDate: String? = null,
    @SerialName("first_air_date") val firstAirDate: String? = null,
    @SerialName("vote_average") val voteAverage: Double = 0.0,
    val character: String? = null,
    val job: String? = null,
    val popularity: Double = 0.0,
)
