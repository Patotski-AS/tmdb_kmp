package pas.tmdb.feature.media.impl.store

import pas.tmdb.auth.api.SessionState
import pas.tmdb.feature.media.api.ContentType
import pas.tmdb.feature.media.api.MediaFilters
import pas.tmdb.feature.media.api.MediaGenre
import pas.tmdb.feature.media.api.MediaItemModel

data class MediaState(
    val contentType: ContentType = ContentType.Movies,
    val searchQuery: String = "",
    val filters: MediaFilters = MediaFilters(),
    val movieGenres: List<MediaGenre> = emptyList(),
    val tvGenres: List<MediaGenre> = emptyList(),
    val items: List<MediaItemModel> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val error: String? = null,
    val page: Int = 0,
    val endReached: Boolean = false,
    val favoriteIds: Set<Int> = emptySet(),
    val sessionState: SessionState = SessionState.Guest,
) {
    val genres: List<MediaGenre>
        get() = when (contentType) {
            ContentType.Movies -> movieGenres
            ContentType.Tv -> tvGenres
            ContentType.People -> emptyList()
        }
}
