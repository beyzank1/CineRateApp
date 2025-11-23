package edu.augusta.scc.cinerateui

interface MovieRepository {
    suspend fun getPopularMovies(): Result<List<Movie>>
    suspend fun searchMovies(query: String): Result<List<Movie>>
    suspend fun submitReview(movieId: String, rating: Int, text: String): Result<Unit>
}