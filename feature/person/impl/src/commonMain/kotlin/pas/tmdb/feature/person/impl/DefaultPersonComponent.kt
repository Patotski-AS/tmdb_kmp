package pas.tmdb.feature.person.impl

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
import pas.tmdb.feature.details.api.DetailsMediaType
import pas.tmdb.feature.person.api.PersonComponent
import pas.tmdb.feature.person.api.PersonIntent
import pas.tmdb.feature.person.api.PersonModel
import pas.tmdb.feature.person.impl.store.PersonLabel
import pas.tmdb.feature.person.impl.store.PersonState
import pas.tmdb.feature.person.impl.store.PersonStoreFactory

internal class DefaultPersonComponent(
    componentContext: ComponentContext,
    personId: Int,
    storeFactory: PersonStoreFactory,
    private val onBack: () -> Unit,
    private val onNavigateToDetails: (Int, DetailsMediaType) -> Unit,
) : PersonComponent, ComponentContext by componentContext {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private val store = storeFactory.create(personId = personId)

    private val _model = MutableValue(PersonModel(isLoading = true))

    override val model: Value<PersonModel> = _model

    init {
        scope.launch {
            store.states.collect { state ->
                _model.value = state.toModel()
            }
        }
        scope.launch {
            store.labels.collect { label ->
                when (label) {
                    PersonLabel.NavigateBack -> onBack()
                    is PersonLabel.NavigateToDetails ->
                        onNavigateToDetails(label.id, label.mediaType)
                    is PersonLabel.ShowError -> Unit
                }
            }
        }
        lifecycle.doOnDestroy {
            store.dispose()
            scope.cancel()
        }
    }

    override fun onIntent(intent: PersonIntent) {
        store.accept(intent)
    }
}

private fun PersonState.toModel(): PersonModel =
    PersonModel(
        name = name,
        biography = biography,
        profileUrl = profileUrl,
        knownForDepartment = knownForDepartment,
        birthday = birthday,
        deathday = deathday,
        placeOfBirth = placeOfBirth,
        alsoKnownAs = alsoKnownAs,
        knownFor = knownFor,
        castCredits = castCredits,
        crewCredits = crewCredits,
        showFullCastCredits = showFullCastCredits,
        showFullCrewCredits = showFullCrewCredits,
        isLoading = isLoading,
        error = error,
    )
