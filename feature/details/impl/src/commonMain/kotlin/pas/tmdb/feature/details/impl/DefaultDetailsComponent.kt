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
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import pas.tmdb.feature.details.api.DetailsComponent
import pas.tmdb.feature.details.api.DetailsIntent
import pas.tmdb.feature.details.api.DetailsMediaType
import pas.tmdb.feature.details.api.DetailsModel
import pas.tmdb.feature.details.impl.store.DetailsLabel
import pas.tmdb.feature.details.impl.store.DetailsState
import pas.tmdb.feature.details.impl.store.DetailsStoreFactory

internal class DefaultDetailsComponent(
    componentContext: ComponentContext,
    mediaId: Int,
    mediaType: DetailsMediaType,
    storeFactory: DetailsStoreFactory,
    private val onBack: () -> Unit,
    private val onNavigateToLogin: () -> Unit,
    private val onNavigateToDetails: (Int, DetailsMediaType) -> Unit,
    private val onNavigateToPerson: (Int) -> Unit,
) : DetailsComponent, ComponentContext by componentContext {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private val store = storeFactory.create(mediaId = mediaId, mediaType = mediaType)

    private val _model = MutableValue(DetailsModel(isLoading = true))
    private val _externalUrlEvents = MutableSharedFlow<String>(extraBufferCapacity = 1)

    override val model: Value<DetailsModel> = _model
    override val externalUrlEvents: SharedFlow<String> = _externalUrlEvents.asSharedFlow()

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
                    DetailsLabel.NavigateToLogin -> onNavigateToLogin()
                    is DetailsLabel.NavigateToPerson -> onNavigateToPerson(label.personId)
                    is DetailsLabel.NavigateToDetails -> onNavigateToDetails(label.id, label.mediaType)
                    is DetailsLabel.ShowError -> Unit
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
        details.copy(
            isLoading = isLoading,
            error = error,
            sessionState = sessionState,
            isFavorite = isFavorite,
            isFavoriteLoading = isFavoriteLoading,
            selectedVideoKey = selectedVideoKey,
            imageGallery = imageGallery,
        )
    } else {
        DetailsModel(
            mediaType = mediaType,
            isLoading = isLoading,
            error = error,
            sessionState = sessionState,
            isFavorite = isFavorite,
            isFavoriteLoading = isFavoriteLoading,
            selectedVideoKey = selectedVideoKey,
            imageGallery = imageGallery,
        )
    }
