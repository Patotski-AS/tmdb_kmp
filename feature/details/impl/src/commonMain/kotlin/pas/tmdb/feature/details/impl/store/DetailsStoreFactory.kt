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
import pas.tmdb.data.tmdb.api.TvRepository
import pas.tmdb.data.tmdb.api.model.CastMember
import pas.tmdb.data.tmdb.api.model.CrewMember
import pas.tmdb.data.tmdb.api.model.Movie
import pas.tmdb.data.tmdb.api.model.MovieDetails
import pas.tmdb.data.tmdb.api.model.TvShow
import pas.tmdb.data.tmdb.api.model.TvShowDetails
import pas.tmdb.data.tmdb.api.model.Video
import pas.tmdb.feature.details.api.DetailsCastMember
import pas.tmdb.feature.details.api.DetailsCrewMember
import pas.tmdb.feature.details.api.DetailsImage
import pas.tmdb.feature.details.api.DetailsImageGallery
import pas.tmdb.feature.details.api.DetailsImageGallerySection
import pas.tmdb.feature.details.api.DetailsIntent
import pas.tmdb.feature.details.api.DetailsMediaType
import pas.tmdb.feature.details.api.DetailsModel
import pas.tmdb.feature.details.api.DetailsRecommendation
import pas.tmdb.feature.details.api.DetailsVideo

interface DetailsStore : Store<DetailsIntent, DetailsState, DetailsLabel>

class DetailsStoreFactory(
    private val storeFactory: StoreFactory,
    private val movieRepository: MovieRepository,
    private val tvRepository: TvRepository,
    private val accountRepository: AccountRepository,
    private val sessionRepository: SessionRepository,
) {
    fun create(mediaId: Int, mediaType: DetailsMediaType): DetailsStore =
        object : DetailsStore, Store<DetailsIntent, DetailsState, DetailsLabel> by storeFactory.create(
            name = "DetailsStore",
            initialState = DetailsState(mediaId = mediaId, mediaType = mediaType),
            bootstrapper = BootstrapperImpl(sessionRepository),
            executorFactory = {
                ExecutorImpl(
                    movieRepository = movieRepository,
                    tvRepository = tvRepository,
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
        private val tvRepository: TvRepository,
        private val accountRepository: AccountRepository,
        private val sessionRepository: SessionRepository,
    ) : CoroutineExecutor<DetailsIntent, Action, DetailsState, Message, DetailsLabel>() {

        override fun executeAction(action: Action) {
            when (action) {
                Action.LoadInitial -> loadDetails()
                is Action.SessionChanged -> {
                    dispatch(Message.SessionChanged(action.sessionState))
                    if (action.sessionState is SessionState.Authenticated &&
                        state().mediaType == DetailsMediaType.Movie
                    ) {
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

                is DetailsIntent.OnCastMemberClick -> publish(DetailsLabel.NavigateToPerson(intent.personId))

                is DetailsIntent.OnVideoClick -> dispatch(Message.VideoSelected(intent.videoKey))

                is DetailsIntent.OnImageClick -> dispatch(
                    Message.ImageGalleryChanged(
                        DetailsImageGallery(
                            section = intent.section,
                            index = intent.index,
                        ),
                    ),
                )

                is DetailsIntent.OnRecommendationClick -> publish(
                    DetailsLabel.NavigateToDetails(
                        id = intent.id,
                        mediaType = state().mediaType,
                    ),
                )

                DetailsIntent.ShowFullCast -> dispatch(Message.ShowFullCastChanged(showFullCast = true))

                DetailsIntent.DismissVideoPlayer -> dispatch(Message.VideoSelected(videoKey = null))

                DetailsIntent.DismissImageGallery -> dispatch(Message.ImageGalleryChanged(gallery = null))
            }
        }

        private fun loadDetails() {
            val currentState = state()
            if (currentState.isLoading) return

            scope.launch {
                dispatch(Message.LoadingChanged(isLoading = true))
                dispatch(Message.ErrorChanged(error = null))

                val result = when (currentState.mediaType) {
                    DetailsMediaType.Movie ->
                        movieRepository.getMovieDetails(currentState.mediaId).mapSuccess { it.toModel() }

                    DetailsMediaType.Tv ->
                        tvRepository.getTvShowDetails(currentState.mediaId).mapSuccess { it.toModel() }
                }

                when (result) {
                    is AppResult.Success -> {
                        dispatch(Message.DetailsLoaded(result.value))
                        if (currentState.mediaType == DetailsMediaType.Movie &&
                            sessionRepository.sessionState.value is SessionState.Authenticated
                        ) {
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
            if (state().mediaType != DetailsMediaType.Movie) return
            val mediaId = state().mediaId
            scope.launch {
                when (val result = accountRepository.isFavorite(mediaId)) {
                    is AppResult.Success -> dispatch(Message.FavoriteChanged(isFavorite = result.value))
                    is AppResult.Error -> Unit
                }
            }
        }

        private fun toggleFavorite() {
            val currentState = state()
            if (currentState.mediaType != DetailsMediaType.Movie) return
            if (currentState.sessionState !is SessionState.Authenticated) {
                publish(DetailsLabel.NavigateToLogin)
                return
            }
            if (currentState.isFavoriteLoading) return

            val newFavorite = !(currentState.isFavorite ?: false)
            scope.launch {
                dispatch(Message.FavoriteLoadingChanged(isFavoriteLoading = true))

                when (val result = accountRepository.setFavorite(currentState.mediaId, newFavorite)) {
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
            is Message.VideoSelected -> copy(selectedVideoKey = msg.videoKey)
            is Message.ImageGalleryChanged -> copy(imageGallery = msg.gallery)
            is Message.ShowFullCastChanged -> copy(
                details = details.copy(showFullCast = msg.showFullCast),
            )
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
        data class VideoSelected(val videoKey: String?) : Message
        data class ImageGalleryChanged(val gallery: DetailsImageGallery?) : Message
        data class ShowFullCastChanged(val showFullCast: Boolean) : Message
    }
}

private fun <T, R> AppResult<T>.mapSuccess(transform: (T) -> R): AppResult<R> =
    when (this) {
        is AppResult.Success -> AppResult.Success(transform(value))
        is AppResult.Error -> this
    }

private fun MovieDetails.toModel(): DetailsModel =
    DetailsModel(
        mediaType = DetailsMediaType.Movie,
        title = title,
        overview = overview,
        posterUrl = posterUrl,
        backdropUrl = backdropUrl,
        tagline = tagline,
        voteAverage = voteAverage,
        releaseDate = releaseDate,
        runtime = runtime,
        genres = genres,
        originalTitle = originalTitle,
        status = status,
        originalLanguage = originalLanguage,
        budget = budget.takeIf { it > 0 },
        revenue = revenue.takeIf { it > 0 },
        cast = cast.map(CastMember::toModel),
        crew = crew.toKeyCrew(),
        videos = videos.toDisplayVideos(),
        backdrops = backdrops.map { DetailsImage(url = it.url) },
        posters = posters.map { DetailsImage(url = it.url) },
        recommendations = recommendations.map(Movie::toRecommendation),
    )

private fun TvShowDetails.toModel(): DetailsModel =
    DetailsModel(
        mediaType = DetailsMediaType.Tv,
        title = name,
        overview = overview,
        posterUrl = posterUrl,
        backdropUrl = backdropUrl,
        tagline = tagline,
        voteAverage = voteAverage,
        releaseDate = firstAirDate,
        runtime = episodeRuntimeMinutes,
        genres = genres,
        originalTitle = originalName,
        status = status,
        originalLanguage = originalLanguage,
        numberOfSeasons = numberOfSeasons,
        numberOfEpisodes = numberOfEpisodes,
        cast = cast.map(CastMember::toModel),
        crew = createdBy.toCreatorsCrew() + crew.toKeyCrew(),
        videos = videos.toDisplayVideos(),
        backdrops = backdrops.map { DetailsImage(url = it.url) },
        posters = posters.map { DetailsImage(url = it.url) },
        recommendations = recommendations.map(TvShow::toRecommendation),
    )

private fun CastMember.toModel(): DetailsCastMember =
    DetailsCastMember(
        id = id,
        name = name,
        character = character,
        profileUrl = profileUrl,
    )

private fun List<String>.toCreatorsCrew(): List<DetailsCrewMember> =
    if (isEmpty()) {
        emptyList()
    } else {
        listOf(DetailsCrewMember(name = joinToString(", "), jobLabel = "Создатель"))
    }

private fun List<CrewMember>.toKeyCrew(): List<DetailsCrewMember> {
    val directors = filter { it.job == "Director" }.map { it.name }.distinct()
    val writers = filter { it.job in WRITER_JOBS }.map { it.name }.distinct()
    return buildList {
        if (directors.isNotEmpty()) {
            add(DetailsCrewMember(name = directors.joinToString(", "), jobLabel = "Режиссёр"))
        }
        if (writers.isNotEmpty()) {
            add(DetailsCrewMember(name = writers.joinToString(", "), jobLabel = "Сценарий"))
        }
    }
}

private fun List<Video>.toDisplayVideos(): List<DetailsVideo> {
    val priority = listOf("Trailer", "Teaser", "Clip")
    return sortedWith(
        compareBy<Video> { video ->
            val index = priority.indexOf(video.type)
            if (index == -1) priority.size else index
        }.thenByDescending { it.official },
    ).map { video ->
        DetailsVideo(
            key = video.key,
            name = video.name,
            typeLabel = video.type.toVideoTypeLabel(),
        )
    }
}

private fun String.toVideoTypeLabel(): String =
    when (this) {
        "Trailer" -> "Трейлер"
        "Teaser" -> "Тизер"
        "Clip" -> "Клип"
        else -> this
    }

private fun Movie.toRecommendation(): DetailsRecommendation =
    DetailsRecommendation(
        id = id,
        title = title,
        posterUrl = posterUrl,
        voteAverage = voteAverage,
    )

private fun TvShow.toRecommendation(): DetailsRecommendation =
    DetailsRecommendation(
        id = id,
        title = name,
        posterUrl = posterUrl,
        voteAverage = voteAverage,
    )

private val WRITER_JOBS = setOf("Screenplay", "Writer", "Story")
