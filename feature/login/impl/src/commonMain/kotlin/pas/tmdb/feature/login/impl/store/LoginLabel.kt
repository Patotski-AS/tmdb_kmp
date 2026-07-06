package pas.tmdb.feature.login.impl.store

import pas.tmdb.feature.login.api.ProfileMenuItem

sealed interface LoginLabel {
    data class OpenBrowser(val url: String) : LoginLabel
    data class NavigateToDetails(val movieId: Int) : LoginLabel
    data class NavigateToSection(val item: ProfileMenuItem) : LoginLabel
}
