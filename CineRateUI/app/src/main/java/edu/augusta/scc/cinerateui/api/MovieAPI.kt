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
    val poster: String?,
    val plot: String?
)

interface MovieApi {

    @GET("/search")
    suspend fun searchMovies(
        @Query("q") query: String
    ): List<MovieRecord>

    @POST("/reviews")
    suspend fun submitReview(
        @Body req: ReviewRequest
    )

    @GET("/reviews/{movieId}")
    suspend fun getReviews(
        @Path("movieId") movieId: String
    ): List<ReviewResponse>

    @GET("/movie/{imdbId}")
    suspend fun getMovieDetails(
        @Path("imdbId") imdbId: String
    ): MovieRecord



}