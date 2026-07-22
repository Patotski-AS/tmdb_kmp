package pas.tmdb.feature.main.api

import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.value.Value
import pas.tmdb.feature.details.api.DetailsComponent
import pas.tmdb.feature.login.api.LoginComponent
import pas.tmdb.feature.media.api.MediaComponent
import pas.tmdb.feature.person.api.PersonComponent

interface MainComponent {
    val childStack: Value<ChildStack<*, Child>>

    fun onBack()

    sealed interface Child {
        data class Media(val component: MediaComponent) : Child
        data class Profile(val component: LoginComponent) : Child
        data class Details(val component: DetailsComponent) : Child
        data class Person(val component: PersonComponent) : Child
    }
}
