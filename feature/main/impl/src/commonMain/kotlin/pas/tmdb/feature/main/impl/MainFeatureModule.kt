package pas.tmdb.feature.main.impl

import com.arkivanov.decompose.ComponentContext
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module
import pas.tmdb.feature.details.api.DetailsComponent
import pas.tmdb.feature.login.api.LoginComponent
import pas.tmdb.feature.main.api.MainComponent
import pas.tmdb.feature.media.api.MediaComponent

val mainFeatureModule = module {
    factory<MainComponent> { (componentContext: ComponentContext) ->
        DefaultMainComponent(
            componentContext = componentContext,
            mediaComponentFactory = { childContext, onNavigateToDetails, onNavigateToProfile ->
                get<MediaComponent> {
                    parametersOf(childContext, onNavigateToDetails, onNavigateToProfile)
                }
            },
            loginComponentFactory = { childContext, onNavigateToDetails ->
                get<LoginComponent> { parametersOf(childContext, onNavigateToDetails) }
            },
            detailsComponentFactory = { childContext, movieId, onBack, onNavigateToLogin ->
                get<DetailsComponent> { parametersOf(childContext, movieId, onBack, onNavigateToLogin) }
            },
        )
    }
}
