package pas.tmdb.feature.media.impl.store

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineBootstrapper
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import pas.tmdb.auth.api.SessionRepository
import pas.tmdb.core.common.AppResult
import pas.tmdb.data.tmdb.api.FavoriteMoviesCache
import pas.tmdb.data.tmdb.api.MovieRepository
import pas.tmdb.data.tmdb.api.TvRepository
import pas.tmdb.data.tmdb.api.model.Genre
import pas.tmdb.data.tmdb.api.model.Movie
import pas.tmdb.data.tmdb.api.model.PagedMovies
import pas.tmdb.data.tmdb.api.model.TvShow
import pas.tmdb.feature.media.api.ContentType
import pas.tmdb.feature.media.api.MediaFilters
import pas.tmdb.feature.media.api.MediaGenre
import pas.tmdb.feature.media.api.MediaIntent
import pas.tmdb.feature.media.api.MediaItemModel

interface MediaStore : Store<MediaIntent, MediaState, MediaLabel>

class MediaStoreFactory(
    private val storeFactory: StoreFactory,
    private val movieRepository: MovieRepository,
    private val tvRepository: TvRepository,
    private val favoriteMoviesCache: FavoriteMoviesCache,
    private val sessionRepository: SessionRepository,
) {
    fun create(): MediaStore =
        object : MediaStore, Store<MediaIntent, MediaState, MediaLabel> by storeFactory.create(
            name = "MediaStore",
            initialState = MediaState(),
            bootstrapper = BootstrapperImpl(favoriteMoviesCache, sessionRepository),
            executorFactory = { ExecutorImpl(movieRepository, tvRepository) },
            reducer = ReducerImpl,
        ) {}

    private class BootstrapperImpl(
        private val favoriteMoviesCache: FavoriteMoviesCache,
        private val sessionRepository: SessionRepository,
    ) : CoroutineBootstrapper<Action>() {
        override fun invoke() {
            dispatch(Action.LoadInitial)
            dispatch(Action.LoadGenres)
            scope.launch {
                favoriteMoviesCache.favoriteIds.collect { favoriteIds ->
                    dispatch(Action.FavoriteIdsChanged(favoriteIds))
                }
            }
            scope.launch {
                sessionRepository.sessionState.collect { sessionState ->
                    dispatch(Action.SessionChanged(sessionState))
                }
            }
        }
    }

    private class ExecutorImpl(
        private val movieRepository: MovieRepository,
        private val tvRepository: TvRepository,
    ) : CoroutineExecutor<MediaIntent, Action, MediaState, Message, MediaLabel>() {

        private var searchJob: Job? = null

        override fun executeAction(action: Action) {
            when (action) {
                Action.LoadInitial -> loadInitial()
                Action.LoadGenres -> loadGenres()
                is Action.FavoriteIdsChanged -> dispatch(Message.FavoriteIdsChanged(action.favoriteIds))
                is Action.SessionChanged -> dispatch(Message.SessionChanged(action.sessionState))
            }
        }

        override fun executeIntent(intent: MediaIntent) {
            when (intent) {
                MediaIntent.Load -> loadInitial(force = true)
                MediaIntent.LoadMore -> loadMore()
                is MediaIntent.OnItemClick -> publish(
                    MediaLabel.NavigateToDetails(
                        id = intent.id,
                        contentType = state().contentType,
                    ),
                )
                is MediaIntent.OnContentTypeSelected -> onContentTypeSelected(intent.contentType)
                is MediaIntent.QueryChanged -> onQueryChanged(intent.query)
                MediaIntent.ClearQuery -> onQueryChanged("")
                is MediaIntent.ApplyFilters -> onFiltersApplied(intent.filters)
                MediaIntent.ResetFilters -> onFiltersReset()
                MediaIntent.OnProfileClick -> publish(MediaLabel.NavigateToProfile)
            }
        }

        private fun onQueryChanged(query: String) {
            val previousQuery = state().searchQuery
            dispatch(Message.SearchQueryChanged(query))
            searchJob?.cancel()

            if (query != previousQuery) {
                dispatch(
                    Message.ItemsUpdated(
                        items = emptyList(),
                        page = 0,
                        endReached = false,
                        append = false,
                    ),
                )
            }

            if (query.isBlank()) {
                loadInitial(force = true)
                return
            }

            if (query != previousQuery) {
                dispatch(Message.LoadingChanged(isLoading = true))
                dispatch(Message.ErrorChanged(error = null))
            }

            searchJob = scope.launch {
                delay(DEBOUNCE_MILLIS)
                loadInitial(force = true)
            }
        }

        private fun onFiltersApplied(filters: MediaFilters) {
            dispatch(Message.FiltersChanged(filters))
            if (state().contentType != ContentType.Movies) return
            loadInitial(force = true)
        }

        private fun onFiltersReset() {
            dispatch(Message.FiltersChanged(MediaFilters()))
            if (state().contentType != ContentType.Movies) return
            loadInitial(force = true)
        }

        private fun loadGenres() {
            scope.launch {
                when (val result = movieRepository.getMovieGenres()) {
                    is AppResult.Success ->
                        dispatch(Message.GenresLoaded(result.value.map(Genre::toMediaGenre)))
                    is AppResult.Error -> Unit
                }
            }
        }

        private fun onContentTypeSelected(contentType: ContentType) {
            if (state().contentType == contentType) return
            searchJob?.cancel()
            dispatch(
                Message.ContentTypeChanged(
                    contentType = contentType,
                    searchQuery = "",
                    filters = MediaFilters(),
                    items = emptyList(),
                    page = 0,
                    endReached = contentType == ContentType.People,
                    error = null,
                ),
            )
            when (contentType) {
                ContentType.Movies,
                ContentType.Tv,
                -> loadInitial(force = true)

                ContentType.People -> Unit
            }
        }

        private fun loadInitial(force: Boolean = false) {
            val currentState = state()
            if (currentState.contentType == ContentType.People) return
            if (!force && (currentState.items.isNotEmpty() || currentState.isLoading)) return
            loadPage(page = 1, append = false)
        }

        private fun loadMore() {
            val currentState = state()
            if (currentState.contentType == ContentType.People) return
            if (currentState.isLoadingMore || currentState.endReached || currentState.isLoading) return
            loadPage(page = currentState.page + 1, append = true)
        }

        private fun loadPage(page: Int, append: Boolean) {
            scope.launch {
                when {
                    append -> dispatch(Message.LoadingMoreChanged(isLoadingMore = true))
                    else -> dispatch(Message.LoadingChanged(isLoading = true))
                }
                dispatch(Message.ErrorChanged(error = null))

                val currentState = state()
                when (currentState.contentType) {
                    ContentType.Movies -> loadMoviesPage(page, append, currentState)
                    ContentType.Tv -> loadTvPage(page, append, currentState)
                    ContentType.People -> Unit
                }

                when {
                    append -> dispatch(Message.LoadingMoreChanged(isLoadingMore = false))
                    else -> dispatch(Message.LoadingChanged(isLoading = false))
                }
            }
        }

        private suspend fun loadMoviesPage(page: Int, append: Boolean, currentState: MediaState) {
            val query = currentState.searchQuery.trim()
            val filters = currentState.filters

            val result = when {
                query.isNotEmpty() && filters.isActive ->
                    movieRepository.discoverMovies(
                        genreId = filters.genreId,
                        sortBy = filters.sortBy,
                        page = page,
                    ).filterMoviesByTitle(query)

                query.isNotEmpty() ->
                    movieRepository.searchMovies(query, page)

                filters.isActive ->
                    movieRepository.discoverMovies(
                        genreId = filters.genreId,
                        sortBy = filters.sortBy,
                        page = page,
                    )

                else -> movieRepository.getPopularMovies(page)
            }

            when (result) {
                is AppResult.Success -> {
                    val paged = result.value
                    dispatch(
                        Message.ItemsUpdated(
                            items = paged.movies.map(Movie::toItemModel),
                            page = paged.page,
                            endReached = paged.page >= paged.totalPages,
                            append = append,
                        ),
                    )
                }

                is AppResult.Error -> {
                    dispatch(Message.ErrorChanged(error = result.message))
                    if (append) {
                        publish(MediaLabel.ShowError(result.message))
                    }
                }
            }
        }

        private suspend fun loadTvPage(page: Int, append: Boolean, currentState: MediaState) {
            val query = currentState.searchQuery.trim()
            val result = if (query.isNotEmpty()) {
                tvRepository.searchTvShows(query, page)
            } else {
                tvRepository.getPopularTvShows(page)
            }

            when (result) {
                is AppResult.Success -> {
                    val paged = result.value
                    dispatch(
                        Message.ItemsUpdated(
                            items = paged.shows.map(TvShow::toItemModel),
                            page = paged.page,
                            endReached = paged.page >= paged.totalPages,
                            append = append,
                        ),
                    )
                }

                is AppResult.Error -> {
                    dispatch(Message.ErrorChanged(error = result.message))
                    if (append) {
                        publish(MediaLabel.ShowError(result.message))
                    }
                }
            }
        }
    }

    private object ReducerImpl : Reducer<MediaState, Message> {
        override fun MediaState.reduce(msg: Message): MediaState = when (msg) {
            is Message.LoadingChanged -> copy(isLoading = msg.isLoading)
            is Message.LoadingMoreChanged -> copy(isLoadingMore = msg.isLoadingMore)
            is Message.ErrorChanged -> copy(error = msg.error)
            is Message.SearchQueryChanged -> copy(searchQuery = msg.query)
            is Message.FiltersChanged -> copy(filters = msg.filters)
            is Message.GenresLoaded -> copy(genres = msg.genres)
            is Message.FavoriteIdsChanged -> copy(favoriteIds = msg.favoriteIds)
            is Message.SessionChanged -> copy(sessionState = msg.sessionState)
            is Message.ContentTypeChanged -> copy(
                contentType = msg.contentType,
                searchQuery = msg.searchQuery,
                filters = msg.filters,
                items = msg.items,
                page = msg.page,
                endReached = msg.endReached,
                error = msg.error,
                isLoading = false,
                isLoadingMore = false,
            )
            is Message.ItemsUpdated -> copy(
                items = if (msg.append) (items + msg.items).distinctBy { it.id } else msg.items,
                page = msg.page,
                endReached = msg.endReached,
            )
        }
    }

    private sealed interface Action {
        data object LoadInitial : Action
        data object LoadGenres : Action
        data class FavoriteIdsChanged(val favoriteIds: Set<Int>) : Action
        data class SessionChanged(val sessionState: pas.tmdb.auth.api.SessionState) : Action
    }

    private sealed interface Message {
        data class LoadingChanged(val isLoading: Boolean) : Message
        data class LoadingMoreChanged(val isLoadingMore: Boolean) : Message
        data class ErrorChanged(val error: String?) : Message
        data class SearchQueryChanged(val query: String) : Message
        data class FiltersChanged(val filters: MediaFilters) : Message
        data class GenresLoaded(val genres: List<MediaGenre>) : Message
        data class FavoriteIdsChanged(val favoriteIds: Set<Int>) : Message
        data class SessionChanged(val sessionState: pas.tmdb.auth.api.SessionState) : Message
        data class ContentTypeChanged(
            val contentType: ContentType,
            val searchQuery: String,
            val filters: MediaFilters,
            val items: List<MediaItemModel>,
            val page: Int,
            val endReached: Boolean,
            val error: String?,
        ) : Message
        data class ItemsUpdated(
            val items: List<MediaItemModel>,
            val page: Int,
            val endReached: Boolean,
            val append: Boolean,
        ) : Message
    }

    private companion object {
        const val DEBOUNCE_MILLIS: Long = 400
    }
}

private fun AppResult<PagedMovies>.filterMoviesByTitle(query: String): AppResult<PagedMovies> =
    when (this) {
        is AppResult.Success -> {
            val normalizedQuery = query.lowercase()
            val filtered = value.movies.filter { movie ->
                movie.title.lowercase().contains(normalizedQuery)
            }
            AppResult.Success(
                value.copy(
                    movies = filtered,
                    totalResults = filtered.size,
                ),
            )
        }

        is AppResult.Error -> this
    }

private fun Genre.toMediaGenre(): MediaGenre =
    MediaGenre(id = id, name = name)

private fun Movie.toItemModel(): MediaItemModel =
    MediaItemModel(
        id = id,
        title = title,
        posterUrl = posterUrl,
        voteAverage = voteAverage,
    )

private fun TvShow.toItemModel(): MediaItemModel =
    MediaItemModel(
        id = id,
        title = name,
        posterUrl = posterUrl,
        voteAverage = voteAverage,
    )
