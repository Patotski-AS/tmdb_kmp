package pas.tmdb.data.tmdb.impl.mapper

import pas.tmdb.data.tmdb.api.model.ImageConfiguration
import pas.tmdb.data.tmdb.api.model.PagedTvShows
import pas.tmdb.data.tmdb.api.model.TvShow
import pas.tmdb.data.tmdb.api.model.TvShowDetails
import pas.tmdb.data.tmdb.impl.dto.CreatedByDto
import pas.tmdb.data.tmdb.impl.dto.CrewMemberDto
import pas.tmdb.data.tmdb.impl.dto.PopularTvResponseDto
import pas.tmdb.data.tmdb.impl.dto.TvShowDetailsDto
import pas.tmdb.data.tmdb.impl.dto.TvShowDto
import pas.tmdb.data.tmdb.impl.dto.VideoDto

internal fun PopularTvResponseDto.toDomain(imageConfiguration: ImageConfiguration): PagedTvShows =
    PagedTvShows(
        shows = results.map { it.toDomain(imageConfiguration) },
        page = page,
        totalPages = totalPages,
        totalResults = totalResults,
    )

internal fun TvShowDto.toDomain(imageConfiguration: ImageConfiguration): TvShow =
    TvShow(
        id = id,
        name = name,
        overview = overview,
        posterUrl = posterPath?.toPosterUrl(imageConfiguration),
        voteAverage = voteAverage,
        firstAirDate = firstAirDate,
    )

internal fun TvShowDetailsDto.toDomain(imageConfiguration: ImageConfiguration): TvShowDetails =
    TvShowDetails(
        id = id,
        name = name,
        overview = overview,
        posterUrl = posterPath?.toPosterUrl(imageConfiguration),
        voteAverage = voteAverage,
        firstAirDate = firstAirDate,
        episodeRuntimeMinutes = episodeRunTime.firstOrNull()?.takeIf { it > 0 },
        genres = genres.map { it.name },
        tagline = tagline?.takeIf { it.isNotBlank() },
        backdropUrl = backdropPath?.toBackdropUrl(imageConfiguration),
        originalName = originalName?.takeIf { it.isNotBlank() },
        status = status?.takeIf { it.isNotBlank() },
        originalLanguage = originalLanguage?.takeIf { it.isNotBlank() },
        numberOfSeasons = numberOfSeasons?.takeIf { it > 0 },
        numberOfEpisodes = numberOfEpisodes?.takeIf { it > 0 },
        cast = credits?.cast.orEmpty().map { it.toDomain(imageConfiguration) },
        crew = credits?.crew.orEmpty().map(CrewMemberDto::toDomain),
        createdBy = createdBy.map(CreatedByDto::name).filter { it.isNotBlank() }.distinct(),
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
