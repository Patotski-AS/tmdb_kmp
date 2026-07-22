package pas.tmdb.data.tmdb.impl.mapper

import pas.tmdb.data.tmdb.api.model.ImageConfiguration
import pas.tmdb.data.tmdb.api.model.PagedPeople
import pas.tmdb.data.tmdb.api.model.Person
import pas.tmdb.data.tmdb.api.model.PersonCredit
import pas.tmdb.data.tmdb.api.model.PersonCreditMediaType
import pas.tmdb.data.tmdb.api.model.PersonDetails
import pas.tmdb.data.tmdb.impl.dto.PersonCreditDto
import pas.tmdb.data.tmdb.impl.dto.PersonDetailsDto
import pas.tmdb.data.tmdb.impl.dto.PersonDto
import pas.tmdb.data.tmdb.impl.dto.PopularPeopleResponseDto

internal fun PopularPeopleResponseDto.toDomain(imageConfiguration: ImageConfiguration): PagedPeople =
    PagedPeople(
        people = results.map { it.toDomain(imageConfiguration) },
        page = page,
        totalPages = totalPages,
        totalResults = totalResults,
    )

internal fun PersonDto.toDomain(imageConfiguration: ImageConfiguration): Person =
    Person(
        id = id,
        name = name,
        profileUrl = profilePath?.toProfileUrl(imageConfiguration),
        knownForDepartment = knownForDepartment?.takeIf { it.isNotBlank() },
    )

internal fun PersonDetailsDto.toDomain(imageConfiguration: ImageConfiguration): PersonDetails {
    val castCredits = combinedCredits?.cast.orEmpty()
        .mapNotNull { it.toDomain(imageConfiguration, isCast = true) }
        .distinctBy { Triple(it.id, it.mediaType, it.characterOrJob) }
        .sortedByDescending { it.releaseDate.orEmpty() }
    val crewCredits = combinedCredits?.crew.orEmpty()
        .mapNotNull { it.toDomain(imageConfiguration, isCast = false) }
        .distinctBy { Triple(it.id, it.mediaType, it.characterOrJob) }
        .sortedByDescending { it.releaseDate.orEmpty() }

    val knownFor = castCredits
        .sortedByDescending { it.voteAverage }
        .distinctBy { it.id to it.mediaType }
        .take(KNOWN_FOR_LIMIT)
        .ifEmpty {
            crewCredits
                .sortedByDescending { it.voteAverage }
                .distinctBy { it.id to it.mediaType }
                .take(KNOWN_FOR_LIMIT)
        }

    return PersonDetails(
        id = id,
        name = name,
        biography = biography.takeIf { it.isNotBlank() },
        profileUrl = profilePath?.toProfileUrl(imageConfiguration),
        knownForDepartment = knownForDepartment?.takeIf { it.isNotBlank() },
        birthday = birthday?.takeIf { it.isNotBlank() },
        deathday = deathday?.takeIf { it.isNotBlank() },
        placeOfBirth = placeOfBirth?.takeIf { it.isNotBlank() },
        alsoKnownAs = alsoKnownAs.filter { it.isNotBlank() }.distinct(),
        knownFor = knownFor,
        castCredits = castCredits,
        crewCredits = crewCredits,
    )
}

private fun PersonCreditDto.toDomain(
    imageConfiguration: ImageConfiguration,
    isCast: Boolean,
): PersonCredit? {
    val mediaType = mediaType.toCreditMediaType() ?: return null
    val title = title?.takeIf { it.isNotBlank() }
        ?: name?.takeIf { it.isNotBlank() }
        ?: return null

    return PersonCredit(
        id = id,
        title = title,
        posterUrl = posterPath?.toPosterUrl(imageConfiguration),
        mediaType = mediaType,
        characterOrJob = if (isCast) {
            character?.takeIf { it.isNotBlank() }
        } else {
            job?.takeIf { it.isNotBlank() }
        },
        releaseDate = releaseDate?.takeIf { it.isNotBlank() }
            ?: firstAirDate?.takeIf { it.isNotBlank() },
        voteAverage = voteAverage,
        isCast = isCast,
    )
}

private fun String.toCreditMediaType(): PersonCreditMediaType? =
    when (lowercase()) {
        "movie" -> PersonCreditMediaType.Movie
        "tv" -> PersonCreditMediaType.Tv
        else -> null
    }

private fun String.toPosterUrl(imageConfiguration: ImageConfiguration): String =
    "${imageConfiguration.posterBaseUrl}${imageConfiguration.posterSize}$this"

private fun String.toProfileUrl(imageConfiguration: ImageConfiguration): String =
    "${imageConfiguration.posterBaseUrl}${imageConfiguration.profileSize}$this"

private const val KNOWN_FOR_LIMIT: Int = 8
