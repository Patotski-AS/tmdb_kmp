package pas.tmdb.feature.login.impl

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.push
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.states
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import pas.tmdb.auth.api.SessionState
import pas.tmdb.feature.login.api.LoginChild
import pas.tmdb.feature.login.api.LoginComponent
import pas.tmdb.feature.login.api.LoginIntent
import pas.tmdb.feature.login.api.LoginModel
import pas.tmdb.feature.login.impl.store.LoginLabel
import pas.tmdb.feature.login.impl.store.LoginState
import pas.tmdb.feature.login.impl.store.LoginStoreFactory

internal class DefaultLoginComponent(
    componentContext: ComponentContext,
    storeFactory: LoginStoreFactory,
    private val onNavigateToDetails: (Int) -> Unit,
) : LoginComponent, ComponentContext by componentContext {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private val store = storeFactory.create()

    private val navigation = StackNavigation<ProfileChildConfig>()

    override val childStack: Value<ChildStack<*, LoginChild>> =
        childStack(
            source = navigation,
            serializer = ProfileChildConfig.serializer(),
            initialConfiguration = ProfileChildConfig.Menu,
            handleBackButton = false,
            childFactory = ::child,
        )

    private val _model = MutableValue(LoginModel())

    override val model: Value<LoginModel> = _model

    private val _browserUrl = MutableSharedFlow<String>(extraBufferCapacity = 1)
    override val browserUrlEvents: SharedFlow<String> = _browserUrl.asSharedFlow()

    init {
        scope.launch {
            store.states.collect { state ->
                _model.value = state.toModel()
                if (state.sessionState is SessionState.Guest && childStack.value.backStack.isNotEmpty()) {
                    navigation.pop { configurations ->
                        listOf(ProfileChildConfig.Menu)
                    }
                }
            }
        }
        scope.launch {
            store.labels.collect { label ->
                when (label) {
                    is LoginLabel.OpenBrowser -> _browserUrl.emit(label.url)
                    is LoginLabel.NavigateToDetails -> onNavigateToDetails(label.movieId)
                    is LoginLabel.NavigateToSection -> navigation.push(ProfileChildConfig.Section(label.item))
                }
            }
        }
        lifecycle.doOnDestroy {
            store.dispose()
            scope.cancel()
        }
    }

    override fun onIntent(intent: LoginIntent) {
        store.accept(intent)
    }

    override fun onBack(): Boolean {
        if (childStack.value.backStack.isNotEmpty()) {
            navigation.pop()
            return true
        }
        return false
    }

    private fun child(config: ProfileChildConfig, childContext: ComponentContext): LoginChild =
        when (config) {
            ProfileChildConfig.Menu -> LoginChild.Menu
            is ProfileChildConfig.Section -> LoginChild.Section(config.item)
        }
}

private fun LoginState.toModel(): LoginModel =
    LoginModel(
        sessionState = sessionState,
        isLoading = isLoading,
        isAwaitingApproval = isAwaitingApproval,
        error = error,
        favorites = favorites,
        isFavoritesLoading = isFavoritesLoading,
        favoritesError = favoritesError,
    )
