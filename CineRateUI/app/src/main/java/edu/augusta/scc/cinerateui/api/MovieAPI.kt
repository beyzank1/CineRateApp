package edu.augusta.scc.cinerateui.api

import retrofit2.http.GET
import retrofit2.http.Query

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
}