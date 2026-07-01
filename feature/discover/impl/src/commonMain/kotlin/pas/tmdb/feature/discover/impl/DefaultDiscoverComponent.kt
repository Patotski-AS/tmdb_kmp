package pas.tmdb.feature.discover.impl

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
import pas.tmdb.feature.discover.api.DiscoverComponent
import pas.tmdb.feature.discover.api.DiscoverIntent
import pas.tmdb.feature.discover.api.DiscoverModel
import pas.tmdb.feature.discover.impl.store.DiscoverLabel
import pas.tmdb.feature.discover.impl.store.DiscoverState
import pas.tmdb.feature.discover.impl.store.DiscoverStoreFactory

internal class DefaultDiscoverComponent(
    componentContext: ComponentContext,
    storeFactory: DiscoverStoreFactory,
    private val onNavigateToDetails: (Int) -> Unit,
) : DiscoverComponent, ComponentContext by componentContext {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private val store = storeFactory.create()

    private val _model = MutableValue(DiscoverModel())

    override val model: Value<DiscoverModel> = _model

    init {
        scope.launch {
            store.states.collect { state ->
                _model.value = state.toModel()
            }
        }
        scope.launch {
            store.labels.collect { label ->
                when (label) {
                    is DiscoverLabel.NavigateToDetails -> onNavigateToDetails(label.movieId)
                    is DiscoverLabel.ShowError -> Unit
                }
            }
        }
        lifecycle.doOnDestroy {
            store.dispose()
            scope.cancel()
        }
    }

    override fun onIntent(intent: DiscoverIntent) {
        store.accept(intent)
    }
}

private fun DiscoverState.toModel(): DiscoverModel =
    DiscoverModel(
        movies = movies,
        isLoading = isLoading,
        isRefreshing = isRefreshing,
        isLoadingMore = isLoadingMore,
        error = error,
        endReached = endReached,
        sessionState = sessionState,
    )
