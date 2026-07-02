package pas.tmdb.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.SubcomposeAsyncImage
import coil3.compose.SubcomposeAsyncImageContent

data class MediaPosterItem(
    val id: Int,
    val title: String,
    val posterUrl: String?,
    val voteAverage: Double? = null,
    val isFavorite: Boolean = false,
)

object TmdbPosterColors {
    val frameDefault: Color = Color.Black
    val frameFavorite: Color = Color(0xFFFF6B00)
    val favoriteStar: Color = Color(0xFFFFC107)
    val placeholderBackground: Color = TmdbColors.SurfaceVariant
}

private val MediaPosterAspectRatio = 2f / 3f
private val MediaTitleHeight = 40.dp
private val MediaFrameShape = RoundedCornerShape(10.dp)
private val MediaPosterShape = RoundedCornerShape(6.dp)
private val MediaFrameBorderWidth = 2.dp
private val MediaFramePadding = 6.dp

@Composable
fun MediaPosterCell(
    item: MediaPosterItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    MediaPosterFrame(
        isFavorite = item.isFavorite,
        onClick = onClick,
        modifier = modifier,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(MediaPosterAspectRatio),
        ) {
            PosterImage(
                posterUrl = item.posterUrl,
                contentDescription = item.title,
                modifier = Modifier.fillMaxSize(),
            )
            if (item.isFavorite) {
                FavoriteStarBadge(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp),
                )
            }
            item.voteAverage?.let { rating ->
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = MaterialTheme.colorScheme.scrim.copy(alpha = 0.72f),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(6.dp),
                ) {
                    Text(
                        text = "★ ${"%.1f".format(rating)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    )
                }
            }
        }
        Text(
            text = item.title,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Start,
            modifier = Modifier
                .fillMaxWidth()
                .height(MediaTitleHeight)
                .padding(top = 8.dp),
        )
    }
}

@Composable
private fun FavoriteStarBadge(
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.scrim.copy(alpha = 0.72f),
        modifier = modifier.size(28.dp),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = "★",
                style = MaterialTheme.typography.titleSmall,
                color = TmdbPosterColors.favoriteStar,
            )
        }
    }
}

@Composable
private fun MediaPosterFrame(
    isFavorite: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val borderColor = if (isFavorite) {
        TmdbPosterColors.frameFavorite
    } else {
        TmdbPosterColors.frameDefault
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(MediaFrameShape)
            .border(
                width = MediaFrameBorderWidth,
                color = borderColor,
                shape = MediaFrameShape,
            )
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick)
            .padding(MediaFramePadding),
    ) {
        content()
    }
}

@Composable
fun PosterImage(
    posterUrl: String?,
    contentDescription: String,
    modifier: Modifier = Modifier,
) {
    if (posterUrl.isNullOrBlank()) {
        PosterPlaceholder(modifier = modifier.clip(MediaPosterShape))
        return
    }

    SubcomposeAsyncImage(
        model = posterUrl,
        contentDescription = contentDescription,
        modifier = modifier.clip(MediaPosterShape),
        contentScale = ContentScale.Crop,
        loading = {
            PosterLoadingState(modifier = Modifier.fillMaxSize())
        },
        error = {
            PosterPlaceholder(modifier = Modifier.fillMaxSize())
        },
        success = {
            SubcomposeAsyncImageContent()
        },
    )
}

@Composable
fun PosterPlaceholder(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.background(TmdbPosterColors.placeholderBackground),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "🎬",
            style = MaterialTheme.typography.headlineMedium,
        )
    }
}

@Composable
private fun PosterLoadingState(
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
        PosterPlaceholder(modifier = Modifier.fillMaxSize())
        ShimmerBox(
            modifier = Modifier
                .fillMaxSize()
                .blur(10.dp),
        )
    }
}

@Composable
fun MediaPosterGrid(
    items: List<MediaPosterItem>,
    isLoadingMore: Boolean,
    endReached: Boolean,
    onLoadMore: () -> Unit,
    onItemClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(16.dp),
) {
    val gridState = rememberSaveable(
        saver = LazyGridState.Saver,
    ) {
        LazyGridState()
    }

    LaunchedEffect(gridState, items.size, endReached, isLoadingMore) {
        snapshotFlow {
            val layoutInfo = gridState.layoutInfo
            val lastVisibleIndex = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            lastVisibleIndex to layoutInfo.totalItemsCount
        }.collect { (lastVisibleIndex, totalItems) ->
            if (totalItems > 0 && lastVisibleIndex >= totalItems - 4 && !endReached && !isLoadingMore) {
                onLoadMore()
            }
        }
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        state = gridState,
        contentPadding = contentPadding,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier.fillMaxSize(),
    ) {
        items(items, key = { it.id }) { item ->
            MediaPosterCell(
                item = item,
                onClick = { onItemClick(item.id) },
            )
        }

        if (isLoadingMore) {
            item(key = "loading_more", span = { GridItemSpan(2) }) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(32.dp))
                }
            }
        }
    }
}

@Composable
fun ShimmerMediaGrid(
    itemCount: Int = 12,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(16.dp),
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = contentPadding,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier.fillMaxSize(),
        userScrollEnabled = false,
    ) {
        items(itemCount) {
            ShimmerPosterCell()
        }
    }
}

@Composable
private fun ShimmerPosterCell(
    modifier: Modifier = Modifier,
) {
    MediaPosterFrame(
        isFavorite = false,
        onClick = {},
        modifier = modifier,
    ) {
        ShimmerBox(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(MediaPosterAspectRatio),
        )
        ShimmerBox(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
                .height(MediaTitleHeight),
        )
    }
}

@Composable
fun MediaGridLoadingState(
    modifier: Modifier = Modifier,
    posterPlaceholderCount: Int = 12,
) {
    Box(modifier = modifier.fillMaxSize()) {
        ShimmerMediaGrid(
            itemCount = posterPlaceholderCount,
            modifier = Modifier.fillMaxSize(),
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(TmdbColors.DarkBlue.copy(alpha = 0.45f)),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator(modifier = Modifier.size(48.dp))
        }
    }
}
