package pas.tmdb.feature.person.impl.store

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineBootstrapper
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import kotlinx.coroutines.launch
import pas.tmdb.core.common.AppResult
import pas.tmdb.data.tmdb.api.PersonRepository
import pas.tmdb.data.tmdb.api.model.PersonCredit
import pas.tmdb.data.tmdb.api.model.PersonCreditMediaType
import pas.tmdb.data.tmdb.api.model.PersonDetails
import pas.tmdb.data.tmdb.api.model.toDepartmentLabel
import pas.tmdb.feature.details.api.DetailsMediaType
import pas.tmdb.feature.person.api.PersonCreditModel
import pas.tmdb.feature.person.api.PersonIntent

interface PersonStore : Store<PersonIntent, PersonState, PersonLabel>

class PersonStoreFactory(
    private val storeFactory: StoreFactory,
    private val personRepository: PersonRepository,
) {
    fun create(personId: Int): PersonStore =
        object : PersonStore, Store<PersonIntent, PersonState, PersonLabel> by storeFactory.create(
            name = "PersonStore",
            initialState = PersonState(personId = personId, isLoading = true),
            bootstrapper = BootstrapperImpl(),
            executorFactory = { ExecutorImpl(personRepository) },
            reducer = ReducerImpl,
        ) {}

    private class BootstrapperImpl : CoroutineBootstrapper<Action>() {
        override fun invoke() {
            dispatch(Action.Load)
        }
    }

    private class ExecutorImpl(
        private val personRepository: PersonRepository,
    ) : CoroutineExecutor<PersonIntent, Action, PersonState, Message, PersonLabel>() {

        override fun executeAction(action: Action) {
            when (action) {
                Action.Load -> load()
            }
        }

        override fun executeIntent(intent: PersonIntent) {
            when (intent) {
                PersonIntent.Load,
                PersonIntent.Retry,
                -> load()

                PersonIntent.Back -> publish(PersonLabel.NavigateBack)
                PersonIntent.ShowFullCastCredits -> dispatch(Message.ShowFullCastCredits)
                PersonIntent.ShowFullCrewCredits -> dispatch(Message.ShowFullCrewCredits)
                is PersonIntent.OnCreditClick -> publish(
                    PersonLabel.NavigateToDetails(
                        id = intent.id,
                        mediaType = intent.mediaType,
                    ),
                )
            }
        }

        private fun load() {
            val personId = state().personId
            scope.launch {
                dispatch(Message.LoadingChanged(isLoading = true))
                dispatch(Message.ErrorChanged(error = null))

                when (val result = personRepository.getPersonDetails(personId)) {
                    is AppResult.Success -> dispatch(Message.DetailsLoaded(result.value))
                    is AppResult.Error -> {
                        dispatch(Message.ErrorChanged(error = result.message))
                        publish(PersonLabel.ShowError(result.message))
                    }
                }

                dispatch(Message.LoadingChanged(isLoading = false))
            }
        }
    }

    private object ReducerImpl : Reducer<PersonState, Message> {
        override fun PersonState.reduce(msg: Message): PersonState = when (msg) {
            is Message.LoadingChanged -> copy(isLoading = msg.isLoading)
            is Message.ErrorChanged -> copy(error = msg.error)
            Message.ShowFullCastCredits -> copy(showFullCastCredits = true)
            Message.ShowFullCrewCredits -> copy(showFullCrewCredits = true)
            is Message.DetailsLoaded -> {
                val details = msg.details
                copy(
                    name = details.name,
                    biography = details.biography,
                    profileUrl = details.profileUrl,
                    knownForDepartment = details.knownForDepartment?.toDepartmentLabel(),
                    birthday = details.birthday,
                    deathday = details.deathday,
                    placeOfBirth = details.placeOfBirth,
                    alsoKnownAs = details.alsoKnownAs,
                    knownFor = details.knownFor.map(PersonCredit::toModel),
                    castCredits = details.castCredits.map(PersonCredit::toModel),
                    crewCredits = details.crewCredits.map(PersonCredit::toModel),
                    error = null,
                )
            }
        }
    }

    private sealed interface Action {
        data object Load : Action
    }

    private sealed interface Message {
        data class LoadingChanged(val isLoading: Boolean) : Message
        data class ErrorChanged(val error: String?) : Message
        data class DetailsLoaded(val details: PersonDetails) : Message
        data object ShowFullCastCredits : Message
        data object ShowFullCrewCredits : Message
    }
}

private fun PersonCredit.toModel(): PersonCreditModel =
    PersonCreditModel(
        id = id,
        title = title,
        posterUrl = posterUrl,
        mediaType = mediaType.toDetailsMediaType(),
        subtitle = characterOrJob,
        voteAverage = voteAverage.takeIf { it > 0 },
    )

private fun PersonCreditMediaType.toDetailsMediaType(): DetailsMediaType =
    when (this) {
        PersonCreditMediaType.Movie -> DetailsMediaType.Movie
        PersonCreditMediaType.Tv -> DetailsMediaType.Tv
    }
