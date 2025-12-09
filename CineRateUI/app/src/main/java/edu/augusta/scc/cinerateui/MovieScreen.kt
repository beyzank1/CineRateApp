package edu.augusta.scc.cinerateui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.rememberCoroutineScope

import edu.augusta.scc.cinerateui.api.ApiClient
import edu.augusta.scc.cinerateui.api.ReviewRequest
import kotlinx.coroutines.launch



@Composable
fun MovieScreen(username: String,
                modifier: Modifier = Modifier) {
    val context = LocalContext.current

    var query by remember { mutableStateOf("") }
    var movies by remember { mutableStateOf<List<Movie>>(emptyList()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    var showReviewDialog by remember { mutableStateOf(false) }
    var reviewMovie by remember { mutableStateOf<Movie?>(null) }

    var initialLoaded by remember { mutableStateOf(false) }

    var detailMovie by remember { mutableStateOf<Movie?>(null) }
    val scope = rememberCoroutineScope()
    var reviewRefreshKey by remember { mutableStateOf(0) }




    LaunchedEffect(Unit) {
        val starter = StarterMoviesLoader.load(context)

        println("DEBUG: MovieScreen initial load, starter size = ${starter.size}")

        movies = starter.map {
            Movie(
                id = it.imdbId ?: "",
                title = it.title ?: "",
                description = it.plot ?: "Year: ${it.year}",
                avgRating = 0.0,
                posterUrl = it.poster,
                year = it.year?.toIntOrNull()
            )
        }
    }

    LaunchedEffect(query) {
        if (query.isBlank()) {
            if (!initialLoaded) return@LaunchedEffect
            movies = emptyList()
            errorMessage = null
            return@LaunchedEffect
        }

        try {
            val results = ApiClient.api.searchMovies(query)

            movies = results.map {
                Movie(
                    id = it.imdbId ?: "",
                    title = it.title ?: "Unknown Movie",
                    description = it.plot ?: "Year: ${it.year}",
                    avgRating = 0.0,
                    posterUrl = it.poster,
                    year = it.year?.toIntOrNull()
                )
            }

            initialLoaded = true
            errorMessage = null

        } catch (e: Exception) {
            e.printStackTrace()
            movies = emptyList()
            errorMessage = e.message ?: "Failed to load movies"
        }
    }




    if (detailMovie == null) {
        // 🔹 LIST SCREEN
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp)
        )
        {
            Text(
                text = "Welcome, $username",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Movie Rating App",
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                label = { Text("Search movies") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(
                    onSearch = {

                    }
                )
            )

            Spacer(Modifier.height(16.dp))

            Text(
                text = "Results: ${movies.size}",
                style = MaterialTheme.typography.bodySmall
            )
            if (errorMessage != null) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = errorMessage!!,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Spacer(Modifier.height(8.dp))


            MovieList(
                movies = movies,
                onMovieClick = { movie ->
                    scope.launch {
                        try {

                            val fullMovie = ApiClient.api.getMovieDetails(movie.id)


                            detailMovie = movie.copy(
                                description = fullMovie.plot ?: movie.description,
                                posterUrl = fullMovie.poster ?: movie.posterUrl,
                                year = fullMovie.year?.toIntOrNull() ?: movie.year
                            )

                        } catch (e: Exception) {
                            e.printStackTrace()


                            detailMovie = movie
                        }
                    }
                },
                onWriteReviewClick = { movie ->
                    reviewMovie = movie
                    showReviewDialog = true
                }
            )

        }
    } else {
        // 🔹 DETAIL SCREEN
        MovieDetailScreen(
            movie = detailMovie!!,
            reviewRefreshKey = reviewRefreshKey,
            onBack = { detailMovie = null },
            onWriteReviewClick = { movie ->
                reviewMovie = movie
                showReviewDialog = true
            },
            onAverageUpdated = { avg ->
                detailMovie = detailMovie!!.copy(avgRating = avg)
            }
        )

    }

    // 🔹 Shared write-review dialog
    if (showReviewDialog && reviewMovie != null) {
        WriteReviewDialog(
            movie = reviewMovie!!,
            onDismiss = { showReviewDialog = false },
            onSubmit = { rating, text ->

                if (rating == 0 || text.isBlank()) {
                    return@WriteReviewDialog  
                }

                showReviewDialog = false

                scope.launch {
                    try {
                        ApiClient.api.submitReview(
                            ReviewRequest(
                                movieId = reviewMovie!!.id,
                                value = rating,
                                review = text
                            )
                        )

                        reviewRefreshKey++

                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        )
    }
}