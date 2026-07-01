package pas.tmdb.feature.main.api

import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.value.Value
import pas.tmdb.feature.discover.api.DiscoverComponent

interface MainComponent {
    val childStack: Value<ChildStack<*, Child>>

    sealed interface Child {
        data class Discover(val component: DiscoverComponent) : Child
    }
}
