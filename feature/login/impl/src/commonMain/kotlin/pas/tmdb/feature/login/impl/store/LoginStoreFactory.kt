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
import pas.tmdb.feature.login.api.LoginIntent

interface LoginStore : Store<LoginIntent, LoginState, LoginLabel>

class LoginStoreFactory(
    private val storeFactory: StoreFactory,
    private val sessionClient: TmdbSessionClient,
    private val sessionRepository: SessionRepository,
    private val authResultHandler: AuthResultHandler,
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
    ) : CoroutineExecutor<LoginIntent, Action, LoginState, Message, LoginLabel>() {

        override fun executeAction(action: Action) {
            when (action) {
                is Action.SessionChanged -> dispatch(Message.SessionChanged(action.sessionState))
            }
        }

        override fun executeIntent(intent: LoginIntent) {
            when (intent) {
                LoginIntent.Login -> startLogin()
                LoginIntent.ConfirmApproval -> completeLogin()
                LoginIntent.CancelApproval -> cancelApproval()
                LoginIntent.Logout -> logout()
                LoginIntent.DismissError -> dispatch(Message.ErrorChanged(error = null))
            }
        }

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
    }

    private companion object {
        const val TMDB_AUTHENTICATE_URL: String = "https://www.themoviedb.org/authenticate/"
    }
}
