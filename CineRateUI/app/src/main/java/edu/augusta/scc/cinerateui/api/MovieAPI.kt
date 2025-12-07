package edu.augusta.scc.cinerateui.api

import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

import edu.augusta.scc.cinerateui.Review




data class MovieRecord(
    val originalQuery: String?,
    val title: String?,
    val year: String?,
    val imdbId: String?,
    val type: String?,
    val poster: String?
)

interface MovieApi {

    @GET("/search")
    suspend fun searchMovies(
        @Query("q") query: String
    ): List<MovieRecord>

    @GET("/")
    suspend fun health(): String

    @GET("/reviews/{movieId}")
    suspend fun getReviews(
        @Path("movieId") movieId: String
    ): List<Review>

    @POST("/reviews")
    suspend fun submitReview(
        @Body review: ReviewRequest
    )

}