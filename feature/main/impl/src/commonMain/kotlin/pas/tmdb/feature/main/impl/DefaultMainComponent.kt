package pas.tmdb.feature.main.impl

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.value.Value
import kotlinx.serialization.Serializable
import pas.tmdb.feature.discover.api.DiscoverComponent
import pas.tmdb.feature.main.api.MainComponent

class DefaultMainComponent(
    componentContext: ComponentContext,
    private val discoverComponentFactory: (ComponentContext) -> DiscoverComponent,
) : MainComponent, ComponentContext by componentContext {

    private val navigation = StackNavigation<Config>()

    override val childStack: Value<ChildStack<*, MainComponent.Child>> =
        childStack(
            source = navigation,
            serializer = Config.serializer(),
            initialConfiguration = Config.Discover,
            handleBackButton = true,
            childFactory = ::child,
        )

    private fun child(config: Config, childContext: ComponentContext): MainComponent.Child =
        when (config) {
            Config.Discover -> MainComponent.Child.Discover(discoverComponentFactory(childContext))
        }

    @Serializable
    private sealed interface Config {
        @Serializable
        data object Discover : Config
    }
}
