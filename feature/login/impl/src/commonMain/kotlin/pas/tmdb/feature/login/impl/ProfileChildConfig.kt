package pas.tmdb.feature.login.impl

import kotlinx.serialization.Serializable
import pas.tmdb.feature.login.api.ProfileMenuItem

@Serializable
internal sealed interface ProfileChildConfig {
    @Serializable
    data object Menu : ProfileChildConfig

    @Serializable
    data class Section(val item: ProfileMenuItem) : ProfileChildConfig
}
