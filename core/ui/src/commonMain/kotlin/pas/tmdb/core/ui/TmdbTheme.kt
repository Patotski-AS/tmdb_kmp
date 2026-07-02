package pas.tmdb.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Официальные цвета бренда TMDB:
 * https://www.themoviedb.org/about/logos-attribution
 */
object TmdbColors {
    val DarkBlue = Color(0xFF0D253F)
    val DarkBlueDeep = Color(0xFF081522)
    val LightBlue = Color(0xFF01B4E4)
    val LightGreen = Color(0xFF90CEA1)
    val Surface = Color(0xFF152B40)
    val SurfaceVariant = Color(0xFF1C3448)
    val OnDark = Color(0xFFE8EEF3)
    val OnDarkMuted = Color(0xFF9EACBA)
    val Outline = Color(0xFF2A4560)
}

private val TmdbDarkColorScheme = darkColorScheme(
    primary = TmdbColors.LightBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF015A73),
    onPrimaryContainer = Color.White,
    secondary = TmdbColors.LightGreen,
    onSecondary = TmdbColors.DarkBlue,
    secondaryContainer = Color(0xFF3D6B52),
    onSecondaryContainer = TmdbColors.OnDark,
    tertiary = TmdbColors.LightGreen,
    onTertiary = TmdbColors.DarkBlue,
    background = TmdbColors.DarkBlue,
    onBackground = TmdbColors.OnDark,
    surface = TmdbColors.Surface,
    onSurface = TmdbColors.OnDark,
    surfaceVariant = TmdbColors.SurfaceVariant,
    onSurfaceVariant = TmdbColors.OnDarkMuted,
    surfaceContainer = TmdbColors.Surface,
    surfaceContainerLow = Color(0xFF122A3D),
    surfaceContainerHigh = Color(0xFF1F3A50),
    surfaceContainerHighest = Color(0xFF25455C),
    outline = TmdbColors.Outline,
    outlineVariant = Color(0xFF223A4F),
    error = Color(0xFFFF8A80),
    onError = Color(0xFF4A0000),
)

@Composable
fun TmdbTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = TmdbDarkColorScheme,
        content = {
            TmdbBackground {
                content()
            }
        },
    )
}

@Composable
fun TmdbBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        TmdbColors.DarkBlue,
                        TmdbColors.DarkBlueDeep,
                    ),
                ),
            ),
    ) {
        content()
    }
}

@Composable
fun PlaceholderScreen(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
) {
    Surface(
        modifier = modifier,
        color = Color.Transparent,
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        }
    }
}
