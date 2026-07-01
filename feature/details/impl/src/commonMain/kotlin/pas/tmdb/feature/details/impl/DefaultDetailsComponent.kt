package pas.tmdb.feature.details.impl

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
import pas.tmdb.feature.details.api.DetailsComponent
import pas.tmdb.feature.details.api.DetailsIntent
import pas.tmdb.feature.details.api.DetailsModel
import pas.tmdb.feature.details.impl.store.DetailsLabel
import pas.tmdb.feature.details.impl.store.DetailsState
import pas.tmdb.feature.details.impl.store.DetailsStoreFactory

internal class DefaultDetailsComponent(
    componentContext: ComponentContext,
    movieId: Int,
    storeFactory: DetailsStoreFactory,
    private val onBack: () -> Unit,
) : DetailsComponent, ComponentContext by componentContext {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private val store = storeFactory.create(movieId)

    private val _model = MutableValue(DetailsModel(isLoading = true))

    override val model: Value<DetailsModel> = _model

    init {
        scope.launch {
            store.states.collect { state ->
                _model.value = state.toModel()
            }
        }
        scope.launch {
            store.labels.collect { label ->
                when (label) {
                    DetailsLabel.NavigateBack -> onBack()
                }
            }
        }
        lifecycle.doOnDestroy {
            store.dispose()
            scope.cancel()
        }
    }

    override fun onIntent(intent: DetailsIntent) {
        store.accept(intent)
    }
}

private fun DetailsState.toModel(): DetailsModel =
    if (details.title != null) {
        details.copy(isLoading = isLoading, error = error)
    } else {
        DetailsModel(
            isLoading = isLoading,
            error = error,
        )
    }
