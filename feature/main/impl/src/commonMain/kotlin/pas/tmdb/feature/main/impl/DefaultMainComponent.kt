package pas.tmdb.feature.main.impl

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.bringToFront
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.push
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import kotlinx.serialization.Serializable
import pas.tmdb.feature.details.api.DetailsComponent
import pas.tmdb.feature.discover.api.DiscoverComponent
import pas.tmdb.feature.main.api.MainComponent
import pas.tmdb.feature.search.api.SearchComponent

class DefaultMainComponent(
    componentContext: ComponentContext,
    private val discoverComponentFactory: (ComponentContext, (Int) -> Unit) -> DiscoverComponent,
    private val searchComponentFactory: (ComponentContext, (Int) -> Unit) -> SearchComponent,
    private val detailsComponentFactory: (ComponentContext, Int, () -> Unit) -> DetailsComponent,
) : MainComponent, ComponentContext by componentContext {

    private val navigation = StackNavigation<Config>()

    private val _selectedTab = MutableValue(MainComponent.Tab.Discover)

    override val selectedTab: Value<MainComponent.Tab> = _selectedTab

    override val childStack: Value<ChildStack<*, MainComponent.Child>> =
        childStack(
            source = navigation,
            serializer = Config.serializer(),
            initialConfiguration = Config.Discover,
            handleBackButton = true,
            childFactory = ::child,
        )

    override fun onTabSelected(tab: MainComponent.Tab) {
        val config = tab.toConfig()
        navigation.bringToFront(config)
        _selectedTab.value = tab
    }

    private fun child(config: Config, childContext: ComponentContext): MainComponent.Child =
        when (config) {
            Config.Discover -> MainComponent.Child.Discover(
                discoverComponentFactory(childContext) { movieId ->
                    onMovieClick(movieId)
                },
            )

            Config.Search -> MainComponent.Child.Search(
                searchComponentFactory(childContext) { movieId ->
                    onMovieClick(movieId)
                },
            )

            is Config.Details -> MainComponent.Child.Details(
                detailsComponentFactory(childContext, config.movieId) {
                    navigation.pop()
                },
            )
        }

    private fun onMovieClick(movieId: Int) {
        navigation.push(Config.Details(movieId))
    }

    @Serializable
    private sealed interface Config {
        @Serializable
        data object Discover : Config

        @Serializable
        data object Search : Config

        @Serializable
        data class Details(val movieId: Int) : Config
    }

    private fun MainComponent.Tab.toConfig(): Config =
        when (this) {
            MainComponent.Tab.Discover -> Config.Discover
            MainComponent.Tab.Search -> Config.Search
        }
}
