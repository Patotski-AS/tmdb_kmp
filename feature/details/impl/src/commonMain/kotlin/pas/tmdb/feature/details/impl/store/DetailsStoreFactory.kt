package pas.tmdb.feature.details.impl.store

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineBootstrapper
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import kotlinx.coroutines.launch
import pas.tmdb.auth.api.SessionRepository
import pas.tmdb.auth.api.SessionState
import pas.tmdb.core.common.AppResult
import pas.tmdb.data.tmdb.api.AccountRepository
import pas.tmdb.data.tmdb.api.MovieRepository
import pas.tmdb.data.tmdb.api.model.MovieDetails
import pas.tmdb.feature.details.api.DetailsIntent
import pas.tmdb.feature.details.api.DetailsModel

interface DetailsStore : Store<DetailsIntent, DetailsState, DetailsLabel>

class DetailsStoreFactory(
    private val storeFactory: StoreFactory,
    private val movieRepository: MovieRepository,
    private val accountRepository: AccountRepository,
    private val sessionRepository: SessionRepository,
) {
    fun create(movieId: Int): DetailsStore =
        object : DetailsStore, Store<DetailsIntent, DetailsState, DetailsLabel> by storeFactory.create(
            name = "DetailsStore",
            initialState = DetailsState(movieId = movieId),
            bootstrapper = BootstrapperImpl(sessionRepository),
            executorFactory = {
                ExecutorImpl(
                    movieRepository = movieRepository,
                    accountRepository = accountRepository,
                    sessionRepository = sessionRepository,
                )
            },
            reducer = ReducerImpl,
        ) {}

    private class BootstrapperImpl(
        private val sessionRepository: SessionRepository,
    ) : CoroutineBootstrapper<Action>() {
        override fun invoke() {
            dispatch(Action.LoadInitial)
            scope.launch {
                sessionRepository.sessionState.collect { sessionState ->
                    dispatch(Action.SessionChanged(sessionState))
                }
            }
        }
    }

    private class ExecutorImpl(
        private val movieRepository: MovieRepository,
        private val accountRepository: AccountRepository,
        private val sessionRepository: SessionRepository,
    ) : CoroutineExecutor<DetailsIntent, Action, DetailsState, Message, DetailsLabel>() {

        override fun executeAction(action: Action) {
            when (action) {
                Action.LoadInitial -> loadDetails()
                is Action.SessionChanged -> {
                    dispatch(Message.SessionChanged(action.sessionState))
                    if (action.sessionState is SessionState.Authenticated) {
                        loadFavoriteState()
                    } else {
                        dispatch(Message.FavoriteChanged(isFavorite = null))
                    }
                }
            }
        }

        override fun executeIntent(intent: DetailsIntent) {
            when (intent) {
                DetailsIntent.Load,
                DetailsIntent.Retry,
                -> loadDetails()

                DetailsIntent.Back -> publish(DetailsLabel.NavigateBack)

                DetailsIntent.ToggleFavorite -> toggleFavorite()
            }
        }

        private fun loadDetails() {
            val currentState = state()
            if (currentState.isLoading) return

            scope.launch {
                dispatch(Message.LoadingChanged(isLoading = true))
                dispatch(Message.ErrorChanged(error = null))

                when (val result = movieRepository.getMovieDetails(currentState.movieId)) {
                    is AppResult.Success -> {
                        dispatch(Message.DetailsLoaded(result.value.toModel()))
                        if (sessionRepository.sessionState.value is SessionState.Authenticated) {
                            loadFavoriteState()
                        }
                    }

                    is AppResult.Error -> {
                        dispatch(Message.ErrorChanged(error = result.message))
                    }
                }

                dispatch(Message.LoadingChanged(isLoading = false))
            }
        }

        private fun loadFavoriteState() {
            val movieId = state().movieId
            scope.launch {
                when (val result = accountRepository.isFavorite(movieId)) {
                    is AppResult.Success -> dispatch(Message.FavoriteChanged(isFavorite = result.value))
                    is AppResult.Error -> Unit
                }
            }
        }

        private fun toggleFavorite() {
            val currentState = state()
            if (currentState.sessionState !is SessionState.Authenticated) {
                publish(DetailsLabel.NavigateToLogin)
                return
            }
            if (currentState.isFavoriteLoading) return

            val newFavorite = !(currentState.isFavorite ?: false)
            scope.launch {
                dispatch(Message.FavoriteLoadingChanged(isFavoriteLoading = true))

                when (val result = accountRepository.setFavorite(currentState.movieId, newFavorite)) {
                    is AppResult.Success -> {
                        dispatch(Message.FavoriteChanged(isFavorite = newFavorite))
                    }

                    is AppResult.Error -> {
                        publish(DetailsLabel.ShowError(result.message))
                    }
                }

                dispatch(Message.FavoriteLoadingChanged(isFavoriteLoading = false))
            }
        }
    }

    private object ReducerImpl : Reducer<DetailsState, Message> {
        override fun DetailsState.reduce(msg: Message): DetailsState = when (msg) {
            is Message.LoadingChanged -> copy(isLoading = msg.isLoading)
            is Message.ErrorChanged -> copy(error = msg.error)
            is Message.DetailsLoaded -> copy(
                details = msg.details,
                error = null,
            )
            is Message.SessionChanged -> copy(sessionState = msg.sessionState)
            is Message.FavoriteChanged -> copy(isFavorite = msg.isFavorite)
            is Message.FavoriteLoadingChanged -> copy(isFavoriteLoading = msg.isFavoriteLoading)
        }
    }

    private sealed interface Action {
        data object LoadInitial : Action
        data class SessionChanged(val sessionState: SessionState) : Action
    }

    private sealed interface Message {
        data class LoadingChanged(val isLoading: Boolean) : Message
        data class ErrorChanged(val error: String?) : Message
        data class DetailsLoaded(val details: DetailsModel) : Message
        data class SessionChanged(val sessionState: SessionState) : Message
        data class FavoriteChanged(val isFavorite: Boolean?) : Message
        data class FavoriteLoadingChanged(val isFavoriteLoading: Boolean) : Message
    }
}

private fun MovieDetails.toModel(): DetailsModel =
    DetailsModel(
        title = title,
        overview = overview,
        posterUrl = posterUrl,
        voteAverage = voteAverage,
        releaseDate = releaseDate,
        runtime = runtime,
        genres = genres,
    )
