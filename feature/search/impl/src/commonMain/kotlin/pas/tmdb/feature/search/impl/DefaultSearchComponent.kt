package pas.tmdb.feature.search.impl

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
import pas.tmdb.feature.search.api.SearchComponent
import pas.tmdb.feature.search.api.SearchIntent
import pas.tmdb.feature.search.api.SearchModel
import pas.tmdb.feature.search.impl.store.SearchLabel
import pas.tmdb.feature.search.impl.store.SearchState
import pas.tmdb.feature.search.impl.store.SearchStoreFactory

internal class DefaultSearchComponent(
    componentContext: ComponentContext,
    storeFactory: SearchStoreFactory,
    private val onNavigateToDetails: (Int) -> Unit,
) : SearchComponent, ComponentContext by componentContext {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private val store = storeFactory.create()

    private val _model = MutableValue(SearchModel())

    override val model: Value<SearchModel> = _model

    init {
        scope.launch {
            store.states.collect { state ->
                _model.value = state.toModel()
            }
        }
        scope.launch {
            store.labels.collect { label ->
                when (label) {
                    is SearchLabel.NavigateToDetails -> onNavigateToDetails(label.movieId)
                    is SearchLabel.ShowError -> Unit
                }
            }
        }
        lifecycle.doOnDestroy {
            store.dispose()
            scope.cancel()
        }
    }

    override fun onIntent(intent: SearchIntent) {
        store.accept(intent)
    }
}

private fun SearchState.toModel(): SearchModel =
    SearchModel(
        query = query,
        movies = movies,
        isLoading = isLoading,
        isLoadingMore = isLoadingMore,
        error = error,
        endReached = endReached,
        hasSearched = hasSearched,
    )
