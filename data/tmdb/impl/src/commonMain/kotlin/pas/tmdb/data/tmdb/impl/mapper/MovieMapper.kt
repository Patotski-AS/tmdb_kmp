package pas.tmdb.data.tmdb.impl.mapper

import pas.tmdb.data.tmdb.api.model.CastMember
import pas.tmdb.data.tmdb.api.model.CrewMember
import pas.tmdb.data.tmdb.api.model.Genre
import pas.tmdb.data.tmdb.api.model.ImageConfiguration
import pas.tmdb.data.tmdb.api.model.MediaImage
import pas.tmdb.data.tmdb.api.model.Movie
import pas.tmdb.data.tmdb.api.model.MovieDetails
import pas.tmdb.data.tmdb.api.model.PagedMovies
import pas.tmdb.data.tmdb.api.model.Video
import pas.tmdb.data.tmdb.impl.dto.CastMemberDto
import pas.tmdb.data.tmdb.impl.dto.ConfigurationResponseDto
import pas.tmdb.data.tmdb.impl.dto.CrewMemberDto
import pas.tmdb.data.tmdb.impl.dto.GenreDto
import pas.tmdb.data.tmdb.impl.dto.GenreListResponseDto
import pas.tmdb.data.tmdb.impl.dto.ImageDto
import pas.tmdb.data.tmdb.impl.dto.MovieDetailsDto
import pas.tmdb.data.tmdb.impl.dto.MovieDto
import pas.tmdb.data.tmdb.impl.dto.PopularMoviesResponseDto
import pas.tmdb.data.tmdb.impl.dto.VideoDto

internal fun GenreListResponseDto.toDomain(): List<Genre> =
    genres.map(GenreDto::toDomain)

internal fun GenreDto.toDomain(): Genre =
    Genre(id = id, name = name)

internal fun ConfigurationResponseDto.toDomain(): ImageConfiguration =
    ImageConfiguration(
        posterBaseUrl = images.secureBaseUrl,
        posterSize = images.posterSizes
            .firstOrNull { it == ImageConfiguration.DEFAULT_POSTER_SIZE }
            ?: ImageConfiguration.DEFAULT_POSTER_SIZE,
        backdropSize = images.backdropSizes
            .firstOrNull { it == ImageConfiguration.DEFAULT_BACKDROP_SIZE }
            ?: images.backdropSizes.lastOrNull { it != "original" }
            ?: ImageConfiguration.DEFAULT_BACKDROP_SIZE,
        profileSize = images.profileSizes
            .firstOrNull { it == ImageConfiguration.DEFAULT_PROFILE_SIZE }
            ?: ImageConfiguration.DEFAULT_PROFILE_SIZE,
    )

internal fun PopularMoviesResponseDto.toDomain(imageConfiguration: ImageConfiguration): PagedMovies =
    PagedMovies(
        movies = results.map { it.toDomain(imageConfiguration) },
        page = page,
        totalPages = totalPages,
        totalResults = totalResults,
    )

internal fun MovieDto.toDomain(imageConfiguration: ImageConfiguration): Movie =
    Movie(
        id = id,
        title = title,
        overview = overview,
        posterUrl = posterPath?.let { path ->
            "${imageConfiguration.posterBaseUrl}${imageConfiguration.posterSize}$path"
        },
        voteAverage = voteAverage,
        releaseDate = releaseDate,
    )

internal fun MovieDetailsDto.toDomain(imageConfiguration: ImageConfiguration): MovieDetails =
    MovieDetails(
        id = id,
        title = title,
        overview = overview,
        posterUrl = posterPath?.toPosterUrl(imageConfiguration),
        voteAverage = voteAverage,
        releaseDate = releaseDate,
        runtime = runtime,
        genres = genres.map { it.name },
        tagline = tagline?.takeIf { it.isNotBlank() },
        backdropUrl = backdropPath?.toBackdropUrl(imageConfiguration),
        originalTitle = originalTitle?.takeIf { it.isNotBlank() },
        status = status?.takeIf { it.isNotBlank() },
        originalLanguage = originalLanguage?.takeIf { it.isNotBlank() },
        budget = budget,
        revenue = revenue,
        cast = credits?.cast.orEmpty().map { it.toDomain(imageConfiguration) },
        crew = credits?.crew.orEmpty().map(CrewMemberDto::toDomain),
        videos = videos?.results.orEmpty()
            .map(VideoDto::toDomain)
            .filter { it.site.equals("YouTube", ignoreCase = true) },
        backdrops = images?.backdrops.orEmpty().map { it.toDomain(imageConfiguration, isBackdrop = true) },
        posters = images?.posters.orEmpty().map { it.toDomain(imageConfiguration, isBackdrop = false) },
        recommendations = recommendations?.results.orEmpty().map { it.toDomain(imageConfiguration) },
    )

private fun String.toPosterUrl(imageConfiguration: ImageConfiguration): String =
    "${imageConfiguration.posterBaseUrl}${imageConfiguration.posterSize}$this"

private fun String.toBackdropUrl(imageConfiguration: ImageConfiguration): String =
    "${imageConfiguration.posterBaseUrl}${imageConfiguration.backdropSize}$this"

private fun String.toProfileUrl(imageConfiguration: ImageConfiguration): String =
    "${imageConfiguration.posterBaseUrl}${imageConfiguration.profileSize}$this"

internal fun CastMemberDto.toDomain(imageConfiguration: ImageConfiguration): CastMember =
    CastMember(
        id = id,
        name = name,
        character = character,
        profileUrl = profilePath?.toProfileUrl(imageConfiguration),
        order = order,
    )

internal fun CrewMemberDto.toDomain(): CrewMember =
    CrewMember(
        id = id,
        name = name,
        job = job,
    )

internal fun VideoDto.toDomain(): Video =
    Video(
        id = id,
        key = key,
        name = name,
        site = site,
        type = type,
        official = official,
    )

internal fun ImageDto.toDomain(
    imageConfiguration: ImageConfiguration,
    isBackdrop: Boolean,
): MediaImage {
    val size = if (isBackdrop) imageConfiguration.backdropSize else imageConfiguration.posterSize
    return MediaImage(
        url = "${imageConfiguration.posterBaseUrl}$size$filePath",
        aspectRatio = aspectRatio,
        width = width,
    )
}
