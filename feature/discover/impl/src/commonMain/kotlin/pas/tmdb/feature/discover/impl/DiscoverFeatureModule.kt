package pas.tmdb.feature.discover.impl

import com.arkivanov.decompose.ComponentContext
import org.koin.dsl.module
import pas.tmdb.feature.discover.api.DiscoverComponent

val discoverFeatureModule = module {
    factory<DiscoverComponent> { (componentContext: ComponentContext) ->
        DefaultDiscoverComponent(componentContext)
    }
}
