package pas.tmdb.data.tmdb.impl.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class MovieDetailsDto(
    val id: Int,
    val title: String,
    val overview: String,
    @SerialName("poster_path") val posterPath: String? = null,
    @SerialName("backdrop_path") val backdropPath: String? = null,
    val tagline: String? = null,
    @SerialName("vote_average") val voteAverage: Double = 0.0,
    @SerialName("release_date") val releaseDate: String? = null,
    val runtime: Int? = null,
    val genres: List<GenreDto> = emptyList(),
    @SerialName("original_title") val originalTitle: String? = null,
    val status: String? = null,
    @SerialName("original_language") val originalLanguage: String? = null,
    val budget: Long = 0L,
    val revenue: Long = 0L,
    val credits: CreditsDto? = null,
    val videos: VideosResponseDto? = null,
    val images: ImagesResponseDto? = null,
    val recommendations: RecommendationsResponseDto? = null,
)

@Serializable
internal data class GenreDto(
    val id: Int,
    val name: String,
)

@Serializable
internal data class CreditsDto(
    val cast: List<CastMemberDto> = emptyList(),
    val crew: List<CrewMemberDto> = emptyList(),
)

@Serializable
internal data class CastMemberDto(
    val id: Int,
    val name: String,
    val character: String = "",
    val order: Int = 0,
    @SerialName("profile_path") val profilePath: String? = null,
)

@Serializable
internal data class CrewMemberDto(
    val id: Int,
    val name: String,
    val job: String = "",
)

@Serializable
internal data class VideosResponseDto(
    val results: List<VideoDto> = emptyList(),
)

@Serializable
internal data class VideoDto(
    val id: String,
    val key: String,
    val name: String,
    val site: String,
    val type: String,
    val official: Boolean = false,
)

@Serializable
internal data class ImagesResponseDto(
    val backdrops: List<ImageDto> = emptyList(),
    val posters: List<ImageDto> = emptyList(),
)

@Serializable
internal data class ImageDto(
    @SerialName("file_path") val filePath: String,
    @SerialName("aspect_ratio") val aspectRatio: Double = 1.0,
    val width: Int = 0,
)

@Serializable
internal data class RecommendationsResponseDto(
    val results: List<MovieDto> = emptyList(),
)
