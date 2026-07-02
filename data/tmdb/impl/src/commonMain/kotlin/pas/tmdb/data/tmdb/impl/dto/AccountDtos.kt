package pas.tmdb.data.tmdb.impl.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class MovieAccountStatesDto(
    @SerialName("favorite") val favorite: Boolean = false,
)

@Serializable
internal data class FavoriteRequestDto(
    @SerialName("media_type") val mediaType: String,
    @SerialName("media_id") val mediaId: Int,
    @SerialName("favorite") val favorite: Boolean,
)
