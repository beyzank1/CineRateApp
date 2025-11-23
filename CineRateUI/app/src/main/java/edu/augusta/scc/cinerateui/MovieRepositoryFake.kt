package edu.augusta.scc.cinerateui

class MovieRepositoryFake : MovieRepository {

    private val movies = listOf(
        Movie(
            id = "1",
            title = "Inception",
            description = "A thief enters people's dreams to steal their secrets.",
            cast = listOf("Leonardo DiCaprio", "Elliot Page", "Tom Hardy"),
            avgRating = 4.7
        ),
        Movie(
            id = "2",
            title = "Interstellar",
            description = "A team travels through a wormhole in space to save humanity.",
            cast = listOf("Matthew McConaughey", "Anne Hathaway", "Jessica Chastain"),
            avgRating = 4.8
        )
        // add more here if you want to see more rows
    )

    override suspend fun getPopularMovies(): Result<List<Movie>> {
        // later: call server; for now just return fake list
        return Result.success(movies)
    }

    override suspend fun searchMovies(query: String): Result<List<Movie>> {
        if (query.isBlank()) return Result.success(movies)
        val q = query.lowercase()
        return Result.success(
            movies.filter { it.title.lowercase().contains(q) }
        )
    }

    override suspend fun submitReview(movieId: String, rating: Int, text: String): Result<Unit> {
        // later: write to local DB, queue for sync, send to server, etc.
        println("FAKE REVIEW: movieId=$movieId rating=$rating text=$text")
        return Result.success(Unit)
    }
}