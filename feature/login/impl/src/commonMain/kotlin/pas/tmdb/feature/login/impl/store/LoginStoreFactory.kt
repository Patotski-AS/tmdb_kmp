package pas.tmdb.feature.login.impl.store

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineBootstrapper
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import kotlinx.coroutines.launch
import pas.tmdb.auth.api.AuthResultHandler
import pas.tmdb.auth.api.SessionRepository
import pas.tmdb.auth.api.SessionState
import pas.tmdb.auth.api.TmdbSessionClient
import pas.tmdb.core.common.AppResult
import pas.tmdb.data.tmdb.api.AccountRepository
import pas.tmdb.data.tmdb.api.model.Movie
import pas.tmdb.feature.login.api.FavoriteMovieModel
import pas.tmdb.feature.login.api.LoginIntent
import pas.tmdb.feature.login.api.ProfileMenuItem

interface LoginStore : Store<LoginIntent, LoginState, LoginLabel>

class LoginStoreFactory(
    private val storeFactory: StoreFactory,
    private val sessionClient: TmdbSessionClient,
    private val sessionRepository: SessionRepository,
    private val authResultHandler: AuthResultHandler,
    private val accountRepository: AccountRepository,
) {
    fun create(): LoginStore =
        object : LoginStore, Store<LoginIntent, LoginState, LoginLabel> by storeFactory.create(
            name = "LoginStore",
            initialState = LoginState(),
            bootstrapper = BootstrapperImpl(sessionRepository),
            executorFactory = {
                ExecutorImpl(
                    sessionClient = sessionClient,
                    sessionRepository = sessionRepository,
                    authResultHandler = authResultHandler,
                    accountRepository = accountRepository,
                )
            },
            reducer = ReducerImpl,
        ) {}

    private class BootstrapperImpl(
        private val sessionRepository: SessionRepository,
    ) : CoroutineBootstrapper<Action>() {
        override fun invoke() {
            scope.launch {
                sessionRepository.sessionState.collect { sessionState ->
                    dispatch(Action.SessionChanged(sessionState))
                }
            }
        }
    }

    private class ExecutorImpl(
        private val sessionClient: TmdbSessionClient,
        private val sessionRepository: SessionRepository,
        private val authResultHandler: AuthResultHandler,
        private val accountRepository: AccountRepository,
    ) : CoroutineExecutor<LoginIntent, Action, LoginState, Message, LoginLabel>() {

        override fun executeAction(action: Action) {
            when (action) {
                is Action.SessionChanged -> onSessionChanged(action.sessionState)
            }
        }

        override fun executeIntent(intent: LoginIntent) {
            when (intent) {
                LoginIntent.Login -> startLogin()
                is LoginIntent.LoginWithCredentials -> loginWithCredentials(
                    username = intent.username,
                    password = intent.password,
                )
                LoginIntent.ConfirmApproval -> completeLogin()
                LoginIntent.CancelApproval -> cancelApproval()
                LoginIntent.Logout -> logout()
                LoginIntent.DismissError -> dispatch(Message.ErrorChanged(error = null))
                LoginIntent.RefreshFavorites -> loadFavorites()
                is LoginIntent.OnFavoriteClick -> publish(LoginLabel.NavigateToDetails(intent.movieId))
                is LoginIntent.OnMenuItemClick -> publish(LoginLabel.NavigateToSection(intent.item))
                LoginIntent.OnViewProfileClick -> openProfilePage()
                is LoginIntent.ScreenOpened -> onScreenOpened(intent.item)
                is LoginIntent.OpenOnWebsite -> openOnWebsite(intent.item)
            }
        }

        private fun onScreenOpened(item: ProfileMenuItem) {
            if (item == ProfileMenuItem.Favorites) {
                loadFavorites()
            }
        }

        private fun openOnWebsite(item: ProfileMenuItem) {
            if (item == ProfileMenuItem.Favorites) return
            val username = authenticatedUsername() ?: return
            val url = "$TMDB_PROFILE_BASE$username${item.pathSuffix}"
            publish(LoginLabel.OpenBrowser(url))
        }

        private fun onSessionChanged(sessionState: SessionState) {
            val wasAuthenticated = state().sessionState is SessionState.Authenticated
            dispatch(Message.SessionChanged(sessionState))
            when (sessionState) {
                is SessionState.Authenticated -> if (!wasAuthenticated) loadFavorites()
                SessionState.Guest -> dispatch(Message.FavoritesLoaded(emptyList()))
            }
        }

        private fun loadFavorites() {
            if (state().sessionState !is SessionState.Authenticated) return

            scope.launch {
                dispatch(Message.FavoritesLoadingChanged(isLoading = true))
                dispatch(Message.FavoritesErrorChanged(error = null))

                when (val result = accountRepository.getFavoriteMovies()) {
                    is AppResult.Success ->
                        dispatch(Message.FavoritesLoaded(result.value.map(Movie::toFavoriteModel)))

                    is AppResult.Error ->
                        dispatch(Message.FavoritesErrorChanged(error = result.message))
                }

                dispatch(Message.FavoritesLoadingChanged(isLoading = false))
            }
        }

        private fun openProfilePage() {
            val username = authenticatedUsername() ?: return
            publish(LoginLabel.OpenBrowser("$TMDB_PROFILE_BASE$username"))
        }

        private fun authenticatedUsername(): String? =
            (state().sessionState as? SessionState.Authenticated)
                ?.username
                ?.takeIf { it.isNotBlank() }

        private fun startLogin() {
            if (state().isLoading || state().isAwaitingApproval) return

            scope.launch {
                dispatch(Message.LoadingChanged(isLoading = true))
                dispatch(Message.ErrorChanged(error = null))

                when (val result = sessionClient.createRequestToken()) {
                    is AppResult.Success -> {
                        val requestToken = result.value
                        val authenticateUrl = "$TMDB_AUTHENTICATE_URL$requestToken"
                        dispatch(
                            Message.AwaitingApprovalChanged(
                                isAwaitingApproval = true,
                                requestToken = requestToken,
                            ),
                        )
                        publish(LoginLabel.OpenBrowser(authenticateUrl))
                    }

                    is AppResult.Error -> {
                        dispatch(Message.ErrorChanged(error = result.message))
                    }
                }

                dispatch(Message.LoadingChanged(isLoading = false))
            }
        }

        private fun completeLogin() {
            val requestToken = state().pendingRequestToken ?: return
            if (state().isLoading) return

            scope.launch {
                dispatch(Message.LoadingChanged(isLoading = true))
                dispatch(Message.ErrorChanged(error = null))

                when (val sessionResult = sessionClient.createSession(requestToken)) {
                    is AppResult.Success -> {
                        val sessionId = sessionResult.value
                        when (val accountResult = sessionClient.getAccount(sessionId)) {
                            is AppResult.Success -> {
                                authResultHandler.onLoginSuccess(
                                    sessionId = sessionId,
                                    accountId = accountResult.value.id,
                                    username = accountResult.value.username,
                                )
                                dispatch(Message.AwaitingApprovalChanged(isAwaitingApproval = false, requestToken = null))
                            }

                            is AppResult.Error -> {
                                dispatch(Message.ErrorChanged(error = accountResult.message))
                            }
                        }
                    }

                    is AppResult.Error -> {
                        dispatch(Message.ErrorChanged(error = sessionResult.message))
                    }
                }

                dispatch(Message.LoadingChanged(isLoading = false))
            }
        }

        private fun cancelApproval() {
            dispatch(Message.AwaitingApprovalChanged(isAwaitingApproval = false, requestToken = null))
        }

        private fun loginWithCredentials(username: String, password: String) {
            if (state().isLoading || state().isAwaitingApproval) return
            if (username.isBlank() || password.isBlank()) {
                dispatch(Message.ErrorChanged(error = "Введите логин и пароль"))
                return
            }

            scope.launch {
                dispatch(Message.LoadingChanged(isLoading = true))
                dispatch(Message.ErrorChanged(error = null))

                when (val result = sessionClient.loginWithCredentials(username, password)) {
                    is AppResult.Success -> {
                        authResultHandler.onLoginSuccess(
                            sessionId = result.value.sessionId,
                            accountId = result.value.account.id,
                            username = result.value.account.username,
                        )
                    }

                    is AppResult.Error -> {
                        dispatch(Message.ErrorChanged(error = result.message))
                    }
                }

                dispatch(Message.LoadingChanged(isLoading = false))
            }
        }

        private fun logout() {
            scope.launch {
                sessionRepository.logout()
            }
        }
    }

    private object ReducerImpl : Reducer<LoginState, Message> {
        override fun LoginState.reduce(msg: Message): LoginState = when (msg) {
            is Message.LoadingChanged -> copy(isLoading = msg.isLoading)
            is Message.ErrorChanged -> copy(error = msg.error)
            is Message.SessionChanged -> copy(sessionState = msg.sessionState)
            is Message.AwaitingApprovalChanged -> copy(
                isAwaitingApproval = msg.isAwaitingApproval,
                pendingRequestToken = msg.requestToken,
            )
            is Message.FavoritesLoaded -> copy(favorites = msg.favorites)
            is Message.FavoritesLoadingChanged -> copy(isFavoritesLoading = msg.isLoading)
            is Message.FavoritesErrorChanged -> copy(favoritesError = msg.error)
        }
    }

    private sealed interface Action {
        data class SessionChanged(val sessionState: SessionState) : Action
    }

    private sealed interface Message {
        data class LoadingChanged(val isLoading: Boolean) : Message
        data class ErrorChanged(val error: String?) : Message
        data class SessionChanged(val sessionState: SessionState) : Message
        data class AwaitingApprovalChanged(
            val isAwaitingApproval: Boolean,
            val requestToken: String?,
        ) : Message
        data class FavoritesLoaded(val favorites: List<FavoriteMovieModel>) : Message
        data class FavoritesLoadingChanged(val isLoading: Boolean) : Message
        data class FavoritesErrorChanged(val error: String?) : Message
    }

    private companion object {
        const val TMDB_AUTHENTICATE_URL: String = "https://www.themoviedb.org/authenticate/"
        const val TMDB_PROFILE_BASE: String = "https://www.themoviedb.org/u/"
    }
}

private val ProfileMenuItem.pathSuffix: String
    get() = when (this) {
        ProfileMenuItem.Discussions -> "/discussions"
        ProfileMenuItem.Lists -> "/lists"
        ProfileMenuItem.Reviews -> "/reviews"
        ProfileMenuItem.Ratings -> "/ratings"
        ProfileMenuItem.Watchlist -> "/watchlist"
        ProfileMenuItem.Favorites -> "/favorites"
        ProfileMenuItem.ApiSubscription -> "/settings/api"
        ProfileMenuItem.EditProfile -> "/edit"
        ProfileMenuItem.Settings -> "/settings"
    }

private fun Movie.toFavoriteModel(): FavoriteMovieModel =
    FavoriteMovieModel(
        id = id,
        title = title,
        posterUrl = posterUrl,
        voteAverage = voteAverage,
    )
