package pas.tmdb.feature.media.impl

import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathBuilder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

private val FilterTuneIcon: ImageVector by lazy {
    ImageVector.Builder(
        name = "FilterTune",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        val color = SolidColor(Color.Black)
        val trackWidth = 2f
        val knobRadius = 2.8f
        val gap = knobRadius + 1.2f

        // y-позиция дорожки и x-позиция ручки для каждого из трёх слайдеров
        val rows = listOf(
            6f to 16f,
            12f to 8f,
            18f to 15f,
        )

        rows.forEach { (y, knobX) ->
            path(
                fill = null,
                stroke = color,
                strokeLineWidth = trackWidth,
                strokeLineCap = StrokeCap.Round,
            ) {
                moveTo(3f, y)
                lineTo(knobX - gap, y)
                moveTo(knobX + gap, y)
                lineTo(21f, y)
            }
            path(fill = color) {
                addCircle(knobX, y, radius = knobRadius)
            }
        }
    }.build()
}

private fun PathBuilder.addCircle(centerX: Float, centerY: Float, radius: Float) {
    moveTo(centerX + radius, centerY)
    arcToRelative(
        a = radius,
        b = radius,
        theta = 0f,
        isMoreThanHalf = true,
        isPositiveArc = true,
        dx1 = -radius * 2f,
        dy1 = 0f,
    )
    arcToRelative(
        a = radius,
        b = radius,
        theta = 0f,
        isMoreThanHalf = true,
        isPositiveArc = true,
        dx1 = radius * 2f,
        dy1 = 0f,
    )
    close()
}

@Composable
internal fun FilterFabIcon(
    modifier: Modifier = Modifier,
    contentDescription: String = "Фильтры",
) {
    Icon(
        imageVector = FilterTuneIcon,
        contentDescription = contentDescription,
        modifier = modifier,
    )
}
