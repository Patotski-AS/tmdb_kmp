package pas.tmdb.data.tmdb.impl.dto

import kotlinx.serialization.Serializable

@Serializable
internal data class GenreListResponseDto(
    val genres: List<GenreDto> = emptyList(),
)
