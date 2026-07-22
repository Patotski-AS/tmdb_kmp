package pas.tmdb.feature.details.api

import com.arkivanov.decompose.value.Value
import kotlinx.coroutines.flow.Flow
import pas.tmdb.auth.api.SessionState

interface DetailsComponent {
    val model: Value<DetailsModel>
    val externalUrlEvents: Flow<String>
    fun onIntent(intent: DetailsIntent)
}

data class DetailsCastMember(
    val id: Int,
    val name: String,
    val character: String,
    val profileUrl: String?,
)

data class DetailsCrewMember(
    val name: String,
    val jobLabel: String,
)

data class DetailsVideo(
    val key: String,
    val name: String,
    val typeLabel: String,
)

data class DetailsImage(
    val url: String,
)

data class DetailsRecommendation(
    val id: Int,
    val title: String,
    val posterUrl: String?,
    val voteAverage: Double?,
)

enum class DetailsImageGallerySection {
    Backdrops,
    Posters,
}

data class DetailsImageGallery(
    val section: DetailsImageGallerySection,
    val index: Int,
)

data class DetailsModel(
    val mediaType: DetailsMediaType = DetailsMediaType.Movie,
    val title: String? = null,
    val overview: String? = null,
    val posterUrl: String? = null,
    val backdropUrl: String? = null,
    val tagline: String? = null,
    val voteAverage: Double? = null,
    val releaseDate: String? = null,
    val runtime: Int? = null,
    val genres: List<String> = emptyList(),
    val originalTitle: String? = null,
    val status: String? = null,
    val originalLanguage: String? = null,
    val budget: Long? = null,
    val revenue: Long? = null,
    val numberOfSeasons: Int? = null,
    val numberOfEpisodes: Int? = null,
    val cast: List<DetailsCastMember> = emptyList(),
    val showFullCast: Boolean = false,
    val crew: List<DetailsCrewMember> = emptyList(),
    val videos: List<DetailsVideo> = emptyList(),
    val backdrops: List<DetailsImage> = emptyList(),
    val posters: List<DetailsImage> = emptyList(),
    val recommendations: List<DetailsRecommendation> = emptyList(),
    val selectedVideoKey: String? = null,
    val imageGallery: DetailsImageGallery? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val sessionState: SessionState = SessionState.Guest,
    val isFavorite: Boolean? = null,
    val isFavoriteLoading: Boolean = false,
)

sealed interface DetailsIntent {
    data object Load : DetailsIntent
    data object Retry : DetailsIntent
    data object Back : DetailsIntent
    data object ToggleFavorite : DetailsIntent
    data class OnCastMemberClick(val personId: Int) : DetailsIntent
    data class OnVideoClick(val videoKey: String) : DetailsIntent
    data class OnImageClick(
        val section: DetailsImageGallerySection,
        val index: Int,
    ) : DetailsIntent
    data class OnRecommendationClick(val id: Int) : DetailsIntent
    data object ShowFullCast : DetailsIntent
    data object DismissVideoPlayer : DetailsIntent
    data object DismissImageGallery : DetailsIntent
}
