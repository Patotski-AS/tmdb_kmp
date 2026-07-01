package pas.tmdb.data.tmdb.impl.mapper

import pas.tmdb.data.tmdb.api.model.ImageConfiguration
import pas.tmdb.data.tmdb.api.model.Movie
import pas.tmdb.data.tmdb.api.model.PagedMovies
import pas.tmdb.data.tmdb.impl.dto.ConfigurationResponseDto
import pas.tmdb.data.tmdb.impl.dto.MovieDto
import pas.tmdb.data.tmdb.impl.dto.PopularMoviesResponseDto

internal fun ConfigurationResponseDto.toDomain(): ImageConfiguration =
    ImageConfiguration(
        posterBaseUrl = images.secureBaseUrl,
        posterSize = images.posterSizes
            .firstOrNull { it == ImageConfiguration.DEFAULT_POSTER_SIZE }
            ?: ImageConfiguration.DEFAULT_POSTER_SIZE,
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
