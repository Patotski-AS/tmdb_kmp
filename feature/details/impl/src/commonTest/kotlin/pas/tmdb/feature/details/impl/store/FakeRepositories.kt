package pas.tmdb.feature.details.impl.store

import pas.tmdb.core.common.AppResult
import pas.tmdb.data.tmdb.api.AccountRepository
import pas.tmdb.data.tmdb.api.MovieRepository
import pas.tmdb.data.tmdb.api.model.CastMember
import pas.tmdb.data.tmdb.api.model.CrewMember
import pas.tmdb.data.tmdb.api.model.Genre
import pas.tmdb.data.tmdb.api.model.ImageConfiguration
import pas.tmdb.data.tmdb.api.model.MediaImage
import pas.tmdb.data.tmdb.api.model.Movie
import pas.tmdb.data.tmdb.api.model.MovieDetails
import pas.tmdb.data.tmdb.api.model.PagedMovies
import pas.tmdb.data.tmdb.api.model.Video

internal class FakeMovieRepository(
    private val movieDetails: AppResult<MovieDetails> = AppResult.Success(sampleMovieDetails()),
) : MovieRepository {
    var detailsCalls: Int = 0
    var lastMovieId: Int? = null

    override suspend fun getMovieGenres(): AppResult<List<Genre>> =
        AppResult.Success(emptyList())

    override suspend fun getPopularMovies(page: Int): AppResult<PagedMovies> =
        AppResult.Success(PagedMovies(emptyList(), page = 1, totalPages = 0, totalResults = 0))

    override suspend fun searchMovies(query: String, page: Int): AppResult<PagedMovies> =
        AppResult.Success(PagedMovies(emptyList(), page = 1, totalPages = 0, totalResults = 0))

    override suspend fun discoverMovies(
        genreId: Int?,
        sortBy: String?,
        page: Int,
    ): AppResult<PagedMovies> =
        AppResult.Success(PagedMovies(emptyList(), page = 1, totalPages = 0, totalResults = 0))

    override suspend fun getMovieDetails(movieId: Int): AppResult<MovieDetails> {
        detailsCalls++
        lastMovieId = movieId
        return movieDetails
    }

    override suspend fun getImageConfiguration(): AppResult<ImageConfiguration> =
        AppResult.Success(ImageConfiguration(posterBaseUrl = "https://image.tmdb.org/t/p/"))

    companion object {
        fun sampleMovieDetails(): MovieDetails =
            MovieDetails(
                id = 550,
                title = "Fight Club",
                overview = "Overview",
                posterUrl = "poster.jpg",
                voteAverage = 8.4,
                releaseDate = "1999-10-15",
                runtime = 139,
                genres = listOf("Драма"),
                tagline = "Mischief. Mayhem. Soap.",
                backdropUrl = "backdrop.jpg",
                originalTitle = "Fight Club",
                status = "Released",
                originalLanguage = "en",
                budget = 63_000_000L,
                revenue = 100_853_753L,
                cast = listOf(
                    CastMember(
                        id = 819,
                        name = "Edward Norton",
                        character = "The Narrator",
                        profileUrl = "profile.jpg",
                        order = 0,
                    ),
                ),
                crew = listOf(
                    CrewMember(id = 7467, name = "David Fincher", job = "Director"),
                    CrewMember(id = 7470, name = "Jim Uhls", job = "Screenplay"),
                ),
                videos = listOf(
                    Video(
                        id = "1",
                        key = "SUXWAEX2jlg",
                        name = "Official Trailer",
                        site = "YouTube",
                        type = "Trailer",
                        official = true,
                    ),
                ),
                backdrops = listOf(MediaImage(url = "backdrop1.jpg", aspectRatio = 1.78, width = 1280)),
                posters = listOf(MediaImage(url = "poster1.jpg", aspectRatio = 0.67, width = 500)),
                recommendations = listOf(
                    Movie(
                        id = 680,
                        title = "Pulp Fiction",
                        overview = "",
                        posterUrl = "rec.jpg",
                        voteAverage = 8.5,
                        releaseDate = "1994",
                    ),
                ),
            )
    }
}

internal class FakeAccountRepository : AccountRepository {
    override suspend fun isFavorite(movieId: Int): AppResult<Boolean> =
        AppResult.Success(false)

    override suspend fun setFavorite(movieId: Int, favorite: Boolean): AppResult<Unit> =
        AppResult.Success(Unit)

    override suspend fun getFavoriteMovieIds(): AppResult<Set<Int>> =
        AppResult.Success(emptySet())

    override suspend fun getFavoriteMovies(): AppResult<List<Movie>> =
        AppResult.Success(emptyList())
}

internal class FakeSessionRepository : pas.tmdb.auth.api.SessionRepository {
    override val sessionState = kotlinx.coroutines.flow.MutableStateFlow(
        pas.tmdb.auth.api.SessionState.Guest,
    )

    override suspend fun getSessionId(): String? = null

    override suspend fun logout() = Unit
}
