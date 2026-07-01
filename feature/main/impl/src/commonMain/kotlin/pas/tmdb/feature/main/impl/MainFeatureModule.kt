package pas.tmdb.feature.main.impl

import com.arkivanov.decompose.ComponentContext
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module
import pas.tmdb.feature.discover.api.DiscoverComponent
import pas.tmdb.feature.main.api.MainComponent

val mainFeatureModule = module {
    factory<MainComponent> { (componentContext: ComponentContext) ->
        DefaultMainComponent(
            componentContext = componentContext,
            discoverComponentFactory = { childContext ->
                get<DiscoverComponent> { parametersOf(childContext) }
            },
        )
    }
}
