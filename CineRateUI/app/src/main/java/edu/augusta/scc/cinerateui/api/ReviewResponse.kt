package edu.augusta.scc.cinerateui.api

data class ReviewResponse(
    val scoreId: Long?,
    val movieId: String,
    val authorId: Int,
    val value: Int,
    val review: String
)