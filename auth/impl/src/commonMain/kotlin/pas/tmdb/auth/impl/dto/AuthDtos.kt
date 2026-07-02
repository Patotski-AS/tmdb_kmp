package pas.tmdb.auth.impl.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class RequestTokenResponseDto(
    @SerialName("request_token") val requestToken: String? = null,
    @SerialName("success") val success: Boolean = false,
)

@Serializable
internal data class SessionRequestDto(
    @SerialName("request_token") val requestToken: String,
)

@Serializable
internal data class ValidateWithLoginRequestDto(
    @SerialName("username") val username: String,
    @SerialName("password") val password: String,
    @SerialName("request_token") val requestToken: String,
)

@Serializable
internal data class SessionResponseDto(
    @SerialName("session_id") val sessionId: String? = null,
    @SerialName("success") val success: Boolean = false,
)

@Serializable
internal data class AccountResponseDto(
    @SerialName("id") val id: Int = 0,
    @SerialName("username") val username: String? = null,
)
