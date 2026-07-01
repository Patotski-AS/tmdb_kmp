package pas.tmdb.feature.discover.impl.store

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineBootstrapper
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import kotlinx.coroutines.launch
import pas.tmdb.auth.api.SessionRepository
import pas.tmdb.auth.api.SessionState
import pas.tmdb.core.common.AppResult
import pas.tmdb.data.tmdb.api.MovieRepository
import pas.tmdb.data.tmdb.api.model.Movie
import pas.tmdb.feature.discover.api.DiscoverIntent
import pas.tmdb.feature.discover.api.MovieItemModel

interface DiscoverStore : Store<DiscoverIntent, DiscoverState, DiscoverLabel>

class DiscoverStoreFactory(
    private val storeFactory: StoreFactory,
    private val movieRepository: MovieRepository,
    private val sessionRepository: SessionRepository,
) {
    fun create(): DiscoverStore =
        object : DiscoverStore, Store<DiscoverIntent, DiscoverState, DiscoverLabel> by storeFactory.create(
            name = "DiscoverStore",
            initialState = DiscoverState(),
            bootstrapper = BootstrapperImpl(sessionRepository),
            executorFactory = { ExecutorImpl(movieRepository) },
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
    ) : CoroutineExecutor<DiscoverIntent, Action, DiscoverState, Message, DiscoverLabel>() {

        override fun executeAction(action: Action) {
            when (action) {
                Action.LoadInitial -> loadInitial()
                is Action.SessionChanged -> dispatch(Message.SessionChanged(action.sessionState))
            }
        }

        override fun executeIntent(intent: DiscoverIntent) {
            when (intent) {
                DiscoverIntent.Load -> loadInitial()
                DiscoverIntent.Refresh -> refresh()
                DiscoverIntent.LoadMore -> loadMore()
                is DiscoverIntent.OnMovieClick -> publish(DiscoverLabel.NavigateToDetails(intent.movieId))
            }
        }

        private fun loadInitial() {
            val currentState = state()
            if (currentState.movies.isNotEmpty() || currentState.isLoading) return
            loadPage(page = 1, refresh = false, append = false)
        }

        private fun refresh() {
            if (state().isRefreshing) return
            loadPage(page = 1, refresh = true, append = false)
        }

        private fun loadMore() {
            val currentState = state()
            if (currentState.isLoadingMore || currentState.endReached || currentState.isLoading) return
            loadPage(page = currentState.page + 1, refresh = false, append = true)
        }

        private fun loadPage(page: Int, refresh: Boolean, append: Boolean) {
            scope.launch {
                when {
                    refresh -> dispatch(Message.RefreshingChanged(isRefreshing = true))
                    append -> dispatch(Message.LoadingMoreChanged(isLoadingMore = true))
                    else -> dispatch(Message.LoadingChanged(isLoading = true))
                }
                dispatch(Message.ErrorChanged(error = null))

                when (val result = movieRepository.getPopularMovies(page)) {
                    is AppResult.Success -> {
                        val paged = result.value
                        dispatch(
                            Message.MoviesUpdated(
                                movies = paged.movies.map(Movie::toItemModel),
                                page = paged.page,
                                endReached = paged.page >= paged.totalPages,
                                append = append,
                            ),
                        )
                    }

                    is AppResult.Error -> {
                        dispatch(Message.ErrorChanged(error = result.message))
                        if (append || refresh) {
                            publish(DiscoverLabel.ShowError(result.message))
                        }
                    }
                }

                when {
                    refresh -> dispatch(Message.RefreshingChanged(isRefreshing = false))
                    append -> dispatch(Message.LoadingMoreChanged(isLoadingMore = false))
                    else -> dispatch(Message.LoadingChanged(isLoading = false))
                }
            }
        }
    }

    private object ReducerImpl : Reducer<DiscoverState, Message> {
        override fun DiscoverState.reduce(msg: Message): DiscoverState = when (msg) {
            is Message.LoadingChanged -> copy(isLoading = msg.isLoading)
            is Message.RefreshingChanged -> copy(isRefreshing = msg.isRefreshing)
            is Message.LoadingMoreChanged -> copy(isLoadingMore = msg.isLoadingMore)
            is Message.ErrorChanged -> copy(error = msg.error)
            is Message.SessionChanged -> copy(sessionState = msg.sessionState)
            is Message.MoviesUpdated -> copy(
                movies = if (msg.append) movies + msg.movies else msg.movies,
                page = msg.page,
                endReached = msg.endReached,
            )
        }
    }

    private sealed interface Action {
        data object LoadInitial : Action
        data class SessionChanged(val sessionState: SessionState) : Action
    }

    private sealed interface Message {
        data class LoadingChanged(val isLoading: Boolean) : Message
        data class RefreshingChanged(val isRefreshing: Boolean) : Message
        data class LoadingMoreChanged(val isLoadingMore: Boolean) : Message
        data class ErrorChanged(val error: String?) : Message
        data class SessionChanged(val sessionState: SessionState) : Message
        data class MoviesUpdated(
            val movies: List<MovieItemModel>,
            val page: Int,
            val endReached: Boolean,
            val append: Boolean,
        ) : Message
    }
}

private fun Movie.toItemModel(): MovieItemModel =
    MovieItemModel(
        id = id,
        title = title,
        posterUrl = posterUrl,
        voteAverage = voteAverage,
        releaseDate = releaseDate,
    )
