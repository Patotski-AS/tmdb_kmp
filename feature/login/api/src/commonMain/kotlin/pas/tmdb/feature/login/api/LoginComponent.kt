package pas.tmdb.feature.login.api

import com.arkivanov.decompose.value.Value
import kotlinx.coroutines.flow.Flow
import pas.tmdb.auth.api.SessionState

interface LoginComponent {
    val model: Value<LoginModel>
    val browserUrlEvents: Flow<String>
    fun onIntent(intent: LoginIntent)
}

data class LoginModel(
    val sessionState: SessionState = SessionState.Guest,
    val isLoading: Boolean = false,
    val isAwaitingApproval: Boolean = false,
    val error: String? = null,
)

sealed interface LoginIntent {
    data object Login : LoginIntent
    data object ConfirmApproval : LoginIntent
    data object CancelApproval : LoginIntent
    data object Logout : LoginIntent
    data object DismissError : LoginIntent
}
