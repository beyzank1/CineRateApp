package edu.augusta.scc.cinerateui

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.foundation.clickable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.material3.IconToggleButton
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.ArrowBack

import edu.augusta.scc.cinerateui.ui.theme.CineRateUITheme

import androidx.activity.viewModels


class MainActivity : ComponentActivity() {

    private val repository = MovieRepositoryFake()

    private val searchViewModel: SearchViewModel by viewModels {
        SearchViewModel.factory(repository)
    }

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CineRateUITheme {
                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(WindowInsets.systemBars.asPaddingValues())
                ) {
                    MovieScreen()
                }
            }
        }

    }
}


data class Movie(
    val id: String,
    val title: String,
    val description: String,
    val cast: List<String>,
    val avgRating: Double,
    val posterUrl: String? = null,
    val rottenTomatoesScore: Int? = null, // 0–100
    val year: Int? = null
)


val fakeMovies = listOf(
    Movie(
        id = "1",
        title = "Inception",
        description = "A thief enters people's dreams to steal their secrets.",
        cast = listOf("Leonardo DiCaprio", "Elliot Page", "Tom Hardy"),
        avgRating = 4.7,
        rottenTomatoesScore = 87,
        year = 2010
    ),
    Movie(
        id = "2",
        title = "Interstellar",
        description = "A team travels through a wormhole in space to save humanity.",
        cast = listOf("Matthew McConaughey", "Anne Hathaway", "Jessica Chastain"),
        avgRating = 4.8,
        rottenTomatoesScore = 73,
        year = 2014
    )
)


@Composable
fun MovieScreen() {
    var query by remember { mutableStateOf("") }

    var showReviewDialog by remember { mutableStateOf(false) }
    var reviewMovie by remember { mutableStateOf<Movie?>(null) }

    var detailMovie by remember { mutableStateOf<Movie?>(null) } // <– NEW

    val moviesToShow = remember(query) {
        if (query.isBlank()) fakeMovies
        else fakeMovies.filter { it.title.contains(query, ignoreCase = true) }
    }

    if (detailMovie == null) {
        // 🔹 LIST SCREEN
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = "Movie Rating App",
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                label = { Text("Search movies") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            MovieList(
                movies = moviesToShow,
                onMovieClick = { movie -> detailMovie = movie },
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
            onBack = { detailMovie = null },
            onWriteReviewClick = { movie ->
                reviewMovie = movie
                showReviewDialog = true
            }
        )
    }

    // 🔹 Shared write-review dialog
    if (showReviewDialog && reviewMovie != null) {
        WriteReviewDialog(
            movie = reviewMovie!!,
            onDismiss = { showReviewDialog = false },
            onSubmit = { rating, text ->
                // TODO: hook to backend later
                showReviewDialog = false
            }
        )
    }
}



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



@Composable
fun MovieRow(
    movie: Movie,
    onClick: () -> Unit,
    onWriteReviewClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }       // <– whole row clickable
            .padding(8.dp)
    ) {
        // LEFT: Poster + edit icon (unchanged except for param names)
        Box(
            modifier = Modifier
                .size(width = 90.dp, height = 130.dp)
                .padding(end = 12.dp)
        ) {
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = MaterialTheme.shapes.medium
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Poster")
                }
            }

            IconButton(
                onClick = onWriteReviewClick,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Edit,
                    contentDescription = "Write review"
                )
            }
        }

        // RIGHT: summary info (as you already had)
        Column(modifier = Modifier.weight(1f)) {
            Text(movie.title, style = MaterialTheme.typography.titleMedium)
            movie.year?.let {
                Text(it.toString(), style = MaterialTheme.typography.bodySmall)
            }
            Spacer(Modifier.height(4.dp))
            Text(movie.description, maxLines = 2)
            Spacer(Modifier.height(4.dp))
            Text(
                "Cast: " + movie.cast.joinToString(", "),
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(Modifier.height(4.dp))
            Text("⭐ ${movie.avgRating}")
        }
    }
}


@Composable
fun WriteReviewDialog(
    movie: Movie,
    onDismiss: () -> Unit,
    onSubmit: (rating: Int, text: String) -> Unit
) {
    var rating by remember { mutableStateOf(0) }
    var text by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Poster placeholder (will swap with Coil later)
                Surface(
                    modifier = Modifier
                        .size(60.dp)
                        .padding(end = 12.dp),
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Poster")
                    }
                }

                // Movie title
                Text(
                    text = movie.title,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        },
        text = {
            Column {

                Spacer(Modifier.height(8.dp))

                // Optional: Short description preview
                Text(
                    text = movie.description,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2
                )

                Spacer(Modifier.height(16.dp))

                // Rating
                Text("Your rating")
                Spacer(Modifier.height(4.dp))

                RatingRow(
                    selected = rating,
                    onSelected = { rating = it }
                )

                Spacer(Modifier.height(16.dp))

                // Review text
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text("Write your review") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onSubmit(rating, text) }) {
                Text("Submit")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}


@Composable
fun RatingRow(
    selected: Int,
    onSelected: (Int) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        for (i in 1..5) {
            IconToggleButton(
                checked = i <= selected,
                onCheckedChange = { onSelected(i) }
            ) {
                val icon = if (i <= selected) {
                    Icons.Filled.Star
                } else {
                    Icons.Outlined.Star
                }
                Icon(
                    imageVector = icon,
                    contentDescription = "Star $i"
                )
            }
        }
    }
}
@Composable
fun FakeReviewsSection(movieId: String) {
    // for now this is just hardcoded; later you’ll pull from backend
    val reviews = listOf(
        "Loved the visuals and concept.",
        "A bit confusing at times, but amazing.",
        "One of my favorite movies ever."
    )

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        reviews.forEach { review ->
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.small,
                tonalElevation = 1.dp
            ) {
                Text(
                    text = review,
                    modifier = Modifier.padding(8.dp),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
fun MovieDetailScreen(
    movie: Movie,
    onBack: () -> Unit,
    onWriteReviewClick: (Movie) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Top row: back button + title
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "Back"
                )
            }
            Spacer(Modifier.width(8.dp))
            Text(
                text = movie.title,
                style = MaterialTheme.typography.titleLarge
            )
        }

        Spacer(Modifier.height(16.dp))

        // Poster placeholder
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Poster")
            }
        }

        Spacer(Modifier.height(16.dp))

        // Ratings row
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Our rating", style = MaterialTheme.typography.labelMedium)
                Text("⭐ %.1f / 5".format(movie.avgRating))
            }
            movie.rottenTomatoesScore?.let { rt ->
                Column {
                    Text("Rotten Tomatoes", style = MaterialTheme.typography.labelMedium)
                    Text("$rt%")
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        movie.year?.let {
            Text("Released: $it", style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(8.dp))
        }

        Text("Description", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(4.dp))
        Text(movie.description, style = MaterialTheme.typography.bodyMedium)

        Spacer(Modifier.height(16.dp))

        Text("Cast", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(4.dp))
        Text(
            movie.cast.joinToString(", "),
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = { onWriteReviewClick(movie) },
            modifier = Modifier.align(Alignment.End)
        ) {
            Icon(
                imageVector = Icons.Filled.Edit,
                contentDescription = null
            )
            Spacer(Modifier.width(8.dp))
            Text("Write a review")
        }

        Spacer(Modifier.height(24.dp))

        Text("Other reviews", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))

        FakeReviewsSection(movieId = movie.id)
    }
}


