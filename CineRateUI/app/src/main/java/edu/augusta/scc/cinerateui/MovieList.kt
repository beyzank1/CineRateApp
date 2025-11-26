package edu.augusta.scc.cinerateui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun MovieList(
    movies: List<Movie>,
    onMovieClick: (Movie) -> Unit,
    onWriteReviewClick: (Movie) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(movies) { movie ->
            MovieRow(
                movie = movie,
                onClick = { onMovieClick(movie) },
                onWriteReviewClick = { onWriteReviewClick(movie) }
            )
        }
    }
}