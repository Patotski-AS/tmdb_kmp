package pas.tmdb.feature.details.impl.store

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineBootstrapper
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import kotlinx.coroutines.launch
import pas.tmdb.core.common.AppResult
import pas.tmdb.data.tmdb.api.MovieRepository
import pas.tmdb.data.tmdb.api.model.MovieDetails
import pas.tmdb.feature.details.api.DetailsIntent
import pas.tmdb.feature.details.api.DetailsModel

interface DetailsStore : Store<DetailsIntent, DetailsState, DetailsLabel>

class DetailsStoreFactory(
    private val storeFactory: StoreFactory,
    private val movieRepository: MovieRepository,
) {
    fun create(movieId: Int): DetailsStore =
        object : DetailsStore, Store<DetailsIntent, DetailsState, DetailsLabel> by storeFactory.create(
            name = "DetailsStore",
            initialState = DetailsState(movieId = movieId),
            bootstrapper = BootstrapperImpl(),
            executorFactory = { ExecutorImpl(movieRepository) },
            reducer = ReducerImpl,
        ) {}

    private class BootstrapperImpl : CoroutineBootstrapper<Action>() {
        override fun invoke() {
            dispatch(Action.LoadInitial)
        }
    }

    private class ExecutorImpl(
        private val movieRepository: MovieRepository,
    ) : CoroutineExecutor<DetailsIntent, Action, DetailsState, Message, DetailsLabel>() {

        override fun executeAction(action: Action) {
            when (action) {
                Action.LoadInitial -> loadDetails()
            }
        }

        override fun executeIntent(intent: DetailsIntent) {
            when (intent) {
                DetailsIntent.Load,
                DetailsIntent.Retry,
                -> loadDetails()

                DetailsIntent.Back -> publish(DetailsLabel.NavigateBack)
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
                    }

                    is AppResult.Error -> {
                        dispatch(Message.ErrorChanged(error = result.message))
                    }
                }

                dispatch(Message.LoadingChanged(isLoading = false))
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
        }
    }

    private sealed interface Action {
        data object LoadInitial : Action
    }

    private sealed interface Message {
        data class LoadingChanged(val isLoading: Boolean) : Message
        data class ErrorChanged(val error: String?) : Message
        data class DetailsLoaded(val details: DetailsModel) : Message
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
