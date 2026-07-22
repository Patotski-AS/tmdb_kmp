package pas.tmdb.feature.details.impl

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import pas.tmdb.auth.api.SessionState
import pas.tmdb.core.ui.CastItem
import pas.tmdb.core.ui.CastRow
import pas.tmdb.core.ui.DetailsHeroSection
import pas.tmdb.core.ui.DetailsSectionTitle
import pas.tmdb.core.ui.ImageGalleryPager
import pas.tmdb.core.ui.MediaImageItem
import pas.tmdb.core.ui.MediaImageRow
import pas.tmdb.core.ui.MediaPosterItem
import pas.tmdb.core.ui.RecommendationPosterRow
import pas.tmdb.core.ui.ShimmerDetails
import pas.tmdb.core.ui.TmdbErrorState
import pas.tmdb.core.ui.VideoThumbnailItem
import pas.tmdb.core.ui.VideoThumbnailRow
import pas.tmdb.core.ui.YoutubeVideoOverlay
import pas.tmdb.feature.details.api.DetailsComponent
import pas.tmdb.feature.details.api.DetailsImageGallerySection
import pas.tmdb.feature.details.api.DetailsIntent
import pas.tmdb.feature.details.api.DetailsModel

private const val CAST_PREVIEW_COUNT = 15

@Composable
fun DetailsExternalUrlEffect(component: DetailsComponent) {
    val uriHandler = LocalUriHandler.current
    LaunchedEffect(component, uriHandler) {
        component.externalUrlEvents.collect { url ->
            uriHandler.openUri(url)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailsContent(
    component: DetailsComponent,
    modifier: Modifier = Modifier,
) {
    val model by component.model.subscribeAsState()

    DetailsExternalUrlEffect(component)

    Box(modifier = modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.onBackground,
            topBar = {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        titleContentColor = MaterialTheme.colorScheme.onBackground,
                        navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
                    ),
                    title = {},
                    navigationIcon = {
                        IconButton(
                            onClick = { component.onIntent(DetailsIntent.Back) },
                            modifier = Modifier.size(48.dp),
                        ) {
                            Text(
                                text = "←",
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.onBackground,
                            )
                        }
                    },
                )
            },
        ) { padding ->
            when {
                model.isLoading && model.title == null -> {
                    ShimmerDetails(modifier = Modifier.fillMaxSize().padding(padding))
                }

                model.error != null && model.title == null -> {
                    TmdbErrorState(
                        message = model.error.orEmpty(),
                        onRetry = { component.onIntent(DetailsIntent.Retry) },
                        modifier = Modifier.fillMaxSize().padding(padding),
                    )
                }

                else -> {
                    DetailsBody(
                        model = model,
                        onIntent = component::onIntent,
                        modifier = Modifier.fillMaxSize().padding(padding),
                    )
                }
            }
        }

        model.imageGallery?.let { gallery ->
            val images = when (gallery.section) {
                DetailsImageGallerySection.Backdrops -> model.backdrops.map { it.url }
                DetailsImageGallerySection.Posters -> model.posters.map { it.url }
            }
            if (images.isNotEmpty()) {
                ImageGalleryPager(
                    images = images,
                    initialIndex = gallery.index,
                    onDismiss = { component.onIntent(DetailsIntent.DismissImageGallery) },
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }

        model.selectedVideoKey?.let { videoKey ->
            YoutubeVideoOverlay(
                videoKey = videoKey,
                onDismiss = { component.onIntent(DetailsIntent.DismissVideoPlayer) },
            )
        }
    }
}

@Composable
private fun DetailsBody(
    model: DetailsModel,
    onIntent: (DetailsIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.verticalScroll(rememberScrollState()),
    ) {
        DetailsHeroSection(
            backdropUrl = model.backdropUrl,
            posterUrl = model.posterUrl,
            title = model.title.orEmpty(),
            voteAverage = model.voteAverage,
            tagline = model.tagline,
            favoriteSlot = {
                FavoriteAction(
                    sessionState = model.sessionState,
                    isFavorite = model.isFavorite,
                    isFavoriteLoading = model.isFavoriteLoading,
                    onToggleFavorite = { onIntent(DetailsIntent.ToggleFavorite) },
                )
            },
        )

        Column(
            modifier = Modifier.padding(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            OverviewSection(model = model)

            if (model.cast.isNotEmpty()) {
                CastSection(
                    model = model,
                    onIntent = onIntent,
                )
            }

            if (model.videos.isNotEmpty()) {
                DetailsSectionTitle(title = "Видео")
                VideoThumbnailRow(
                    items = model.videos.map { video ->
                        VideoThumbnailItem(
                            key = video.key,
                            title = video.name,
                            typeLabel = video.typeLabel,
                        )
                    },
                    onItemClick = { key -> onIntent(DetailsIntent.OnVideoClick(key)) },
                )
            }

            if (model.backdrops.isNotEmpty()) {
                DetailsSectionTitle(title = "Кадры")
                MediaImageRow(
                    items = model.backdrops.map { MediaImageItem(url = it.url) },
                    onItemClick = { index ->
                        onIntent(DetailsIntent.OnImageClick(DetailsImageGallerySection.Backdrops, index))
                    },
                    itemWidth = 220.dp,
                    itemHeight = 124.dp,
                )
            }

            if (model.posters.isNotEmpty()) {
                DetailsSectionTitle(title = "Постеры")
                MediaImageRow(
                    items = model.posters.map { MediaImageItem(url = it.url) },
                    onItemClick = { index ->
                        onIntent(DetailsIntent.OnImageClick(DetailsImageGallerySection.Posters, index))
                    },
                    itemWidth = 120.dp,
                    itemHeight = 180.dp,
                )
            }

            if (hasMetadata(model)) {
                MetadataSection(model = model)
            }

            if (model.recommendations.isNotEmpty()) {
                DetailsSectionTitle(title = "Рекомендации")
                RecommendationPosterRow(
                    items = model.recommendations.map { recommendation ->
                        MediaPosterItem(
                            id = recommendation.id,
                            title = recommendation.title,
                            posterUrl = recommendation.posterUrl,
                            voteAverage = recommendation.voteAverage,
                        )
                    },
                    onItemClick = { movieId -> onIntent(DetailsIntent.OnRecommendationClick(movieId)) },
                )
            }
        }
    }
}

@Composable
private fun OverviewSection(model: DetailsModel) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            model.releaseDate?.takeIf { it.isNotBlank() }?.let { releaseDate ->
                Text(
                    text = releaseDate,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            model.runtime?.takeIf { it > 0 }?.let { runtime ->
                Text(
                    text = formatRuntime(runtime),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        if (model.genres.isNotEmpty()) {
            Text(
                text = model.genres.joinToString(", "),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
            )
        }

        model.crew.forEach { member ->
            Text(
                text = "${member.jobLabel}: ${member.name}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }

        model.overview?.takeIf { it.isNotBlank() }?.let { overview ->
            Text(
                text = overview,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }
    }
}

@Composable
private fun CastSection(
    model: DetailsModel,
    onIntent: (DetailsIntent) -> Unit,
) {
    val visibleCast = if (model.showFullCast) {
        model.cast
    } else {
        model.cast.take(CAST_PREVIEW_COUNT)
    }

    DetailsSectionTitle(title = "В главных ролях")
    CastRow(
        items = visibleCast.map { member ->
            CastItem(
                id = member.id,
                name = member.name,
                subtitle = member.character,
                imageUrl = member.profileUrl,
            )
        },
        onItemClick = { personId -> onIntent(DetailsIntent.OnCastMemberClick(personId)) },
    )

    if (!model.showFullCast && model.cast.size > CAST_PREVIEW_COUNT) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.Center,
        ) {
            TextButton(onClick = { onIntent(DetailsIntent.ShowFullCast) }) {
                Text("Смотреть ещё")
            }
        }
    }
}

@Composable
private fun MetadataSection(model: DetailsModel) {
    DetailsSectionTitle(title = "Метаданные")
    Column(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        model.originalTitle?.let { originalTitle ->
            MetadataRow(label = "Исходное название", value = originalTitle)
        }
        model.status?.let { status ->
            MetadataRow(label = "Статус", value = status)
        }
        model.originalLanguage?.let { language ->
            MetadataRow(label = "Язык", value = language.uppercase())
        }
        model.budget?.let { budget ->
            MetadataRow(label = "Бюджет", value = formatCurrency(budget))
        }
        model.revenue?.let { revenue ->
            MetadataRow(label = "Сборы", value = formatCurrency(revenue))
        }
    }
}

@Composable
private fun MetadataRow(
    label: String,
    value: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
    }
}

private fun hasMetadata(model: DetailsModel): Boolean =
    model.originalTitle != null ||
        model.status != null ||
        model.originalLanguage != null ||
        model.budget != null ||
        model.revenue != null

private fun formatRuntime(minutes: Int): String {
    val hours = minutes / 60
    val mins = minutes % 60
    return when {
        hours > 0 && mins > 0 -> "$hours ч $mins мин"
        hours > 0 -> "$hours ч"
        else -> "$mins мин"
    }
}

private fun formatCurrency(amount: Long): String =
    when {
        amount >= 1_000_000_000 -> "$${formatAmount(amount / 1_000_000_000.0)} млрд"
        amount >= 1_000_000 -> "$${formatAmount(amount / 1_000_000.0)} млн"
        else -> "$$amount"
    }

private fun formatAmount(value: Double): String {
    val rounded = (value * 10).toLong() / 10.0
    return if (rounded % 1.0 == 0.0) {
        rounded.toLong().toString()
    } else {
        rounded.toString()
    }
}

@Composable
private fun FavoriteAction(
    sessionState: SessionState,
    isFavorite: Boolean?,
    isFavoriteLoading: Boolean,
    onToggleFavorite: () -> Unit,
) {
    when (sessionState) {
        is SessionState.Guest -> {
            TextButton(
                onClick = onToggleFavorite,
                contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
            ) {
                Text(
                    text = "Войти",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }

        is SessionState.Authenticated -> {
            TextButton(
                onClick = onToggleFavorite,
                enabled = !isFavoriteLoading,
                contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
            ) {
                Text(
                    text = when (isFavorite) {
                        true -> "★ В избранном"
                        false -> "☆ В избранное"
                        null -> if (isFavoriteLoading) "…" else "☆ В избранное"
                    },
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}
