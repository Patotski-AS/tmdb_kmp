package pas.tmdb.feature.main.impl

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.bringToFront
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.value.Value
import kotlinx.serialization.Serializable
import pas.tmdb.feature.details.api.DetailsComponent
import pas.tmdb.feature.details.api.DetailsMediaType
import pas.tmdb.feature.login.api.LoginComponent
import pas.tmdb.feature.main.api.MainComponent
import pas.tmdb.feature.media.api.ContentType
import pas.tmdb.feature.media.api.MediaComponent
import pas.tmdb.feature.person.api.PersonComponent

class DefaultMainComponent(
    componentContext: ComponentContext,
    private val mediaComponentFactory: (
        ComponentContext,
        (Int, ContentType) -> Unit,
        () -> Unit,
    ) -> MediaComponent,
    private val loginComponentFactory: (
        ComponentContext,
        (Int) -> Unit,
    ) -> LoginComponent,
    private val detailsComponentFactory: (
        ComponentContext,
        Int,
        DetailsMediaType,
        () -> Unit,
        () -> Unit,
        (Int, DetailsMediaType) -> Unit,
        (Int) -> Unit,
    ) -> DetailsComponent,
    private val personComponentFactory: (
        ComponentContext,
        Int,
        () -> Unit,
        (Int, DetailsMediaType) -> Unit,
    ) -> PersonComponent,
) : MainComponent, ComponentContext by componentContext {

    private val navigation = StackNavigation<Config>()

    override val childStack: Value<ChildStack<*, MainComponent.Child>> =
        childStack(
            source = navigation,
            serializer = Config.serializer(),
            initialConfiguration = Config.Media,
            handleBackButton = true,
            childFactory = ::child,
        )

    override fun onBack() {
        navigation.pop()
    }

    private fun child(config: Config, childContext: ComponentContext): MainComponent.Child =
        when (config) {
            Config.Media -> MainComponent.Child.Media(
                mediaComponentFactory(
                    childContext,
                    { id, contentType ->
                        when (contentType) {
                            ContentType.Movies -> onDetailsClick(id, DetailsMediaType.Movie)
                            ContentType.Tv -> onDetailsClick(id, DetailsMediaType.Tv)
                            ContentType.People -> onPersonClick(id)
                        }
                    },
                    { navigation.bringToFront(Config.Profile) },
                ),
            )

            Config.Profile -> MainComponent.Child.Profile(
                loginComponentFactory(
                    childContext,
                    { movieId -> onDetailsClick(movieId, DetailsMediaType.Movie) },
                ),
            )

            is Config.Details -> MainComponent.Child.Details(
                detailsComponentFactory(
                    childContext,
                    config.mediaId,
                    config.mediaType,
                    { navigation.pop() },
                    { onNavigateToLogin() },
                    { id, mediaType -> onDetailsClick(id, mediaType) },
                    { personId -> onPersonClick(personId) },
                ),
            )

            is Config.Person -> MainComponent.Child.Person(
                personComponentFactory(
                    childContext,
                    config.personId,
                    { navigation.pop() },
                    { id, mediaType -> onDetailsClick(id, mediaType) },
                ),
            )
        }

    private fun onDetailsClick(mediaId: Int, mediaType: DetailsMediaType) {
        navigation.bringToFront(Config.Details(mediaId = mediaId, mediaType = mediaType))
    }

    private fun onPersonClick(personId: Int) {
        navigation.bringToFront(Config.Person(personId = personId))
    }

    private fun onNavigateToLogin() {
        navigation.bringToFront(Config.Profile)
    }

    @Serializable
    private sealed interface Config {
        @Serializable
        data object Media : Config

        @Serializable
        data object Profile : Config

        @Serializable
        data class Details(
            val mediaId: Int,
            val mediaType: DetailsMediaType = DetailsMediaType.Movie,
        ) : Config

        @Serializable
        data class Person(
            val personId: Int,
        ) : Config
    }
}
