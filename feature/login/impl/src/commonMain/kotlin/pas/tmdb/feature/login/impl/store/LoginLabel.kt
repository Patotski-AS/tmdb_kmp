package pas.tmdb.feature.login.impl.store

sealed interface LoginLabel {
    data class OpenBrowser(val url: String) : LoginLabel
}
