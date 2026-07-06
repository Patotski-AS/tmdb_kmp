package pas.tmdb.feature.media.impl

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.states
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import pas.tmdb.feature.media.api.MediaComponent
import pas.tmdb.feature.media.api.MediaIntent
import pas.tmdb.feature.media.api.MediaModel
import pas.tmdb.feature.media.impl.store.MediaLabel
import pas.tmdb.feature.media.impl.store.MediaState
import pas.tmdb.feature.media.impl.store.MediaStoreFactory

internal class DefaultMediaComponent(
    componentContext: ComponentContext,
    storeFactory: MediaStoreFactory,
    private val onNavigateToDetails: (Int) -> Unit,
    private val onNavigateToProfile: () -> Unit,
) : MediaComponent, ComponentContext by componentContext {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private val store = storeFactory.create()

    private val _model = MutableValue(MediaModel())

    override val model: Value<MediaModel> = _model

    init {
        scope.launch {
            store.states.collect { state ->
                _model.value = state.toModel()
            }
        }
        scope.launch {
            store.labels.collect { label ->
                when (label) {
                    is MediaLabel.NavigateToDetails -> onNavigateToDetails(label.movieId)
                    MediaLabel.NavigateToProfile -> onNavigateToProfile()
                    is MediaLabel.ShowError -> Unit
                }
            }
        }
        lifecycle.doOnDestroy {
            store.dispose()
            scope.cancel()
        }
    }

    override fun onIntent(intent: MediaIntent) {
        store.accept(intent)
    }
}

private fun MediaState.toModel(): MediaModel =
    MediaModel(
        contentType = contentType,
        searchQuery = searchQuery,
        filters = filters,
        genres = genres,
        items = items,
        isLoading = isLoading,
        isLoadingMore = isLoadingMore,
        error = error,
        endReached = endReached,
        favoriteIds = favoriteIds,
        sessionState = sessionState,
    )
