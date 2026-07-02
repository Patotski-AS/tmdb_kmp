package pas.tmdb.feature.main.impl

import com.arkivanov.decompose.ComponentContext
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module
import pas.tmdb.feature.details.api.DetailsComponent
import pas.tmdb.feature.discover.api.DiscoverComponent
import pas.tmdb.feature.login.api.LoginComponent
import pas.tmdb.feature.main.api.MainComponent
import pas.tmdb.feature.search.api.SearchComponent

val mainFeatureModule = module {
    factory<MainComponent> { (componentContext: ComponentContext) ->
        DefaultMainComponent(
            componentContext = componentContext,
            discoverComponentFactory = { childContext, onNavigateToDetails ->
                get<DiscoverComponent> { parametersOf(childContext, onNavigateToDetails) }
            },
            searchComponentFactory = { childContext, onNavigateToDetails ->
                get<SearchComponent> { parametersOf(childContext, onNavigateToDetails) }
            },
            loginComponentFactory = { childContext ->
                get<LoginComponent> { parametersOf(childContext) }
            },
            detailsComponentFactory = { childContext, movieId, onBack, onNavigateToLogin ->
                get<DetailsComponent> { parametersOf(childContext, movieId, onBack, onNavigateToLogin) }
            },
        )
    }
}
