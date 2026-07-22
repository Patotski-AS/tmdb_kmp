package pas.tmdb.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private const val TMDB_SITE_URL = "https://www.themoviedb.org"
private const val TMDB_ATTRIBUTION_URL = "https://www.themoviedb.org/about/logos-attribution"

/**
 * TMDB attribution footer.
 * Required by [TMDB terms](https://www.themoviedb.org/about/logos-attribution).
 *
 * Brand colors: primary `#0d253f`, secondary `#01b4e4`, tertiary `#90cea1`.
 */
@Composable
fun TmdbAttribution(
    modifier: Modifier = Modifier,
    compact: Boolean = false,
) {
    val uriHandler = LocalUriHandler.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable { uriHandler.openUri(TMDB_SITE_URL) }
            .padding(
                horizontal = 16.dp,
                vertical = if (compact) 8.dp else 16.dp,
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(if (compact) 6.dp else 10.dp),
    ) {
        Text(
            text = "This product uses the TMDB API but is not endorsed or certified by TMDB.",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.clickable { uriHandler.openUri(TMDB_ATTRIBUTION_URL) },
        )
    }
}
