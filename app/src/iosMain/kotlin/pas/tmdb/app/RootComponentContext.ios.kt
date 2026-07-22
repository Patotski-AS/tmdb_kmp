package pas.tmdb.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.backhandler.BackDispatcher
import com.arkivanov.essenty.lifecycle.LifecycleRegistry

@Composable
actual fun rememberRootComponentContext(): ComponentContext =
    remember {
        DefaultComponentContext(
            lifecycle = LifecycleRegistry(),
            backHandler = BackDispatcher(),
        )
    }
