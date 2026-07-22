package pas.tmdb.app

import androidx.compose.runtime.Composable
import com.arkivanov.decompose.ComponentContext

@Composable
expect fun rememberRootComponentContext(): ComponentContext
