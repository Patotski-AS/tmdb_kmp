package pas.tmdb.feature.search.impl.store

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import pas.tmdb.core.common.AppResult
import pas.tmdb.data.tmdb.api.MovieRepository
import pas.tmdb.data.tmdb.api.model.Movie
import pas.tmdb.feature.search.api.MovieItemModel
import pas.tmdb.feature.search.api.SearchIntent

interface SearchStore : Store<SearchIntent, SearchState, SearchLabel>

class SearchStoreFactory(
    private val storeFactory: StoreFactory,
    private val movieRepository: MovieRepository,
) {
    fun create(): SearchStore =
        object : SearchStore, Store<SearchIntent, SearchState, SearchLabel> by storeFactory.create(
            name = "SearchStore",
            initialState = SearchState(),
            executorFactory = { ExecutorImpl(movieRepository) },
            reducer = ReducerImpl,
        ) {}

    private class ExecutorImpl(
        private val movieRepository: MovieRepository,
    ) : CoroutineExecutor<SearchIntent, Nothing, SearchState, Message, SearchLabel>() {

        private var searchJob: Job? = null

        override fun executeIntent(intent: SearchIntent) {
            when (intent) {
                is SearchIntent.QueryChanged -> onQueryChanged(intent.query)
                SearchIntent.Search -> search(state().query)
                SearchIntent.Clear -> onClear()
                SearchIntent.LoadMore -> loadMore()
                is SearchIntent.OnMovieClick -> publish(SearchLabel.NavigateToDetails(intent.movieId))
            }
        }

        private fun onQueryChanged(query: String) {
            dispatch(Message.QueryChanged(query))
            searchJob?.cancel()

            if (query.isBlank()) {
                dispatch(Message.Cleared)
                return
            }

            searchJob = scope.launch {
                delay(DEBOUNCE_MILLIS)
                search(query)
            }
        }

        private fun onClear() {
            searchJob?.cancel()
            dispatch(Message.QueryChanged(""))
            dispatch(Message.Cleared)
        }

        private fun search(query: String) {
            if (query.isBlank()) return
            searchJob?.cancel()
            searchJob = scope.launch {
                loadPage(query = query, page = 1, append = false)
            }
        }

        private fun loadMore() {
            val currentState = state()
            if (currentState.isLoadingMore ||
                currentState.isLoading ||
                currentState.endReached ||
                currentState.query.isBlank()
            ) {
                return
            }
            searchJob = scope.launch {
                loadPage(query = currentState.query, page = currentState.page + 1, append = true)
            }
        }

        private suspend fun loadPage(query: String, page: Int, append: Boolean) {
            if (append) {
                dispatch(Message.LoadingMoreChanged(isLoadingMore = true))
            } else {
                dispatch(Message.LoadingChanged(isLoading = true))
            }
            dispatch(Message.ErrorChanged(error = null))

            when (val result = movieRepository.searchMovies(query, page)) {
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
                    if (append) {
                        publish(SearchLabel.ShowError(result.message))
                    }
                }
            }

            if (append) {
                dispatch(Message.LoadingMoreChanged(isLoadingMore = false))
            } else {
                dispatch(Message.LoadingChanged(isLoading = false))
            }
        }
    }

    private object ReducerImpl : Reducer<SearchState, Message> {
        override fun SearchState.reduce(msg: Message): SearchState = when (msg) {
            is Message.QueryChanged -> copy(query = msg.query)
            is Message.LoadingChanged -> copy(isLoading = msg.isLoading, hasSearched = true)
            is Message.LoadingMoreChanged -> copy(isLoadingMore = msg.isLoadingMore)
            is Message.ErrorChanged -> copy(error = msg.error)
            is Message.Cleared -> copy(
                movies = emptyList(),
                error = null,
                page = 0,
                endReached = false,
                hasSearched = false,
                isLoading = false,
                isLoadingMore = false,
            )
            is Message.MoviesUpdated -> copy(
                movies = if (msg.append) (movies + msg.movies).distinctBy { it.id } else msg.movies,
                page = msg.page,
                endReached = msg.endReached,
            )
        }
    }

    private sealed interface Message {
        data class QueryChanged(val query: String) : Message
        data class LoadingChanged(val isLoading: Boolean) : Message
        data class LoadingMoreChanged(val isLoadingMore: Boolean) : Message
        data class ErrorChanged(val error: String?) : Message
        data object Cleared : Message
        data class MoviesUpdated(
            val movies: List<MovieItemModel>,
            val page: Int,
            val endReached: Boolean,
            val append: Boolean,
        ) : Message
    }

    private companion object {
        const val DEBOUNCE_MILLIS: Long = 400
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
