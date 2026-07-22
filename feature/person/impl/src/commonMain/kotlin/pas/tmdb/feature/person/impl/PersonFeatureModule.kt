package pas.tmdb.feature.person.impl

import com.arkivanov.decompose.ComponentContext
import org.koin.dsl.module
import pas.tmdb.feature.details.api.DetailsMediaType
import pas.tmdb.feature.person.api.PersonComponent
import pas.tmdb.feature.person.impl.store.PersonStoreFactory

val personFeatureModule = module {
    factory {
        PersonStoreFactory(
            storeFactory = get(),
            personRepository = get(),
        )
    }
    factory<PersonComponent> { params ->
        DefaultPersonComponent(
            componentContext = params.get(),
            personId = params.get(),
            storeFactory = get(),
            onBack = params.get(),
            onNavigateToDetails = params.get<(Int, DetailsMediaType) -> Unit>(),
        )
    }
}
