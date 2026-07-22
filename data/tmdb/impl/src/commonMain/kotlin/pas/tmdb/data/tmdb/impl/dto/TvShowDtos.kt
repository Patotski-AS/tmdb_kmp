package pas.tmdb.data.tmdb.impl.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class PopularTvResponseDto(
    val page: Int,
    val results: List<TvShowDto>,
    @SerialName("total_pages") val totalPages: Int,
    @SerialName("total_results") val totalResults: Int,
)

@Serializable
internal data class TvShowDto(
    val id: Int,
    val name: String,
    val overview: String = "",
    @SerialName("poster_path") val posterPath: String? = null,
    @SerialName("vote_average") val voteAverage: Double = 0.0,
    @SerialName("first_air_date") val firstAirDate: String? = null,
)

@Serializable
internal data class TvShowDetailsDto(
    val id: Int,
    val name: String,
    val overview: String = "",
    @SerialName("poster_path") val posterPath: String? = null,
    @SerialName("backdrop_path") val backdropPath: String? = null,
    val tagline: String? = null,
    @SerialName("vote_average") val voteAverage: Double = 0.0,
    @SerialName("first_air_date") val firstAirDate: String? = null,
    @SerialName("episode_run_time") val episodeRunTime: List<Int> = emptyList(),
    val genres: List<GenreDto> = emptyList(),
    @SerialName("original_name") val originalName: String? = null,
    val status: String? = null,
    @SerialName("original_language") val originalLanguage: String? = null,
    @SerialName("number_of_seasons") val numberOfSeasons: Int? = null,
    @SerialName("number_of_episodes") val numberOfEpisodes: Int? = null,
    @SerialName("created_by") val createdBy: List<CreatedByDto> = emptyList(),
    val credits: CreditsDto? = null,
    val videos: VideosResponseDto? = null,
    val images: ImagesResponseDto? = null,
    val recommendations: RecommendationsTvResponseDto? = null,
)

@Serializable
internal data class CreatedByDto(
    val id: Int,
    val name: String,
)

@Serializable
internal data class RecommendationsTvResponseDto(
    val results: List<TvShowDto> = emptyList(),
)
