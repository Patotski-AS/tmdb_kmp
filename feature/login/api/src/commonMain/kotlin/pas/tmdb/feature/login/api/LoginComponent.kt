package pas.tmdb.feature.login.api

import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.value.Value
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.Serializable
import pas.tmdb.auth.api.SessionState

interface LoginComponent {
    val model: Value<LoginModel>
    val childStack: Value<ChildStack<*, LoginChild>>
    val browserUrlEvents: Flow<String>
    fun onIntent(intent: LoginIntent)
    fun onBack(): Boolean
}

sealed interface LoginChild {
    data object Menu : LoginChild
    data class Section(val item: ProfileMenuItem) : LoginChild
}

data class LoginModel(    val sessionState: SessionState = SessionState.Guest,
    val isLoading: Boolean = false,
    val isAwaitingApproval: Boolean = false,
    val error: String? = null,
    val favorites: List<FavoriteMovieModel> = emptyList(),
    val isFavoritesLoading: Boolean = false,
    val favoritesError: String? = null,
)

data class FavoriteMovieModel(
    val id: Int,
    val title: String,
    val posterUrl: String?,
    val voteAverage: Double,
)

@Serializable
enum class ProfileMenuItem {
    Discussions,
    Lists,
    Reviews,
    Ratings,
    Watchlist,
    Favorites,
    ApiSubscription,
    EditProfile,
    Settings,
}

val ProfileMenuItem.title: String
    get() = when (this) {
        ProfileMenuItem.Discussions -> "Обсуждения"
        ProfileMenuItem.Lists -> "Списки"
        ProfileMenuItem.Reviews -> "Обзоры"
        ProfileMenuItem.Ratings -> "Рейтинги"
        ProfileMenuItem.Watchlist -> "Список отслеживания"
        ProfileMenuItem.Favorites -> "Избранное"
        ProfileMenuItem.ApiSubscription -> "API Subscription"
        ProfileMenuItem.EditProfile -> "Править профиль"
        ProfileMenuItem.Settings -> "Параметры"
    }

sealed interface LoginIntent {
    data object Login : LoginIntent
    data class LoginWithCredentials(
        val username: String,
        val password: String,
    ) : LoginIntent
    data object ConfirmApproval : LoginIntent
    data object CancelApproval : LoginIntent
    data object Logout : LoginIntent
    data object DismissError : LoginIntent
    data object RefreshFavorites : LoginIntent
    data class OnFavoriteClick(val movieId: Int) : LoginIntent
    data class OnMenuItemClick(val item: ProfileMenuItem) : LoginIntent
    data object OnViewProfileClick : LoginIntent
    data class ScreenOpened(val item: ProfileMenuItem) : LoginIntent
    data class OpenOnWebsite(val item: ProfileMenuItem) : LoginIntent
}
