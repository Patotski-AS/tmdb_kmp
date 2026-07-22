package pas.tmdb.app

import android.content.Context
import android.content.ContextWrapper
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.defaultComponentContext

@Composable
actual fun rememberRootComponentContext(): ComponentContext {
    val activity = LocalContext.current.findComponentActivity()
    return remember(activity) { activity.defaultComponentContext() }
}

private fun Context.findComponentActivity(): ComponentActivity {
    var context = this
    while (context is ContextWrapper) {
        if (context is ComponentActivity) return context
        context = context.baseContext
    }
    error("ComponentActivity not found in context chain")
}
