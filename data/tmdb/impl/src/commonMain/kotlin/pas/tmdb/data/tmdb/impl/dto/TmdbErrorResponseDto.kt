package pas.tmdb.data.tmdb.impl.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class TmdbErrorResponseDto(
    @SerialName("status_message") val statusMessage: String? = null,
    @SerialName("status_code") val statusCode: Int? = null,
    val success: Boolean? = null,
)
