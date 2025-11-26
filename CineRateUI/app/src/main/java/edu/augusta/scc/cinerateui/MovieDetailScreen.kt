package edu.augusta.scc.cinerateui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

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