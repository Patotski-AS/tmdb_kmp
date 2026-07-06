package pas.tmdb.feature.media.impl.store

import pas.tmdb.feature.media.api.ContentType
import pas.tmdb.feature.media.api.MediaFilters
import pas.tmdb.feature.media.api.MediaGenre
import pas.tmdb.feature.media.api.MediaItemModel

data class MediaState(
    val contentType: ContentType = ContentType.Movies,
    val searchQuery: String = "",
    val filters: MediaFilters = MediaFilters(),
    val genres: List<MediaGenre> = emptyList(),
    val items: List<MediaItemModel> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val error: String? = null,
    val page: Int = 0,
    val endReached: Boolean = false,
    val favoriteIds: Set<Int> = emptySet(),
)
