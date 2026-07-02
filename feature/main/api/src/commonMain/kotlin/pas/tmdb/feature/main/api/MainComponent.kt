package pas.tmdb.feature.main.api

import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.value.Value
import pas.tmdb.feature.details.api.DetailsComponent
import pas.tmdb.feature.discover.api.DiscoverComponent
import pas.tmdb.feature.login.api.LoginComponent
import pas.tmdb.feature.main.api.MainComponent
import pas.tmdb.feature.search.api.SearchComponent

interface MainComponent {
    val childStack: Value<ChildStack<*, Child>>
    val selectedTab: Value<Tab>

    fun onTabSelected(tab: Tab)

    enum class Tab {
        Discover,
        Search,
        Profile,
    }

    sealed interface Child {
        data class Discover(val component: DiscoverComponent) : Child
        data class Search(val component: SearchComponent) : Child
        data class Profile(val component: LoginComponent) : Child
        data class Details(val component: DetailsComponent) : Child
    }
}
