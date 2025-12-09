package edu.augusta.scc.cinerateui.api

data class ReviewRequest(
    val movieId: String,
    val value: Int,
    val review: String,
    val authorId: Int = 1   // temp until auth is real
)
