package pas.tmdb.feature.login.impl

import com.arkivanov.decompose.ComponentContext
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
import pas.tmdb.feature.login.api.LoginComponent
import pas.tmdb.feature.login.api.LoginIntent
import pas.tmdb.feature.login.api.LoginModel
import pas.tmdb.feature.login.impl.store.LoginLabel
import pas.tmdb.feature.login.impl.store.LoginState
import pas.tmdb.feature.login.impl.store.LoginStoreFactory

internal class DefaultLoginComponent(
    componentContext: ComponentContext,
    storeFactory: LoginStoreFactory,
) : LoginComponent, ComponentContext by componentContext {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private val store = storeFactory.create()

    private val _model = MutableValue(LoginModel())

    override val model: Value<LoginModel> = _model

    private val _browserUrl = MutableSharedFlow<String>(extraBufferCapacity = 1)
    override val browserUrlEvents: SharedFlow<String> = _browserUrl.asSharedFlow()

    init {
        scope.launch {
            store.states.collect { state ->
                _model.value = state.toModel()
            }
        }
        scope.launch {
            store.labels.collect { label ->
                when (label) {
                    is LoginLabel.OpenBrowser -> _browserUrl.emit(label.url)
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
}

private fun LoginState.toModel(): LoginModel =
    LoginModel(
        sessionState = sessionState,
        isLoading = isLoading,
        isAwaitingApproval = isAwaitingApproval,
        error = error,
    )
