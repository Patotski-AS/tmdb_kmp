package pas.tmdb.feature.details.impl

import com.arkivanov.decompose.ComponentContext
import org.koin.dsl.module
import pas.tmdb.feature.details.api.DetailsComponent
import pas.tmdb.feature.details.impl.store.DetailsStoreFactory

val detailsFeatureModule = module {
    factory {
        DetailsStoreFactory(
            storeFactory = get(),
            movieRepository = get(),
            tvRepository = get(),
            accountRepository = get(),
            sessionRepository = get(),
        )
    }
    factory<DetailsComponent> { params ->
        DefaultDetailsComponent(
            componentContext = params.get(),
            mediaId = params.get(),
            mediaType = params.get(),
            storeFactory = get(),
            onBack = params.get(),
            onNavigateToLogin = params.get(),
            onNavigateToDetails = params.get(),
            onNavigateToPerson = params.get(),
        )
    }
}
